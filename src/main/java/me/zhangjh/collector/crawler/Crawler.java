package me.zhangjh.collector.crawler;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhangjh
 * @date 2022/8/30
 * 模拟浏览器操作
 */
@Component
public class Crawler {

    @Value("${browser.headless}")
    private boolean headless;

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36";

    private static final List<String> ARG_LIST = Arrays.asList("--no-sandbox","--incognito",
            "--user-agent=" + USER_AGENT, "--disable-dev-shm-usage", "enable-automation",
            "--disable-setuid-sandbox","--disable-dev-shm-usage","--disable-blink-features=AutomationControlled",
            "--disable-extensions","--dns-prefetch-disable","--disable-gpu","--ignore-certificate-errors",
            "--start-maximized","--user-data-dir=./userData");

    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(headless);
        options.addArguments(ARG_LIST);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(50));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        List<String> scripts = hideHeadlessScripts();
        for (String script : scripts) {
            ((JavascriptExecutor) driver).executeScript(script);
        }
        return driver;
    }

    private List<String> hideHeadlessScripts() {
        List<String> scripts = new ArrayList<>();
        scripts.add("() => {" +
                "    const newProto = navigator.__proto__;\n" +
                "    delete newProto.webdriver;\n" +
                "    navigator.__proto__ = newProto;}");
        scripts.add("() => {" +
                "    window.chrome = {};\n" +
                "    window.chrome.app = {\n" +
                "        InstallState: 'hehe',\n" +
                "        RunningState: 'haha',\n" +
                "        getDetails: 'xixi',\n" +
                "        getIsInstalled: 'ohno',\n" +
                "    };\n" +
                "    window.chrome.csi = function () {};\n" +
                "    window.chrome.loadTimes = function () {};\n" +
                "    window.chrome.runtime = function () {};}\n");
        scripts.add("() => {" +
                "    Object.defineProperty(navigator, 'plugins', {\n" +
                "        get: () => [\n" +
                "        {\n" +
                "            0: {\n" +
                "            type: 'application/x-google-chrome-pdf',\n" +
                "            suffixes: 'pdf',\n" +
                "            description: 'Portable Document Format',\n" +
                "            enabledPlugin: Plugin,\n" +
                "            },\n" +
                "            description: 'Portable Document Format',\n" +
                "            filename: 'internal-pdf-viewer',\n" +
                "            length: 1,\n" +
                "            name: 'Chrome PDF Plugin',\n" +
                "        },\n" +
                "        {\n" +
                "            0: {\n" +
                "            type: 'application/pdf',\n" +
                "            suffixes: 'pdf',\n" +
                "            description: '',\n" +
                "            enabledPlugin: Plugin,\n" +
                "            },\n" +
                "            description: '',\n" +
                "            filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai',\n" +
                "            length: 1,\n" +
                "            name: 'Chrome PDF Viewer',\n" +
                "        },\n" +
                "        {\n" +
                "            0: {\n" +
                "            type: 'application/x-nacl',\n" +
                "            suffixes: '',\n" +
                "            description: 'Native Client Executable',\n" +
                "            enabledPlugin: Plugin,\n" +
                "            },\n" +
                "            1: {\n" +
                "            type: 'application/x-pnacl',\n" +
                "            suffixes: '',\n" +
                "            description: 'Portable Native Client Executable',\n" +
                "            enabledPlugin: Plugin,\n" +
                "            },\n" +
                "            description: '',\n" +
                "            filename: 'internal-nacl-plugin',\n" +
                "            length: 2,\n" +
                "            name: 'Native Client',\n" +
                "        },\n" +
                "        ],\n" +
                "    });}\n");
        scripts.add("() => {" +
                "    Object.defineProperty(navigator, 'languages', {\n" +
                "        get: () => ['zh-CN', 'zh', 'en'],\n" +
                "    });}");
        scripts.add("() => {" +
                "    const originalQuery = window.navigator.permissions.query; //notification伪装\n" +
                "    window.navigator.permissions.query = (parameters) =>\n" +
                "        parameters.name === 'notifications'\n" +
                "        ? Promise.resolve({ state: Notification.permission })\n" +
                "        : originalQuery(parameters);\n" +
                "}\n");
        scripts.add("() => {" +
                "    const getParameter = WebGLRenderingContext.getParameter;\n" +
                "    WebGLRenderingContext.prototype.getParameter = function (parameter) {\n" +
                "        // UNMASKED_VENDOR_WEBGL\n" +
                "        if (parameter === 37445) {\n" +
                "            return 'Intel Inc.';\n" +
                "        }\n" +
                "        // UNMASKED_RENDERER_WEBGL\n" +
                "        if (parameter === 37446) {\n" +
                "            return 'Intel(R) Iris(TM) Graphics 6100';\n" +
                "        }\n" +
                "        return getParameter(parameter);\n" +
                "    };}\n");
        return scripts;
    }
}
