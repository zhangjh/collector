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

    public static void main(String[] args) throws Exception {
        download("./", "https://www.bilibili.com/video/BV1iA4y197K6?spm_id_from=333.999.0.0");
    }
}
