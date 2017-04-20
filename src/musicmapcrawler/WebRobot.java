package musicmapcrawler;

import static com.thoughtworks.selenium.SeleneseTestBase.assertFalse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Nexor
 */
public class WebRobot {

    public PhantomJSDriver driver;

    public WebRobot() {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "lib\\phantomjs.exe");
        capabilities.setBrowserName("Mozilla/5.0 (Windows NT x.y; WOW64; rv:10.0) Gecko/20100101 Firefox/10.0");
        driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(new Dimension(1024, 768));
        addShutdownHook();
    }

    public void loadCookies(File f) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        Set<Cookie> cookies = (Set<Cookie>) ois.readObject();
        for (Cookie cooky : cookies) {
            driver.manage().addCookie(cooky);
        }
        ois.close();
    }

    public void saveCookies(File f) throws IOException {
        if (!f.exists()) {
            f.createNewFile();
        }
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(driver.manage().getCookies());
        oos.flush();
        oos.close();
    }

    public static void closePreviousDriverInstances() {
        try {
            System.out.print("Closing older PhantomJS instances... ");
            int exitCode = Runtime.getRuntime().exec("cmd /c taskkill /F /IM phantomjs.exe").waitFor();
            switch (exitCode) {
                case 0:
                    System.out.println("Process [phantomjs.exe] closed.");
                    break;
                case 128:
                    System.out.println("Process [phantomjs.exe] not found.");
                    break;
                case 1:
                    System.out.println("Error terminating process [phantomjs.exe]");
                    break;
                default:
                    System.out.println("deu merda da forte");
                    break;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(WebRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void waitForPageToLoad() {
        ExpectedCondition<Boolean> pageLoad = (WebDriver f) -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
        Wait<WebDriver> wait = new WebDriverWait(driver, 60);
        try {
            wait.until(pageLoad);
        } catch (Throwable pageLoadWaitError) {
            assertFalse("Timeout during page load", true);
        }
    }

    public void saveScreenshotToDesktop() {
        long t1 = System.currentTimeMillis();
        File f = driver.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(f, new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + f.getName()));
        } catch (IOException ex) {
            Logger.getLogger(WebRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Screenshot taken: " + (System.currentTimeMillis() - t1) / 1000 + "s elapsed.");
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                driver.quit();
            }
        });
    }
}
