package com.lic.stock.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lic.stock.domain.TradeDayPO;

public interface TradeDayRepository extends JpaRepository<TradeDayPO, Long> {
 
    public List<TradeDayPO> findBySymbolAndTradeDateBetween(String symbol,Date startDate, Date endDate);

    public List<TradeDayPO> findBySymbolOrderByTradeDate(String symbol);
}
