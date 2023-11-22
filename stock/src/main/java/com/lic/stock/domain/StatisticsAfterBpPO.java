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

@Entity(name="statistics_after_bp")
@Data
public class StatisticsAfterBpPO implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String rangeName;

    @Column
    private String strategyProfileCode;

    @Column
    private Integer count;

    @Column
    private BigDecimal proportion;

    @Column
    private BigDecimal avgChangeRateBp;

    @Column
    private String period;

    @Column
    private Date createDate;
}
