package me.zhangjh.collector.util;

import lombok.SneakyThrows;

/**
 * @author zhangjh
 * @date 2022/8/30
 */
public class DownloadUtil {

    @SneakyThrows
    public static void download(String downloadPath, String url) {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder commandSb = new StringBuilder("sh -x src/main/download.sh")
                .append(" ")
                .append(downloadPath)
                .append(" ")
                .append(url)
                .append(" &");
        System.out.println(commandSb);
        runtime.exec(commandSb.toString()).waitFor();
    }
}
