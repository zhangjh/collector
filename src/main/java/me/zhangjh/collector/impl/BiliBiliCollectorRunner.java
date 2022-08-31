package me.zhangjh.collector.impl;

import lombok.Data;
import me.zhangjh.collector.entity.Torrent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangjh
 * @date 2022/8/29
 * b站视频采集入口
 */
@Component
@ConfigurationProperties(prefix = "bilibili.start")
@Data
public class BiliBiliCollectorRunner {
    private List<String> torrents;

    @Autowired
    private BiliBiliCollectorFactory collectorFactory;

    @PostConstruct
    private void init() {
        List<Torrent> torrentList = torrents.stream().map(torrent -> {
            System.out.println(torrent);
            String[] split = torrent.split("::");
            String url = split[0];
            String name = split[1];
            String type = split[2];
            System.out.println("name:" + name);
            return new Torrent(url, name, type);
        }).collect(Collectors.toList());
        getVideos(torrentList);
    }

    private void getVideos(List<Torrent> torrents) {
//        collectorFactory.run(torrents);
    }
}
