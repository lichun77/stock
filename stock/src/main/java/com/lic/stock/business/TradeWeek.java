package com.lic.stock.business;

import java.math.BigDecimal;

import javax.persistence.Column;

import com.alibaba.fastjson.JSON;
import com.lic.stock.domain.TradeDayPO;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TradeWeek {
    
    private String startDate;

    private String endDate;

    private String symbol;

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private BigDecimal lastClosePrice;

    private BigDecimal changeAmount;

    private BigDecimal changeRate;

    /**成交量(手) */
    private BigDecimal tradeAmount;

    /**成交额(千元) */
    private BigDecimal tradeVolume;


    public void addFirstDay(TradeDayPO day){

            //加入第一天
            this.startDate = day.getTradeDate();
            this.endDate = day.getTradeDate();
            this.symbol = day.getSymbol();
            this.openPrice = day.getOpenPrice();
            this.closePrice = day.getClosePrice();
            this.highestPrice = day.getHighestPrice();
            this.lowestPrice = day.getLowestPrice();
            this.lastClosePrice = day.getLastClosePrice();
            this.changeAmount = day.getChangeAmount();
            this.changeRate = day.getChangeRate();
            this.tradeAmount = day.getTradeAmount();
            this.tradeVolume = day.getTradeVolume();

    }

    //顺序加入后续天
    public void addDay(TradeDayPO day) throws Exception{

        if (!day.getSymbol().equals(this.symbol)) {
            throw new RuntimeException("加入的交易日不属于同一股票weekSymbol = "+this.symbol + "daySymbol="+day.getSymbol());
        }

        if( day.getTradeDate().compareTo(this.endDate) < 0 ){
            String errMsg = "加入的交易日不是week的后续交易日weekEndDate = "+this.endDate + "dayDate="+day.getTradeDate();
            log.error(errMsg);
            throw new Exception(errMsg);
        }

        this.endDate = day.getTradeDate();
        this.closePrice = day.getClosePrice();
        this.highestPrice = day.getHighestPrice().compareTo(highestPrice) > 0 ? day.getHighestPrice() : highestPrice;
        this.lowestPrice = day.getLowestPrice().compareTo(lowestPrice) < 0 ? day.getLowestPrice() : lowestPrice;
        this.changeAmount = day.getClosePrice().subtract( this.getLastClosePrice()  );
        changeRate = this.changeAmount.divide( this.getLastClosePrice(), 4, BigDecimal.ROUND_HALF_UP );
        tradeAmount = this.tradeAmount.add( day.getTradeAmount() );
        tradeVolume = this.tradeVolume.add( day.getTradeVolume() );

    }

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
