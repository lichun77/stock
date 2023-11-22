package com.lic.stock.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.lic.stock.business.BreakingPointUtil;
import com.lic.stock.domain.BreakingPointPO;
import com.lic.stock.domain.StockInfoPO;
import com.lic.stock.domain.StrategyProfilePO;
import com.lic.stock.domain.TradeDayPO;
import com.lic.stock.repository.BreakingPointRepository;
import com.lic.stock.repository.TradeDayRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BreakingPointService {

    public static CountDownLatch countDownLatch;

    private static final String ANALYZE_START_DATE = "20210101";

    @Autowired
    private TradeDayRepository tradeDayRepository;
    @Autowired
    private BreakingPointRepository breakingPointRepository;

    @Autowired
    private BreakingPointUtil breakingPointUtil;

    public void clearAll() {
        breakingPointRepository.trancate();
    }

    @Async("findBPExecutor")
    public void findBreakingPoint(StockInfoPO stock, StrategyProfilePO strateyProfile, Integer total) throws Exception {

        Instant start = Instant.now();
        List<TradeDayPO> days = tradeDayRepository
                .findBySymbolAndTradeDateGreaterThanOrderByTradeDate(stock.getSymbol(), ANALYZE_START_DATE);
        System.out.println("查询耗时：" + (Instant.now().toEpochMilli() - start.toEpochMilli()));

        List<BreakingPointPO> bpList = breakingPointUtil.findBreakingPointFromHistory(strateyProfile, days);
        try {
            if (bpList.size() > 0) {
                breakingPointRepository.saveAll(bpList);
            }
        } catch (Exception e) {
            log.error("插入breaking point失败", e);
            for (BreakingPointPO bp : bpList) {
                log.error("{}", bp);
            }
        }

        synchronized (this) {
            log.info("分析完成[{}/{}]{},BP数 {}",
                    countDownLatch.getCount(), total, stock.getSymbol(), bpList.size());
        }
        
        countDownLatch.countDown();
    }
}
