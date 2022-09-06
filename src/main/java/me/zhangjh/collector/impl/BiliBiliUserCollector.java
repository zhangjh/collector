package me.zhangjh.collector.impl;

import com.ruiyun.jvppeteer.core.page.Page;
import lombok.SneakyThrows;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.util.WebdriverCaptureUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutionException;

/**
 * @author zhangjh
 * @date 2022/8/29
 * b站用户空间合集视频获取
 */
public class BiliBiliUserCollector extends BiliBiliCollectorInstance {

    @Override
    @SneakyThrows
    protected void handle(String url, CollectorContext context) {
        Page page = context.getPage();
        page.goTo(url);

        page.waitForSelector("#page-channel .content");

        Document document = Jsoup.parse(page.content());

        Elements content = document.select("#page-channel .content");
        Elements chanelItems = content.select(".channel-item");
        // 此处个人合集有两种不同格式
        if(chanelItems.size() == 0) {
            chanelItems = content.select(".series-item");
        }

        System.out.println("chanelItems size: " + chanelItems.size());
        if(chanelItems.size() == 0) {
            WebdriverCaptureUtil.capture(page, "screenshot");
        }
        for (Element chanelItem : chanelItems) {
            // 页面点击还得借助无头浏览器
            Elements hasMore = chanelItem.select(".btn.more-btn");
            if(hasMore.isEmpty()) {
                page.waitForSelector(chanelItem.cssSelector());
                page.click(chanelItem.cssSelector());
                collectUrls(page, context, true);
            } else {
                for (Element hasMoreBtn : hasMore) {
                    page.waitForSelector(hasMoreBtn.cssSelector());
                    page.click(hasMoreBtn.cssSelector());
                    collectUrls(page, context, true);
                }
            }
        }
        start2Download(context);
    }

    // 分页递归的时候不需要后退，否则递归会导致多次后退
    private void collectUrls(Page page, CollectorContext context, boolean needBack) throws ExecutionException, InterruptedException {
        System.out.println("start collectUrls");
        Jedis jedis = context.getJedis();
        page.waitForSelector(".video-list > li");

        Document channelItemDocument = Jsoup.parse(page.content());
        String channelTitle = channelItemDocument.select(".item.cur").text();
        Elements channelItemContent = channelItemDocument.select(".video-list > li");
        System.out.println("channelTitle:" + channelTitle + ",channelItemContent size: " + channelItemContent.size());
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
                page.waitForSelector(".be-pager-next");
                page.click(".be-pager-next");
                collectUrls(page, context, false);
            }
        }
        if(needBack) {
            page.evaluate("window.history.go(-1)");
        }
    }
}
