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



        
        importService.importHistoryData();

        System.out.println("***********importHistoryData success*********");
    }
    
}
