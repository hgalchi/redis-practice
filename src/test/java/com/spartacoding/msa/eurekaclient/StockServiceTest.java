package com.spartacoding.msa.eurekaclient;

import com.spartacoding.msa.eurekaclient.domain.dto.StockDto;
import com.spartacoding.msa.eurekaclient.repository.StockRepository;
import com.spartacoding.msa.eurekaclient.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.redisson.api.RedissonClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@EnableCaching
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RedissonClient redissonClient;


    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        stockRepository.deleteAll();
    }

    @Test
    void testStockFlow() {
        // Step 1: stock 생성
        stockService.saveStock(10);
        stockService.saveStock(20);

        // Step 2: 모든 stock 확인
        List<StockDto> allStocks = stockService.readAllStock();
        assertThat(allStocks.size()).isEqualTo(2);
        assertThat(allStocks.get(0).stock()).isEqualTo(10);
        assertThat(allStocks.get(1).stock()).isEqualTo(20);
        System.out.println("readAllStock-");
        allStocks.forEach(System.out::println);

        // Step 3: 단일 stock 확인 (Cache 미적중, DB에서 조회)
        StockDto stock1 = stockService.readStock(1L);
        assertThat(stock1.stock()).isEqualTo(10);
        System.out.println("readS tock-"+stock1);


        // Step 4: 구매 시 redis 데이터 갱신 (분산락)
        StockDto updatedStock = stockService.updateStockWithRedisson(1L);
        assertThat(updatedStock.stock()).isEqualTo(9);

        // Step 5: 단일 stock 확인 (Cache 적중)
        StockDto cachedStock = stockService.readStock(1L);
        assertThat(cachedStock.stock()).isEqualTo(9);
    }
}
