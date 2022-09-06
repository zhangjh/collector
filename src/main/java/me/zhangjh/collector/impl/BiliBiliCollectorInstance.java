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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
public abstract class BiliBiliCollectorInstance {

    protected CloseableHttpClient client = HttpClients.createDefault();

    protected ThreadPoolExecutor executors = new ThreadPoolExecutor(
            5, 10, 10,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10), Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    protected void mkdir(String downloadPath) {
        // 创建下载路径
        File dir = new File(downloadPath);
        if(!dir.exists()) {
            boolean mkdirsRet = dir.mkdirs();
            Assert.isTrue(mkdirsRet, "创建目录失败:" + dir.getAbsolutePath());
        }
    }

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
    }

    protected abstract void handle(String url, CollectorContext context);

    protected void start2Download(CollectorContext context) {
        System.out.println("start2Download");
        Jedis jedis = context.getJedis();
        // 相应地，一次要pop两条
        String channelTitle = jedis.lpop(context.getRedisBucket() + "/urls");
        String channelItemHref = jedis.lpop(context.getRedisBucket() + "/urls");
        while (StringUtils.isNotBlank(channelItemHref)) {
            if(!channelItemHref.startsWith("https")) {
                channelItemHref = "https:" + channelItemHref;
            }
            final String finalResUrl = channelItemHref;
            String finalChannelTitle = channelTitle;
            executors.submit(() -> DownloadUtil.downloadAndPrintLog(context.getDownloadPath()
                    + File.separator + finalChannelTitle, finalResUrl));
            channelTitle = jedis.lpop(context.getRedisBucket() + "/urls");
            channelItemHref = jedis.lpop(context.getRedisBucket() + "/urls");
        }
    }
}
