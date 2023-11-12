package com.lic.stock.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Date;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lic.stock.repository.TradeDayRepository;
import com.lic.stock.domain.TradeDayPO;

@Service
public class ImportService {

    private static String directoryPath = "/Users/lichun/Documents/投资/历史行情数据/oneday/";

    @Autowired
    TradeDayRepository tradeDayRepository;
    
    public void importHistoryData(){

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
        paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(this::processFile);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("打开文件失败"+e.getMessage());
        }

    }

    private void processFile(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath,Charset.forName("GB18030"))) {
            // Skip the title line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                //证券代码,交易时间,开盘价,最高价,最低价,收盘价,前收盘价,涨跌额,涨跌幅,成交量(手),成交额(千元)
                TradeDayPO tradeDay = new TradeDayPO();
                
                tradeDay.setSymbol(fields[0]);
                tradeDay.setTradeDate(fields[1]);
                tradeDay.setOpenPrice(getPriceBigDecimal(fields[2]));
                tradeDay.setHighestPrice(getPriceBigDecimal(fields[3]));
                tradeDay.setLowestPrice(getPriceBigDecimal(fields[4]));
                tradeDay.setClosePrice(getPriceBigDecimal(fields[5]));
                tradeDay.setLastClosePrice(getPriceBigDecimal(fields[6]));
                tradeDay.setChangeAmount(getPriceBigDecimal(fields[7]));
                tradeDay.setChangeRate(getPctBigDecimal(fields[8]));
                tradeDay.setTradeVolume(getVolumeBigDecimal(fields[9]));
                tradeDay.setTradeAmount(getAmountBigDecimal(fields[10]));
                tradeDay.setImportDate(new Date());

                System.out.println(tradeDay.getTradeDate());

                tradeDayRepository.save(tradeDay);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BigDecimal getPriceBigDecimal(String value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getPctBigDecimal(String value){
        return new BigDecimal(value).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getVolumeBigDecimal(String value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getAmountBigDecimal(String value){
        return new BigDecimal(value).setScale(3, BigDecimal.ROUND_HALF_UP);
    }
}
