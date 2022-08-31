package me.zhangjh.collector.impl;

import me.zhangjh.collector.crawler.Crawler;
import me.zhangjh.collector.entity.BiliTypeEnum;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
@Component
public class BiliBiliCollectorFactory {

    private static final Map<BiliTypeEnum, BiliBiliCollectorInstance> MAP = new HashMap<>();
    static {
        MAP.put(BiliTypeEnum.USER, new BiliBiliUserCollector());
        MAP.put(BiliTypeEnum.ITEM, new BiliBiliItemCollector());
    }

    @Value("${bilibili.download.path}")
    private String downloadPre;

    @Value("${spring.redis.bucket}")
    private String redisBucket;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private Integer redisPort;

    @Autowired
    private Crawler crawler;

    private BiliBiliCollectorInstance getInstance(BiliTypeEnum type) {
        return MAP.get(type);
    }

    public void run(Torrent torrent) {
        WebDriver driver = crawler.getDriver();
        CollectorContext context = new CollectorContext();
        context.setWebDriver(driver);
        try {
            String type = torrent.getType();
            Optional<BiliTypeEnum> biliTypeEnum = BiliTypeEnum.getEnumByType(type);
            if(biliTypeEnum.isPresent()) {
                BiliBiliCollectorInstance collector = getInstance(biliTypeEnum.get());
                context.setTorrent(torrent);
                context.setDownloadPre(downloadPre);
                context.setRedisBucket(redisBucket);
                context.setJedis(new Jedis(redisHost, redisPort));
                collector.run(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(driver.getCurrentUrl());
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(screenshot,
                        new File("../screenshot" + File.separator
                                + System.currentTimeMillis() + ".png"));
            } catch (IOException ignored) {
            }
            driver.quit();
        }
    }
}
