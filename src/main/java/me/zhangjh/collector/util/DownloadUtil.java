package me.zhangjh.collector.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author zhangjh
 * @date 2022/8/30
 */
public class DownloadUtil {

    private static String[] buildDownloadCmd(String downloadPath, String url) {
        StringBuilder commandSb = new StringBuilder("sh -x ")
                .append(System.getProperty("user.dir")).append("/src/main/download.sh")
                .append(" ")
                .append(downloadPath)
                .append(" ")
                .append(url);
        String[] cmd = {"sh", "-c", commandSb.toString()};
        System.out.println(Arrays.toString(cmd));
        return cmd;
    }

    public static void download(String downloadPath, String url) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(buildDownloadCmd(downloadPath, url)).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadAndPrintLog(String downloadPath, String url) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.command(buildDownloadCmd(downloadPath, url));
            processBuilder.redirectErrorStream(true);
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            StringBuilder output = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while ((len = inputStreamReader.read(buffer)) != -1) {
                output.append(new String(buffer, 0, len));
                System.out.println(output);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
