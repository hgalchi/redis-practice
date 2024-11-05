package com.spartacoding.msa.eurekaclient.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Facade {

    private final StockService stockService;
    private final RedissonClient redissonClient;

    public void update(Long id) {
        log.info("분산락 시작");
        String lockName = "STOCK" + id;
        RLock rLock = redissonClient.getLock(lockName);
        final String threadName = Thread.currentThread().getName();

        long waitTime =5L;
        long leaseTime=3L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        try {
            boolean available = rLock.tryLock(waitTime, leaseTime, timeUnit);
            log.info("점유 허용 확인 : " + available);
            if (!available) {
                log.info("lock 점유 실패");
            }
            log.info(threadName+"락획득 성공");
            stockService.updateStock(id);
        } catch (InterruptedException e) {
            log.info("락을 얻으려고 시도하다가 인터럽트를 받음");
        }finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
    }


}
