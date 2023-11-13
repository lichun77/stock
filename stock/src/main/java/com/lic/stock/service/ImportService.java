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

import com.lic.stock.repository.StockInfoRepository;
import com.lic.stock.repository.TradeDayRepository;

import lombok.extern.slf4j.Slf4j;

import com.lic.stock.domain.TradeDayPO;

@Slf4j
@Service
public class ImportService {

    private static String directoryPath = "/Users/lichun/Documents/投资/历史行情数据/a股日线/";

    private int fileCount = 0;
    private int finishCount = 0;

    @Autowired
    TradeDayRepository tradeDayRepository;

    @Autowired
    StockInfoRepository stockInfRepository;

    public void importSymbol(){
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
        
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(filename->{
                    String symbol = filename.getFileName().toString().substring(0, 9);
                    String market = filename.getFileName().toString().substring(7, 9);
                    StockInfo stockInfo = new StockInfo();
                    stockInfo.setSymbol(symbol);
                    stockInfo.setMarket(market);
                    stockInfRepository.save(symbol);
                }
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("打开文件失败"+e.getMessage());
        }
    }
    
    public void importHistoryData(){

        try {
            fileCount = (int)Files.list(Paths.get(directoryPath)).count();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
            int lineCount = 0;
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
                if(fields.length  > 10){
                    tradeDay.setTradeAmount(getAmountBigDecimal(fields[10]));
                }
                tradeDay.setImportDate(new Date());

                tradeDayRepository.save(tradeDay);
                lineCount++;
            }

            log.info("导入文件完成[{}/{}]{},记录数 {}",  
                ++finishCount, fileCount, filePath.getFileName(), lineCount);

        } catch (IOException e) {
            e.printStackTrace();
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
