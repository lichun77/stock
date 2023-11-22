package com.lic.stock.repository;

import com.lic.stock.domain.StatisticsAfterBpPO;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StatisticsAfterBpRepository extends JpaRepository<StatisticsAfterBpPO, Integer> {

    @Modifying
    @Query(value = "truncate table statistics_after_bp ", nativeQuery = true)
    void trancate();
    
}
