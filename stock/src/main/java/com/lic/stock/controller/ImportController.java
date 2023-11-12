package com.lic.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lic.stock.service.ImportService;

@RequestMapping("/import")
@Controller
public class ImportController {

    @Autowired
    private ImportService importService;
    
    @PostMapping("/history")
    public String importHistoryData(){

        importService.importHistoryData();

        return "importHistoryData success";
    }
}
