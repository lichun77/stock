package com.lic.stock.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AnalyzServiceIT {
    
    @Autowired
    private AnalyzService analyzService;

    @Test
    public void findBreakingPoint(){

       try {
            analyzService.findBreakingPoint();

        } catch (Exception e) {
            // 
            e.printStackTrace();
        }
    }

    @Test
    public void calcSatatistics(){

        try {
            analyzService.calcSatatistics();

        } catch (Exception e) {
            // 
            e.printStackTrace();
        }
    }
}
