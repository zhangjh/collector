package me.zhangjh.collector.impl;

import com.alibaba.fastjson.JSONObject;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import me.zhangjh.collector.util.DownloadUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
public abstract class BiliBiliCollectorInstance {

    protected CloseableHttpClient client = HttpClients.createDefault();

    protected ThreadPoolExecutor executors = new ThreadPoolExecutor(
            2, 5, 0,TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    /** 确保最多只有2个任务放入线程池中执行 */
    protected Semaphore lock = new Semaphore(2);

    protected void run(CollectorContext context) {
        String name = context.getTorrent().getName();
        String downloadPath = context.getDownloadPre() + "/" + name;
        context.setDownloadPath(downloadPath);

        // 加到redis队列尾部
        Jedis jedis = context.getJedis();
        jedis.rpush(context.getRedisBucket(), JSONObject.toJSONString(context.getTorrent()));
        String qValue = jedis.lpop(context.getRedisBucket());
        while (StringUtils.isNotBlank(qValue)) {
            Torrent torrent = JSONObject.parseObject(qValue, Torrent.class);
            System.out.println("start handle: " + torrent.getName());
            this.handle(torrent.getUrl(), context);
            qValue = jedis.lpop(context.getRedisBucket());
        }
        System.out.println("BiliBiliCollectorInstance run finished");
    }

    protected abstract void handle(String url, CollectorContext context);

    protected void start2Download(CollectorContext context) {
        System.out.println("start2Download");
        Jedis jedis = context.getJedis();
        String redisRet = jedis.lpop(context.getRedisBucket() + "/urls");
        System.out.println("redisRet: " + redisRet);
        while (StringUtils.isNotBlank(redisRet)) {
            Map<String, String> map = JSONObject.parseObject(redisRet, Map.class);
            Assert.isTrue(map.entrySet().size() == 1, "缓存数据不符合预期");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String channelTitle = entry.getKey();
                String channelItemHref = entry.getValue();
                if(!channelItemHref.startsWith("https")) {
                    channelItemHref = "https:" + channelItemHref;
                }
                final String finalResUrl = channelItemHref;
                executors.submit(() -> {
                    DownloadUtil.downloadAndPrintLog(context.getDownloadPath()
                            + File.separator + channelTitle, finalResUrl);
                    // 任务完成释放并发锁资源
                });
                redisRet = jedis.lpop(context.getRedisBucket() + "/urls");
            }
        }
    }
}
