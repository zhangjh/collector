package me.zhangjh.collector.entity;

import lombok.Data;
import redis.clients.jedis.Jedis;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
@Data
public class CollectorContext {
    private Torrent torrent;

    private String downloadPre;

    private String downloadPath;

    private Jedis jedis;

    private String redisBucket;
}
