package com.lic.stock.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;



@Entity(name="strategy_profile")
@Data
public class StrategyProfilePO implements Serializable{


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String strategyCode;

    @Column
    private String profileCode;

    @Column
    private Integer daysOfCycle;

    @Column
    private String changeRateOfCycle;

    @Column
    private Integer cycleOfPlatform;

    @Column
    private Integer mutipleOfAvgVolume;

    @Column
    private Integer multipleOfHighestVolume;
    
}
