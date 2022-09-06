package me.zhangjh.collector.util;

import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.io.File;
import java.io.IOException;

/**
 * @author zhangjh
 * @date 2022/8/31
 */
public class WebdriverCaptureUtil {

    public static void capture(Page page, String capturePath) {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setType("png");
        options.setPath(System.getProperty("user.dir") + File.separator
                + capturePath + File.separator + System.currentTimeMillis() + ".png");
        try {
            page.screenshot(options);
        } catch (IOException ignored) {
        }
    }
}
