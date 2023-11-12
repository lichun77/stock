package com.lic.stock.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lic.stock.domain.BreakingPointPO;

@SpringBootTest
public class AnalyzServiceIT {
    
    @Autowired
    private AnalyzService analyzService;

    @Test
    public void findBreakingPoint(){

       try {
        List<BreakingPointPO> bpList = analyzService.findBreakingPoint();

        for(BreakingPointPO bp : bpList){
            System.out.println(bp);
        }

    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    }
}
