package com.spartacoding.msa.eurekaclient.service;

import com.spartacoding.msa.eurekaclient.domain.Stock;
import com.spartacoding.msa.eurekaclient.domain.dto.StockDto;
import com.spartacoding.msa.eurekaclient.repository.StockRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final RedissonClient redissonClient;

    //동시성 문제 발생
    @Transactional
    public void updateStock(Long id) {
        Stock stock = stockRepository.findById(id).get();
        log.info("stock:" + stock.getStock());
        stock.increase();
    }


    //비관적 락
    @Transactional
    public void updateStockWithLock(Long id) {
        Stock stock = stockRepository.findByIdPerssimisticLock(id).get();
        stock.increase();
    }


    //구매 시 redis데이터 갱신
    //분산락
    @CachePut(cacheNames="stockCache",key="args[0]")
    @CacheEvict(cacheNames="stockCache",allEntries = true)
    public StockDto updateStockWithRedisson(long id) {
        log.info("분산락 시작");
        Stock stock = stockRepository.findById(id).orElseThrow();
        log.info("stock:" + stock.getStock());
        String lockName = "STOCK" + id;
        RLock rLock = redissonClient.getLock(lockName);
        final String threadName = Thread.currentThread().getName();
        log.info("threadName:" + threadName);

        long waitTime =5L;
        long leaseTime=3L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        try {
            boolean available = rLock.tryLock(waitTime, leaseTime, timeUnit);
            log.info("점유 허용 확인 : " + available);
            if (!available) {
                log.info("lock 점유 실패");
            }
            log.info("락획득 성공");
            stock.increase();
            stockRepository.save(stock);
        } catch (InterruptedException e) {
            log.info("락을 얻으려고 시도하다가 인터럽트를 받음");
        }finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        return new StockDto(stock.getId(),stock.getStock());
    }

    public Stock getStock(Long id) {
        return  stockRepository.findById(id).get();
    }

    // 상품을 읽으면 redis에 적재
    @Cacheable(cacheNames = "stockCache", key = "args[0]")
    public StockDto readStock(Long id) {
        Stock stock = stockRepository.findById(id).get();
        return new StockDto(stock.getId(), stock.getStock());
    }

    public void saveStock(Integer n) {
        stockRepository.save(Stock.builder().stock(n).build());
    }

    @Cacheable(cacheNames = "stockAllCache", key = "methodName")
    public List<StockDto> readAllStock() {
        return stockRepository.findAll()
            .stream()
            .map(m -> new StockDto(m.getId(), m.getStock()))
            .toList();
    }




}
