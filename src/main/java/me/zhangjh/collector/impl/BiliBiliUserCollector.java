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
        WebDriver driver = context.getWebDriver();
        driver.get(url);
        Document document = Jsoup.parse(driver.getPageSource());

        Elements content = document.select("#page-channel .content");
        Elements chanelItems = content.select(".channel-item");
        // 此处个人合集有两种不同格式
        if(chanelItems.size() == 0) {
            chanelItems = content.select(".series-item");
        }

        System.out.println("chanelItems size: " + chanelItems.size());
        for (Element chanelItem : chanelItems) {
            // 页面点击还得借助无头浏览器
            Elements hasMore = chanelItem.select(".btn.more-btn");
            if(hasMore.isEmpty()) {
                driver.findElement(By.cssSelector(chanelItem.cssSelector())).click();
                collectUrls(driver, context, true);
            } else {
                for (Element hasMoreBtn : hasMore) {
                    driver.findElement(By.cssSelector(hasMoreBtn.cssSelector())).click();
                    collectUrls(driver, context, true);
                }
            }
        }
        start2Download(context);
    }

    // 分页递归的时候不需要后退，否则递归会导致多次后退
    private void collectUrls(WebDriver driver, CollectorContext context, boolean needBack) {
        System.out.println("start collectUrls");
        Jedis jedis = context.getJedis();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".content")));

        Document channelItemDocument = Jsoup.parse(driver.getPageSource());
        String channelTitle = channelItemDocument.select(".item.cur").text();
        Elements channelItemContent = channelItemDocument.select(".video-list > li");
        System.out.println("channelItemContent size: " + channelItemContent.size());
        for (Element element : channelItemContent) {
            String channelItemHref = element.select("a").attr("href");
            // 一次push两条：title+url
            if(StringUtils.isNotBlank(channelTitle) && StringUtils.isNotBlank(channelItemHref)) {
                jedis.rpush(context.getRedisBucket() + "/urls", channelTitle);
                jedis.rpush(context.getRedisBucket() + "/urls", channelItemHref);
            }
        }
        // 如果有下一页的话翻页获取下一页
        if(!channelItemDocument.select(".be-pager").attr("style").contains("display: none")) {
            // 翻到头了
            if(channelItemDocument.select(".be-pager-next.be-pager-disabled").isEmpty()) {
                driver.findElement(By.cssSelector(".be-pager-next")).click();
                collectUrls(driver, context, false);
            }
        }
        if(needBack) {
            driver.navigate().back();
        }
    }

    private void start2Download(CollectorContext context) {
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
