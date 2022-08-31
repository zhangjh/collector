package me.zhangjh.collector.util;

/**
 * @author zhangjh
 * @date 2022/8/30
 */
public class DownloadUtil {

    public static void download(String downloadPath, String url) throws Exception {
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
