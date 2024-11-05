package com.spartacoding.msa.eurekaclient;

import static org.junit.jupiter.api.Assertions.*;
import com.spartacoding.msa.eurekaclient.domain.Stock;
import com.spartacoding.msa.eurekaclient.repository.StockRepository;
import com.spartacoding.msa.eurekaclient.service.Facade;
import com.spartacoding.msa.eurekaclient.service.StockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Slf4j
class LockTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private Facade f;

    private Long STOCK_ID = 1L;
    private final Integer STOCK_COUNT = 100;

    /*@BeforeEach
    public void before() {
        log.info("재고 100개로 생성");
        Stock stock=Stock.builder().stock(STOCK_COUNT).build();
        Stock createStock = stockRepository.save(stock);
        STOCK_ID = createStock.getId();
    }*/

    @Test
    public void 동시에_100개_요청() throws InterruptedException {
        int threadCount = 100;
        //동시에 여러 스레드를 실행 32개의 스레드를 고정적으로 사용하여 100개의 요청을 처리한다.
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.updateStockWithLock(1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockService.getStock(1L);
        assertEquals(0, stock.getStock());
    }

    @Test
    public void 분산락을_사용해_동시에_100개_요청처리() throws InterruptedException {
        int threadCount = 100;
        //동시에 여러 스레드를 실행 32개의 스레드를 고정적으로 사용하여 100개의 요청을 처리한다.
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    f.update(STOCK_ID);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockService.getStock(STOCK_ID);
        assertEquals(0, stock.getStock());
    }

}