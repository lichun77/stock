package com.lic.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lic.stock.domain.BreakingPointPO;

public interface BreakingPointRepository extends JpaRepository<BreakingPointPO, Integer> {
    
}
