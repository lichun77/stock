package com.lic.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lic.stock.domain.StockInfoPO;

public interface StockInfoRepository extends JpaRepository<StockInfoPO, Long> {
    
}
