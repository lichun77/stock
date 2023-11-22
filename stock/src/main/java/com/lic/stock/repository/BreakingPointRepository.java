package com.lic.stock.repository;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lic.stock.domain.BreakingPointPO;

public interface BreakingPointRepository extends JpaRepository<BreakingPointPO, Long> {

    @Query(value = "select range_name as rangeName, count(*) as count,  avg(change_rate_bp) as avgChangeRateBp " +
            "from stock.breaking_point bp " +
            "left join stock.change_rate_range crr on bp.change_rate_one_month between crr.low_rate and crr.high_rate " +
            "where bp.change_rate_one_month is not null and bp.strategy_profile_code = ? " +
            "group by range_name",
            nativeQuery = true)
    List<Map<String,Object>> findChangeRateStatisticsOneMonth(String strategyProfileCode);

    @Query(value = "select range_name as rangeName, count(*) as count,  avg(change_rate_bp) as avgChangeRateBp " +
            "from stock.breaking_point bp " +
            "left join stock.change_rate_range crr on bp.change_rate_one_day between crr.low_rate and crr.high_rate " +
            "where bp.change_rate_one_day is not null and bp.strategy_profile_code = ? " +
            "group by range_name",
            nativeQuery = true)
    List<Map<String,Object>> findChangeRateStatisticsOneDay(String strategyProfileCode);

     @Query(value = "select range_name as rangeName, count(*) as count,  avg(change_rate_bp) as avgChangeRateBp " +
            "from stock.breaking_point bp " +
            "left join stock.change_rate_range crr on bp.change_rate_one_cycle between crr.low_rate and crr.high_rate " +
            "where bp.change_rate_one_cycle is not null and bp.strategy_profile_code = ? " +
            "group by range_name",
            nativeQuery = true)
    List<Map<String,Object>> findChangeRateStatisticsOneCycle(String strategyProfileCode);
   
    @Transactional
    @Modifying
    @Query(value = "truncate table breaking_point ", nativeQuery = true)
    void trancate();
    
}
