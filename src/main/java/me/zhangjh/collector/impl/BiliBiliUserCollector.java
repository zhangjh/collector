package me.zhangjh.collector.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import me.zhangjh.collector.util.DownloadUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.time.Duration;

/**
 * @author zhangjh
 * @date 2022/8/29
 * b站用户空间合集视频获取
 */
public class BiliBiliUserCollector extends BiliBiliCollectorInstance {

    @Override
    protected void run(CollectorContext context) {
        String name = context.getTorrent().getName();
        String downloadPath = context.getDownloadPre() + "/" + name;
        mkdir(downloadPath);
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

    @Override
    @SneakyThrows
    protected void handle(String url, CollectorContext context) {
        Jedis jedis = context.getJedis();

        WebDriver driver = context.getWebDriver();
        driver.get(url);
        Document document = Jsoup.parse(driver.getPageSource());

        Elements content = document.select("#page-channel .content");
        Elements chanelItems = content.select(".channel-item");

        for (Element chanelItem : chanelItems) {
            // 页面点击还得借助无头浏览器
            driver.findElement(By.cssSelector(chanelItem.cssSelector())).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".content > .video-list > li")));

            Document channelItemDocument = Jsoup.parse(driver.getPageSource());
            String channelTitle = channelItemDocument.select(".item.cur").text();
            Elements channelItemContent = channelItemDocument.select(".content > .video-list > li");
            for (Element element : channelItemContent) {
                String channelItemHref = element.select("a").attr("href");
                // 一次push两条：title+url
                jedis.rpush(context.getRedisBucket() + "/urls", channelTitle);
                jedis.rpush(context.getRedisBucket() + "/urls", channelItemHref);
            }
            driver.navigate().back();
        }
        // 相应地，一次要pop两条
        String channelItemHref = jedis.lpop(context.getRedisBucket() + "/urls");
        String channelTitle = jedis.lpop(context.getRedisBucket() + "/urls");
        while (StringUtils.isNotBlank(channelItemHref)) {
            if(!channelItemHref.startsWith("https")) {
                channelItemHref = "https:" + channelItemHref;
            }
            final String finalResUrl = channelItemHref;
            String finalChannelTitle = channelTitle;
            executors.submit(() -> DownloadUtil.download(context.getDownloadPath()
                            + File.separator + finalChannelTitle, finalResUrl));
            channelTitle = jedis.lpop(context.getRedisBucket() + "/urls");
            channelItemHref = jedis.lpop(context.getRedisBucket() + "/urls");
        }
    }
}
