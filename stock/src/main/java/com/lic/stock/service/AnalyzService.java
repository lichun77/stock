package com.lic.stock.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lic.stock.domain.StatisticsAfterBpPO;
import com.lic.stock.domain.StockInfoPO;
import com.lic.stock.domain.StrategyProfilePO;
import com.lic.stock.repository.BreakingPointRepository;
import com.lic.stock.repository.StatisticsAfterBpRepository;
import com.lic.stock.repository.StockInfoRepository;
import com.lic.stock.repository.StrategyProfileRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalyzService {

    @Autowired
    private BreakingPointRepository breakingPointRepository;

    @Autowired
    private BreakingPointService breakingPointService;

    @Autowired
    private StockInfoRepository stockInfoRepository;

    @Autowired
    private StrategyProfileRepository strategyProfileRepository;

    @Autowired
    private StatisticsAfterBpRepository statisticsAfterBpRepository;

    public void findBreakingPoint() throws Exception {

        breakingPointService.clearAll();

        List<StockInfoPO> stocks = stockInfoRepository.findAll();

        List<StrategyProfilePO> strategyProfiles = strategyProfileRepository.findAll();

        BreakingPointService.countDownLatch = new CountDownLatch(stocks.size() * strategyProfiles.size());

        Instant start = Instant.now();
        for (StrategyProfilePO strategyProfile : strategyProfiles) {

            for (StockInfoPO stock : stocks) {
                breakingPointService.findBreakingPoint(stock, strategyProfile, stocks.size());
            }

            //log.info("{}分析完成",strategyProfile.getProfileCode());
        }

        BreakingPointService.countDownLatch.await();
        log.info("所有分析完成,总耗时：{}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
    }

    @Transactional
    public void calcSatatistics() throws Exception {

        statisticsAfterBpRepository.trancate();

        List<StrategyProfilePO> strategyProfiles = strategyProfileRepository.findAll();

        for (StrategyProfilePO strateyProfile : strategyProfiles) {
            List<Map<String, Object>> result = breakingPointRepository
                    .findChangeRateStatisticsOneMonth(strateyProfile.getProfileCode());
            saveStatisticsResult(result, strateyProfile.getProfileCode(), "1m");
            result = breakingPointRepository
                    .findChangeRateStatisticsOneDay(strateyProfile.getProfileCode());
            saveStatisticsResult(result, strateyProfile.getProfileCode(), "1d");
            result = breakingPointRepository
                    .findChangeRateStatisticsOneCycle(strateyProfile.getProfileCode());
            saveStatisticsResult(result, strateyProfile.getProfileCode(), "1c");
        }

    }

    private void saveStatisticsResult(List<Map<String, Object>> result, String strategyProfileCode, String period) {

        int sum = result.stream().mapToInt(r -> ((BigInteger) r.get("count")).intValue())
                .sum();

        for (Map<String, Object> r : result) {
            StatisticsAfterBpPO po = convertStatistics(r, strategyProfileCode, period, sum);
            statisticsAfterBpRepository.save(po);
        }

    }

    private StatisticsAfterBpPO convertStatistics(Map<String, Object> map, String strategyProfileCode, String period,
            int sum) {

        StatisticsAfterBpPO po = new StatisticsAfterBpPO();

        po.setRangeName((String) map.get("rangeName"));
        po.setStrategyProfileCode(strategyProfileCode);
        po.setCount(((BigInteger) map.get("count")).intValue());
        po.setAvgChangeRateBp(((BigDecimal) map.get("avgChangeRateBp")).setScale(4, java.math.RoundingMode.HALF_UP));
        po.setPeriod(period);
        po.setCreateDate(new Date());
        po.setProportion(
                new BigDecimal((BigInteger) map.get("count")).divide(
                    new BigDecimal(sum), 4,java.math.RoundingMode.HALF_UP));

        return po;
    }
}
