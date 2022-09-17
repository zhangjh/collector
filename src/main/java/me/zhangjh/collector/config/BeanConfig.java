package me.zhangjh.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
@Configuration
public class BeanConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private Integer redisPort;

    @Value("${spring.redis.requirePass}")
    private boolean requirePass;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public Jedis jedis() {
        Jedis jedis = new Jedis(redisHost, redisPort);
        if(requirePass) {
            jedis.auth(password);
        }
        return jedis;
    }
}
