package me.zhangjh.collector.crawler;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

    private static final List<String> ARG_LIST = Arrays.asList("--no-sandbox","--incognito",
            "--user-agent=" + "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like " +
                    "Gecko) Chrome/102.0.0.0 Safari/537.36",
            "--disable-setuid-sandbox","-disable-dev-shm-usage","--disable-blink-features=AutomationControlled",
            "--start-maximized","--user-data-dir=./userData");

    public Page getPage() throws Exception {
        // 第一次自动下载，后续不再下载
        BrowserFetcher.downloadIfNotExist(null);

        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(ARG_LIST)
//                .withExecutablePath(chromeExecutablePath)
                .withHeadless(true).build();
        options.setDevtools(false);
        options.setViewport(null);
        options.setIgnoreDefaultArgs(Arrays.asList("--disable-extensions", "--enable-automation"));
        Browser browser = Puppeteer.launch(options);
        Page page = browser.pages().get(0);
        page.setDefaultNavigationTimeout(0);
        hideHeadless(page);
        return page;
    }

    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(headless);
        options.addArguments(ARG_LIST);
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(50));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(50));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(50));
        return driver;
    }

    private void hideHeadless(Page page) {
        page.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");

        page.evaluateOnNewDocument("() => {\n" +
                "    const newProto = navigator.__proto__;\n" +
                "    delete newProto.webdriver;\n" +
                "    navigator.__proto__ = newProto;");
        page.evaluateOnNewDocument("() => {\n" +
                "    window.chrome = {};\n" +
                "    window.chrome.app = {\n" +
                "        InstallState: 'hehe',\n" +
                "        RunningState: 'haha',\n" +
                "        getDetails: 'xixi',\n" +
                "        getIsInstalled: 'ohno',\n" +
                "    };\n" +
                "    window.chrome.csi = function () {};\n" +
                "    window.chrome.loadTimes = function () {};\n" +
                "    window.chrome.runtime = function () {};\n");
        page.evaluateOnNewDocument("() => {\n" +
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
                "    });\n");
        page.evaluateOnNewDocument("() => {\n" +
                "    Object.defineProperty(navigator, 'languages', {\n" +
                "        get: () => ['zh-CN', 'zh', 'en'],\n" +
                "    });");
        page.evaluateOnNewDocument("() => {\n" +
                "    const originalQuery = window.navigator.permissions.query; //notification伪装\n" +
                "    window.navigator.permissions.query = (parameters) =>\n" +
                "        parameters.name === 'notifications'\n" +
                "        ? Promise.resolve({ state: Notification.permission })\n" +
                "        : originalQuery(parameters);\n" +
                "}\n");
        page.evaluateOnNewDocument("() => {\n" +
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
                "    };\n");
    }
}
