package com.lic.stock.business;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lic.stock.domain.TradeDayPO;
import com.lic.stock.repository.TradeDayRepository;
import com.lic.stock.domain.BreakingPointPO;
import com.lic.stock.domain.StrategyProfilePO;

import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 周涨幅 > 15%
 * 分析平台期 24周
 * 周成交量 > 平台期周均成交量*5
 * 周成交量 > 平台期最高成交量*2
 */
//@Slf4j
@Service
public class BreakingPointUtil {

    @Autowired
    private TradeDayRepository tradeDayRepository;

    // /**BP最小周涨幅 */
    // private static String WEEK_CHANGE_RATE = "0.15";
    // /**平台观察期 单位周 */
    // private static String PLATFORM_LENGTH = "24";

    // private static String MUTIPLE_OF_AVG_VOLUME = "5";

    // private static String MUTIPLE_OF_HIGH_VOLUME = "2";

    private static Integer TRADE_DAYS_OF_MONTH = 20;
    
    /**
     * 
     * @param tradeDayList 交易日列表，按日期正序
     * @return
     */
    public  List<BreakingPointPO>  findBreakingPointFromHistory(StrategyProfilePO strateyProfile,List<TradeDayPO> tradeDayList) throws Exception{

        List<BreakingPointPO> resultlist = new ArrayList<>();

        List<TradeCycle>  cycleList = getCycleList(tradeDayList, strateyProfile);
        

        for( int i = 0; i < cycleList.size(); i++){

            if( i < strateyProfile.getCycleOfPlatform() ){
                //平台期不分析
                continue;
            }

            if( isBreakingPoint(cycleList, i, strateyProfile) ){
                resultlist.add(buildBreakingPoint(cycleList,i,strateyProfile));
            }
        }

        return resultlist;
    }

    /**
     * 
     * @param weekList 交易周列表，按日期正序,最后一个元素为要分析的BP周，前面的元素为平台期
     * @param indexOfBp
     * @return
     */
    private  boolean isBreakingPoint(List<TradeCycle> weekList, int indexOfBp,StrategyProfilePO strateyProfile){  

        TradeCycle week = weekList.get(indexOfBp);

        if( week.getChangeRate().compareTo(new BigDecimal(strateyProfile.getChangeRateOfCycle())) < 0 ){
                return false;
        }
            
            //log.info("周涨幅 > 15%:{}", week.getStartDate());
            //分析平台期 24周
            int startWeekOfPlatform = indexOfBp - strateyProfile.getCycleOfPlatform();
            int endWeekOfPlatform = indexOfBp - 1;

            //周成交量 > 平台期周均成交量*5
            BigDecimal avgTradeVolumeOfPlatform = new BigDecimal("0");
            BigDecimal maxTradeVolumeOfPlatform = new BigDecimal("0");
            for( int j = startWeekOfPlatform; j <= endWeekOfPlatform; j++){
                TradeCycle tmpWeek = weekList.get(j);
                if( tmpWeek.getTradeVolume().compareTo(maxTradeVolumeOfPlatform) > 0 ){
                    maxTradeVolumeOfPlatform = tmpWeek.getTradeVolume();
                }
                avgTradeVolumeOfPlatform = avgTradeVolumeOfPlatform.add(tmpWeek.getTradeVolume());
            }
            avgTradeVolumeOfPlatform = avgTradeVolumeOfPlatform.divide(new BigDecimal( strateyProfile.getCycleOfPlatform()), 2, BigDecimal.ROUND_HALF_UP);

            if( week.getTradeVolume().compareTo(avgTradeVolumeOfPlatform.multiply(new BigDecimal(strateyProfile.getMutipleOfAvgVolume()))) < 0 ){
                return false;
            }
            //log.info("周成交量 > 平台期周均成交量*5:{}", week.getStartDate());

            //周成交量 > 平台期最高成交量*2
            if( week.getTradeVolume().compareTo(maxTradeVolumeOfPlatform.multiply(new BigDecimal(strateyProfile.getMultipleOfHighestVolume()))) < 0 ){
                return false;
            }

        return true;
    }

    private  BreakingPointPO buildBreakingPoint(List<TradeCycle> cycleList, int bpIndex, StrategyProfilePO strategyProfile) {

        TradeCycle bpWeek = cycleList.get(bpIndex);

        BreakingPointPO breakingPoint = new BreakingPointPO();
        breakingPoint.setSymbol(bpWeek.getSymbol());
        breakingPoint.setTradeDate(bpWeek.getEndDate());
        breakingPoint.setAnalyzDate(new Date());
        breakingPoint.setChangeRateBp(bpWeek.getChangeRate());
        breakingPoint.setStrategyProfileCode(strategyProfile.getProfileCode());

        BigDecimal high[] = new BigDecimal[4];
        for( int i = 0; i < 4; i++){
            high[i] = new BigDecimal("0");
        }
        
        //计算BP后1个月，2个月，3个月，4个月的最高价
        int month = 0;
        List <TradeCycle> cyclesOfaMonth = new ArrayList<>();
        for (int i = bpIndex + 1; i < cycleList.size(); i++) {
            cyclesOfaMonth.add(cycleList.get(i));
            if (cyclesOfaMonth.size() * strategyProfile.getDaysOfCycle() >= TRADE_DAYS_OF_MONTH) {
                high[month] = new BigDecimal(
                        cyclesOfaMonth.stream()
                                .mapToDouble(a -> a.getHighestPrice().doubleValue())
                                .max()
                                .orElse(0)
                ).setScale(2, BigDecimal.ROUND_HALF_UP);
                cyclesOfaMonth.clear();
                month++;
                if (month >= 4) {
                    break;
                }
            }
        }

        for( int i = 0;i < 4;i++){
            if( high[i].compareTo(new BigDecimal("0")) == 0 ){
                break;
            }   

            BigDecimal changeRate = high[i].subtract(bpWeek.getClosePrice()).divide(bpWeek.getClosePrice(), 4, BigDecimal.ROUND_HALF_UP);
            switch(i){
                case 0:
                    breakingPoint.setChangeRateOneMonth(changeRate);
                    break;
                case 1:
                    breakingPoint.setChangeRateTwoMonth(changeRate);
                    break;
                case 2:
                    breakingPoint.setChangeRateThreeMonth(changeRate);
                    break;
                case 3:
                    breakingPoint.setChangeRateFourMonth(changeRate);
                    break;
            }
        }

        int nextCycleIndex = bpIndex + 1;
        if( nextCycleIndex < cycleList.size() ){
            TradeCycle nextCycle = cycleList.get(nextCycleIndex);
            TradeDayPO nextDay = tradeDayRepository.findBySymbolAndTradeDate( nextCycle.getSymbol(),nextCycle.getStartDate());
            breakingPoint.setChangeRateOneDay(nextDay.getChangeRate());
            breakingPoint.setChangeRateOneCycle(nextCycle.getChangeRate());
        }

        return breakingPoint;
    }

    private  List<TradeCycle>  getCycleList(List<TradeDayPO> tradeDayList, StrategyProfilePO strateyProfile) throws Exception {
        List<TradeCycle> tradeCycleList = new ArrayList<>();

        TradeCycle tradeCycle = new TradeCycle();
        int skipDays = tradeDayList.size() % strateyProfile.getDaysOfCycle();
        int dayOfCycle = 1;

        for( int i = 0; i < tradeDayList.size(); i++){
            if( i < skipDays ){
                continue;
            }
            TradeDayPO tradeDay = tradeDayList.get(i);
            if( dayOfCycle == 1){
                tradeCycle = new TradeCycle();
                tradeCycle.addFirstDay(tradeDay);
            }else{
                tradeCycle.addDay(tradeDay);
            }

            if( dayOfCycle == strateyProfile.getDaysOfCycle()){
                tradeCycleList.add(tradeCycle);
                dayOfCycle = 1;
            }else{
                dayOfCycle++;
            }
        }

        return tradeCycleList;
    }


}
