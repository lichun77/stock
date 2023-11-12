package com.lic.stock.business;

import java.util.List;
import com.lic.stock.domain.TradeDayPO;
import lombok.extern.slf4j.Slf4j;
import com.lic.stock.domain.BreakingPointPO;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 周涨幅 > 15%
 * 分析平台期 24周
 * 周成交量 > 平台期周均成交量*5
 * 周成交量 > 平台期最高成交量*2
 */
@Slf4j
public class BreakingPointUtil {

    
    /**
     * 
     * @param tradeDayList 交易日列表，按日期正序
     * @return
     */
    public static List<BreakingPointPO>  findBreakingPoint(List<TradeDayPO> tradeDayList) throws Exception{

        List<BreakingPointPO> resultlist = new ArrayList<>();

        List<TradeWeek>  weekList = getWeekList(tradeDayList);
        

        for( int i = 0; i < weekList.size(); i++){

            if( i < 24 ){
                //前24周不分析
                continue;
            }

            TradeWeek week = weekList.get(i);
            if( week.getChangeRate().compareTo(new java.math.BigDecimal("0.15")) < 0 ){
                //周涨幅 < 15% 
                continue;
            }
            
            log.info("周涨幅 > 15%:{}", week.getStartDate());
            //分析平台期 24周
            int startWeekIndex = i - 24;
            int endWeekIndex = i - 1;

            TradeWeek startWeek = weekList.get(startWeekIndex);
            TradeWeek endWeek = weekList.get(endWeekIndex);
                //周成交量 > 平台期周均成交量*5
            BigDecimal avgTradeVolume = new BigDecimal("0");
            BigDecimal maxTradeVolume = new BigDecimal("0");
            for( int j = startWeekIndex; j <= endWeekIndex; j++){
                TradeWeek tmpWeek = weekList.get(j);
                if( tmpWeek.getTradeVolume().compareTo(maxTradeVolume) > 0 ){
                    maxTradeVolume = tmpWeek.getTradeVolume();
                }
                avgTradeVolume = avgTradeVolume.add(tmpWeek.getTradeVolume());
            }
            avgTradeVolume = avgTradeVolume.divide(new java.math.BigDecimal("24"), 2, BigDecimal.ROUND_HALF_UP);

            if( week.getTradeVolume().compareTo(avgTradeVolume.multiply(new BigDecimal("5"))) < 0 ){
                continue;
            }
            log.info("周成交量 > 平台期周均成交量*5:{}", week.getStartDate());

            //周成交量 > 平台期最高成交量*2
            if( week.getTradeVolume().compareTo(maxTradeVolume.multiply(new BigDecimal("2"))) < 0 ){
                continue;
            }
                    
            log.info("周成交量 > 平台期最高成交量*2:{}", week);

            BreakingPointPO breakingPoint = new BreakingPointPO();
            breakingPoint.setSymbol(week.getSymbol());
            breakingPoint.setTradeDate(week.getStartDate());
            breakingPoint.setAnalyzDate(new Date());

            resultlist.add(breakingPoint);
        }

        return resultlist;
    }

    private static List<TradeWeek>  getWeekList(List<TradeDayPO> tradeDayList) throws Exception {
        List<TradeWeek> tradeWeekList = new ArrayList<>();

        TradeWeek tradeWeek = new TradeWeek();
        

        int skipDays = tradeDayList.size() % 5;
        int dayOfWeek = 1;

        for( int i = 0; i < tradeDayList.size(); i++){
            if( i < skipDays ){
                continue;
            }
            TradeDayPO tradeDay = tradeDayList.get(i);
            if( dayOfWeek == 1){
                tradeWeek = new TradeWeek();
                tradeWeek.addFirstDay(tradeDay);
            }else{
                tradeWeek.addDay(tradeDay);
            }

            if( dayOfWeek == 5){
                tradeWeekList.add(tradeWeek);
                dayOfWeek = 1;
            }else{
                dayOfWeek++;
            }
        }
        // for( int i = 0; i < tradeWeekList.size(); i++){
        //     TradeWeek week = tradeWeekList.get(i);
        //     System.out.println(week);
        // }
        return tradeWeekList;
    }


}
