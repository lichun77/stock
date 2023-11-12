package com.lic.stock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lic.stock.business.BreakingPointUtil;
import com.lic.stock.domain.BreakingPointPO;
import com.lic.stock.domain.TradeDayPO;
import com.lic.stock.repository.BreakingPointRepository;
import com.lic.stock.repository.TradeDayRepository;


@Service
public class AnalyzService {
    
    @Autowired
    private TradeDayRepository tradeDayRepository;

    @Autowired
    private BreakingPointRepository breakingPointRepository;

    public List<BreakingPointPO>  findBreakingPoint() throws Exception{


       List<TradeDayPO> days = tradeDayRepository.findBySymbolOrderByTradeDate("300459.SZ");
       
        List<BreakingPointPO> bpList =  BreakingPointUtil.findBreakingPoint(days);

        breakingPointRepository.saveAll(bpList);

        return bpList;
    }
}
