package com.lic.stock.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity(name="trade_day")
@Data
public class TradeDayPO implements Serializable{
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String symbol;

    @Column
    private String tradeDate;

    @Column( precision = 10, scale = 2)
    private BigDecimal openPrice;

    @Column( precision = 10, scale = 2)
    private BigDecimal closePrice;

    @Column( precision = 10, scale = 2)
    private BigDecimal highestPrice;

    @Column( precision = 10, scale = 2)
    private BigDecimal lowestPrice;

    @Column( precision = 10, scale = 2)
    private BigDecimal lastClosePrice;

    @Column( precision = 10, scale = 2)
    private BigDecimal changeAmount;

    @Column( precision = 10, scale = 4)
    private BigDecimal changeRate;

    /**成交量(手) */
    @Column( precision = 12, scale = 2)
    private BigDecimal tradeVolume;

     /**成交额(千元) */
    @Column( precision = 12, scale = 4)
    private BigDecimal tradeAmount;

    @Column
    private Date importDate;

}
