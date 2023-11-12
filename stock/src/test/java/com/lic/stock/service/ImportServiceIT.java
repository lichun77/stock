package com.lic.stock.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImportServiceIT {

    @Autowired
    private ImportService importService;

    @Test
    public void importHistoryData(){


        String s1 = "20200101";
        String s2 = "20200102";

        System.out.println();
        System.out.println();
        System.out.println(s2.compareTo(s1));

        importService.importHistoryData();

        System.out.println("***********importHistoryData success*********");
    }
    
}
