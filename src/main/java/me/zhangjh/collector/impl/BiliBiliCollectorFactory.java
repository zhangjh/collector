package me.zhangjh.collector.impl;

import me.zhangjh.collector.entity.BiliTypeEnum;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

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

    private BiliBiliCollectorInstance getInstance(BiliTypeEnum type) {
        return MAP.get(type);
    }

    public void run(Torrent torrent) {
        String type = torrent.getType();
        Optional<BiliTypeEnum> biliTypeEnum = BiliTypeEnum.getEnumByType(type);
        if(biliTypeEnum.isPresent()) {
            BiliBiliCollectorInstance collector = getInstance(biliTypeEnum.get());
            CollectorContext context = new CollectorContext();
            context.setTorrent(torrent);
            context.setDownloadPre(downloadPre);
            context.setRedisBucket(redisBucket);
            context.setJedis(new Jedis(redisHost, redisPort));
            collector.run(context);
        }
    }
}
