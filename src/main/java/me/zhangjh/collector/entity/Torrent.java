package me.zhangjh.collector.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
@Data
@AllArgsConstructor
public class Torrent {
    private String url;
    private String name;
    private String type;
}
