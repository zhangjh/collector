package me.zhangjh.collector.util;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

/**
 * @author zhangjh
 * @date 2022/8/31
 */
public class WebdriverCaptureUtil {

    public static void capture(WebDriver driver, String capturePath) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshot,
                    new File(System.getProperty("user.dir") + File.separator
                            + capturePath + File.separator + System.currentTimeMillis() + ".png"));
        } catch (IOException ignored) {
        }
    }
}
