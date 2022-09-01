package me.zhangjh.collector.impl;

import me.zhangjh.collector.crawler.Crawler;
import me.zhangjh.collector.entity.BiliTypeEnum;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import me.zhangjh.collector.util.WebdriverCaptureUtil;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
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

    private WebDriver driver;

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

    @PostConstruct
    public void init() {
        driver = crawler.getDriver();
    }

    private BiliBiliCollectorInstance getInstance(BiliTypeEnum type) {
        return MAP.get(type);
    }

    public void run(List<Torrent> torrents) {
        CollectorContext context = new CollectorContext();
        context.setWebDriver(driver);
        try {
            for (Torrent torrent : torrents) {
                String type = torrent.getType();
                Optional<BiliTypeEnum> biliTypeEnum = BiliTypeEnum.getEnumByType(type);
                if(biliTypeEnum.isPresent()) {
                    BiliBiliCollectorInstance collector = getInstance(biliTypeEnum.get());
                    context.setTorrent(torrent);
                    context.setDownloadPre(downloadPre);
                    context.setRedisBucket(redisBucket);
                    Jedis jedis = new Jedis(redisHost, redisPort);
                    context.setJedis(jedis);
                    // 开始运行前，初始化redis，防止上次异常退出redis有数据残留
                    jedis.del(redisBucket);
                    jedis.del(redisBucket + "/urls");
                    collector.run(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            WebdriverCaptureUtil.capture(driver, "screenshot");
        } finally {
            driver.quit();
        }
    }

    /** 优雅停机，在此进行redis的清理
     *  chrome的关闭
     * */
    public void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("application shutdown gracefully...");

            Jedis jedis = new Jedis(redisHost, redisPort);
            jedis.del(redisBucket);
            jedis.del(redisBucket + "/urls");

            driver.close();
        }));
    }
}
