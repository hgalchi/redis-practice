package com.spartacoding.msa.eurekaclient.repository;

import com.spartacoding.msa.eurekaclient.domain.Stock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id=:id")
    Optional<Stock> findByIdPerssimisticLock(@Param("id") Long id);

}
