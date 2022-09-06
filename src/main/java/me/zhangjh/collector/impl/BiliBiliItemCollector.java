package me.zhangjh.collector.impl;

import com.ruiyun.jvppeteer.core.page.Page;
import lombok.SneakyThrows;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.util.DownloadUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;

/**
 * @author zhangjh
 * @date 2022/8/29
 * b站节目列表获取
 */
public class BiliBiliItemCollector extends BiliBiliCollectorInstance {

    @Override
    @SneakyThrows
    protected void handle(String url, CollectorContext context) {
        Page page = context.getPage();
        page.goTo(url);

        page.waitForSelector("#eplist_module");

        Document document = Jsoup.parse(page.content());

        String title = document.select("#media_module > div > a").text();

        executors.submit(() -> DownloadUtil.downloadAndPrintLog(context.getDownloadPath()
                + File.separator + title, url));
    }
}
