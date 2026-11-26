
package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import demo.wrappers.Wrappers;

public class TestCases {

    ChromeDriver driver;

    @BeforeTest
    public void startBrowser() {

        System.setProperty(
                "java.util.logging.config.file",
                "logging.properties");

        ChromeOptions options = new ChromeOptions();

        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);

        options.setCapability("goog:loggingPrefs", logs);

        options.addArguments("--remote-allow-origins=*");

        System.setProperty(
                ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
                "build/chromedriver.log");

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    // =====================================================
    // TEST CASE 01 - HOCKEY TEAMS
    // =====================================================

    @Test(priority = 1)
    public void testCase01() throws Exception {

        Wrappers wrapper = new Wrappers(driver);

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(15));

        // Launch website
        wrapper.launchURL(
                "https://www.scrapethissite.com/pages/");

        // Click Hockey Teams
        wrapper.clickElement(
                By.xpath(
                        "//a[text()='Hockey Teams: Forms, Searching and Pagination']"));

        // Wait for table
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("table tbody tr")));

        // Create ArrayList
        ArrayList<HashMap<String, Object>> hockeyData = new ArrayList<>();

        // Epoch timestamp
        long scrapeEpochTime = Instant.now().getEpochSecond();

        // Create output directory
        Path outputDirectory = Paths.get("output");

        Files.createDirectories(outputDirectory);

        // Iterate through first 4 pages
        for (int page = 1; page <= 4; page++) {

            System.out.println("Scraping Hockey Page: " + page);

            List<WebElement> rows = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector("table tbody tr")));

            for (WebElement row : rows) {

                try {

                    List<WebElement> columns = row.findElements(
                            By.tagName("td"));

                    if (columns.size() < 6) {
                        continue;
                    }

                    String teamName = columns.get(0).getText().trim();

                    String year = columns.get(1).getText().trim();

                    String winPercentageText = columns.get(5).getText().trim();

                    double winPercentage = Double.parseDouble(winPercentageText);

                    // Filter Win % less than 0.40
                    if (winPercentage < 0.40) {

                        HashMap<String, Object> teamData = new HashMap<>();

                        teamData.put(
                                "Epoch Time of Scrape",
                                scrapeEpochTime);

                        teamData.put("Team Name", teamName);

                        teamData.put(
                                "Year",
                                Integer.parseInt(year));

                        teamData.put(
                                "Win %",
                                winPercentage);

                        hockeyData.add(teamData);

                        System.out.println(
                                teamName + " | "
                                        + year + " | "
                                        + winPercentage);
                    }

                } catch (StaleElementReferenceException e) {

                    System.out.println("Skipping stale hockey row");

                } catch (NumberFormatException e) {

                    System.out.println("Skipping invalid hockey data");
                }
            }

            // Navigate to next page
            if (page < 4) {

                WebElement firstRow = driver.findElement(
                        By.cssSelector("table tbody tr"));

                int nextPage = page + 1;

                WebElement nextPageLink = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath(
                                        "//ul[contains(@class,'pagination')]"
                                                + "//a[normalize-space()='"
                                                + nextPage + "']")));

                nextPageLink.click();

                wait.until(
                        ExpectedConditions.stalenessOf(firstRow));

                wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("table tbody tr")));
            }
        }

        // Convert Hockey ArrayList to JSON
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(
                SerializationFeature.INDENT_OUTPUT);

        File hockeyFile = outputDirectory.resolve(
                "hockey-team-data.json").toFile();

        mapper.writeValue(hockeyFile, hockeyData);

        // Validate JSON file
        Assert.assertTrue(
                hockeyFile.exists(),
                "Hockey JSON file was not created");

        Assert.assertTrue(
                hockeyFile.length() > 0,
                "Hockey JSON file is empty");

        System.out.println(
                "Hockey records collected: " + hockeyData.size());

        System.out.println(
                "Hockey JSON: " + hockeyFile.getAbsolutePath());
    }

    // =====================================================
    // TEST CASE 02 - OSCAR WINNING FILMS
    // =====================================================

    @Test(priority = 2)
    public void testCase02() throws Exception {

        Wrappers wrapper = new Wrappers(driver);

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(20));

        // Launch website
        wrapper.launchURL(
                "https://www.scrapethissite.com/pages/");

        // Click Oscar Winning Films
        driver.get("https://www.scrapethissite.com/pages/");

        WebDriverWait wait1 = new WebDriverWait(
                driver, Duration.ofSeconds(20));

        WebElement oscarLink = wait1.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath(
                                "//a[contains(normalize-space(.), 'Oscar Winning Films')]")));

        oscarLink.click();

        // Wait for year links
        wait1.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("a.year-link")));

        // Create ArrayList
        ArrayList<HashMap<String, Object>> oscarData = new ArrayList<>();

        // Epoch timestamp
        long scrapeEpochTime = Instant.now().getEpochSecond();

        // Get all available years
        List<WebElement> yearElements = driver.findElements(
                By.cssSelector("a.year-link"));

        ArrayList<String> years = new ArrayList<>();

        for (WebElement element : yearElements) {

            years.add(element.getText().trim());
        }

        System.out.println("Oscar Years: " + years);

        // Iterate through each year
        for (String year : years) {

            System.out.println("Scraping Oscar Year: " + year);

            // Get existing movie rows
            List<WebElement> existingRows = driver.findElements(
                    By.cssSelector("tr.film"));

            WebElement oldFirstRow = existingRows.isEmpty()
                    ? null
                    : existingRows.get(0);

            // Click year link
            WebElement yearLink = wait1.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//a[contains(@class,'year-link')"
                                            + " and normalize-space()='"
                                            + year + "']")));

            yearLink.click();

            // Wait for AJAX update
            if (oldFirstRow != null) {

                wait1.until(
                        ExpectedConditions.stalenessOf(oldFirstRow));
            }

            // Wait for movie rows
            wait1.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(
                            By.cssSelector("tr.film")));

            List<WebElement> movieRows = driver.findElements(
                    By.cssSelector("tr.film"));

            // Get top 5 movies
            int movieCount = Math.min(5, movieRows.size());

            for (int i = 0; i < movieCount; i++) {

                WebElement row = movieRows.get(i);

                String title = row.findElement(
                        By.cssSelector("td.film-title")).getText().trim();

                String nominations = row.findElement(
                        By.cssSelector("td.film-nominations")).getText().trim();

                String awards = row.findElement(
                        By.cssSelector("td.film-awards")).getText().trim();

                WebElement bestPictureCell = row.findElement(
                        By.cssSelector("td.film-best-picture"));

                String bestPictureText = bestPictureCell.getText().trim();

                String bestPictureHtml = bestPictureCell.getAttribute("innerHTML");

                // Check Best Picture winner
                boolean isWinner = bestPictureText.equalsIgnoreCase("Yes")
                        || bestPictureText.equalsIgnoreCase("True")
                        || bestPictureText.equals("★")
                        || bestPictureHtml.contains("glyphicon-star")
                        || bestPictureHtml.contains("fa-star");

                // Store movie information
                HashMap<String, Object> movieData = new HashMap<>();

                movieData.put(
                        "Epoch Time of Scrape",
                        scrapeEpochTime);

                movieData.put(
                        "Year",
                        Integer.parseInt(year));

                movieData.put("Title", title);

                movieData.put(
                        "Nomination",
                        nominations);

                movieData.put(
                        "Awards",
                        awards);

                movieData.put(
                        "isWinner",
                        isWinner);

                oscarData.add(movieData);

                System.out.println(
                        "Title: " + title
                                + " | Year: " + year
                                + " | Nominations: " + nominations
                                + " | Awards: " + awards
                                + " | Winner: " + isWinner);
            }
        }

        // Create output directory
        Path outputDirectory = Paths.get("output");

        Files.createDirectories(outputDirectory);

        // Create Oscar JSON file
        File oscarFile = outputDirectory.resolve(
                "oscar-winner-data.json").toFile();

        // Initialize Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(
                SerializationFeature.INDENT_OUTPUT);

        // Convert ArrayList to JSON
        mapper.writeValue(oscarFile, oscarData);

        // TestNG assertions
        Assert.assertTrue(
                oscarFile.exists(),
                "Oscar JSON file was not created");

        Assert.assertTrue(
                oscarFile.length() > 0,
                "Oscar JSON file is empty");

        System.out.println(
                "Oscar records collected: " + oscarData.size());

        System.out.println(
                "Oscar JSON: " + oscarFile.getAbsolutePath());
    }

    // =====================================================
    // CLOSE BROWSER
    // =====================================================

    @AfterTest
    public void endTest() {

        if (driver != null) {

            driver.quit();
        }
    }
}
