package com.lic.stock.service;

import java.io.File;

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

    @Test
    public void importSymbol(){

        importService.importSymbol();

        System.out.println("***********importSymbol success*********");
    }
    
    @Test
    public void importOneDay(){

        importService.importOneDay("2023-11-21");

        System.out.println("***********importOneDay success*********");
    }

    @Test
    public void testZip(){

        importService.unzipAndRead(new File("/Users/lichun/Documents/投资/历史行情数据/a股日线增量/2023-11-10.zip"));

        System.out.println("***********testZip success*********");
    }
}
