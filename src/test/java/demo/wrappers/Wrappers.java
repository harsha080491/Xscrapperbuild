package demo.wrappers;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wrappers {

    public ChromeDriver driver;
    public WebDriverWait wait;

    public Wrappers(ChromeDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void launchURL(String url) {
        driver.get(url);
    }

    public void clickElement(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        driver.findElement(locator).click();
    }

    public void enterText(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(text);
    }
}