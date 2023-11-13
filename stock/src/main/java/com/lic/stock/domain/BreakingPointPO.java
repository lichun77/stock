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

@Entity(name="breaking_point")
@Data
public class BreakingPointPO implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String symbol;

    @Column
    private String tradeDate;

    @Column
    private Date analyzDate;

    @Column 
    BigDecimal changeRateOneMonth;

    @Column 
    BigDecimal changeRateTwoMonth;

    @Column 
    BigDecimal changeRateThreeMonth;

    @Column 
    BigDecimal changeRateFourMonth;

    @Override
    public String toString(){
        return symbol + " " + tradeDate + " " + analyzDate + 
            " " + changeRateOneMonth + " " + changeRateTwoMonth + 
            " " + changeRateThreeMonth + " " + changeRateFourMonth;
    }
    
}
