package com.lic.stock.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Date;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.stream.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.lic.stock.repository.StockInfoRepository;
import com.lic.stock.repository.TradeDayRepository;

import lombok.extern.slf4j.Slf4j;

import com.lic.stock.domain.StockInfoPO;
import com.lic.stock.domain.TradeDayPO;

@Slf4j
@Service
public class ImportService {

    private static String directoryPath = "/UsersXXXXXXX/lichun/Documents/投资/历史行情数据/a股日线/";

    private int fileCount = 0;
    private int finishCount = 0;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TradeDayRepository tradeDayRepository;

    @Autowired
    StockInfoRepository stockInfoRepository;

    public void importSymbol() {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {

            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .forEach(filename -> {

                        String symbol = filename.getFileName().toString().substring(0, 9);
                        String market = filename.getFileName().toString().substring(7, 9);

                        StockInfoPO stockInfo = new StockInfoPO();
                        stockInfo.setSymbol(symbol);
                        stockInfo.setMarket(market);

                        stockInfoRepository.save(stockInfo);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("打开文件失败" + e.getMessage());
        }
    }

    public void importOneDay(String date) {
        String url = "http://www.juejinshuju.com/update?usr=lichun&pwd=123456&date=" + date + "&file=a股日线";

        log.info("开始下载{}", url);
        restTemplate.execute(url, HttpMethod.GET, null, new ResponseExtractor<Void>() {
            @Override
            public Void extractData(ClientHttpResponse response) throws IOException {
                File tempZip = File.createTempFile("temp", ".zip");
                FileCopyUtils.copy(response.getBody(), new FileOutputStream(tempZip));
                log.info("下载完成{}", tempZip.getAbsolutePath());
                unzipAndRead(tempZip);
                return null;
            }
        });

        log.info("导入{}数据完成", date);

    }

    public void unzipAndRead(File zipFile) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".csv")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                   int lineCount = importTradeData(reader);
                   log.info("导入文件完成{},记录数 {}",
                     zipEntry.getName(), lineCount);
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importHistoryData() {

        try {
            fileCount = (int) Files.list(Paths.get(directoryPath)).count();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {

            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .forEach(this::processFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("打开文件失败" + e.getMessage());
        }

    }

    private void processFile(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, Charset.forName("GB18030"))) {
            
            int lineCount = importTradeData(reader);

            log.info("导入文件完成[{}/{}]{},记录数 {}",
                    ++finishCount, fileCount, filePath.getFileName(), lineCount);

        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private int importTradeData(BufferedReader reader) throws IOException {
        // Skip the title line
        reader.readLine();

        String line;
        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            // 证券代码,交易时间,开盘价,最高价,最低价,收盘价,前收盘价,涨跌额,涨跌幅,成交量(手),成交额(千元)
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
            if (fields.length > 10) {
                // 有些文件没有成交额
                tradeDay.setTradeAmount(getAmountBigDecimal(fields[10]));
            }
            tradeDay.setImportDate(new Date());

            tradeDayRepository.save(tradeDay);
            lineCount++;
        }
        return lineCount;
    }

    private BigDecimal getPriceBigDecimal(String value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getPctBigDecimal(String value) {
        return new BigDecimal(value).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getVolumeBigDecimal(String value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getAmountBigDecimal(String value) {
        return new BigDecimal(value).setScale(3, BigDecimal.ROUND_HALF_UP);
    }
}
