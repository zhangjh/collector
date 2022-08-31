package me.zhangjh.collector.impl;

import lombok.SneakyThrows;
import me.zhangjh.collector.entity.CollectorContext;
import me.zhangjh.collector.entity.Torrent;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;

import java.io.File;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
public abstract class BiliBiliCollectorInstance {

    protected CloseableHttpClient client = HttpClients.createDefault();

    protected void mkdir(String downloadPath) {
        // 创建下载路径
        File dir = new File(downloadPath);
        if(!dir.exists()) {
            boolean mkdirsRet = dir.mkdirs();
            Assert.isTrue(mkdirsRet, "创建目录失败:" + dir.getAbsolutePath());
        }
    }

    @SneakyThrows
    protected Document request(Torrent torrent) {
        String url = torrent.getUrl();
        HttpUriRequest request = new HttpGet(url);
        CloseableHttpResponse response = client.execute(request);
        if(response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("访问url: " + url + "失败");
        }
        String content = EntityUtils.toString(response.getEntity());
        Document document = Jsoup.parse(content);

        return document;
    }

    protected abstract void run(CollectorContext context);

    protected abstract void handle(String url, CollectorContext context);
}
