import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Selenium2Example {

    static final int TIME_OUT_IN_SECONDS = 30;
    static final int NUMBER_RETRY_ATTEMPTS = 3;


    public static void main(String[] args) throws AWTException, IOException {

        Boolean bookedSucessFully = false;
        Logger log = LogManager.getLogger();
        log.info("Starting Booking App");

        if (args.length != 2) {
            throw new IllegalArgumentException("Please provide the time of the slot that you want to book & path to chrome driver ");
        }

        for (int attempCount = 0; !bookedSucessFully && attempCount < NUMBER_RETRY_ATTEMPTS; attempCount++) {

            log.info("Attempt Number {}", attempCount);

            WebDriver driver = null;
            try {

                System.setProperty("webdriver.chrome.driver", args[1]);

                Map<String, String> mobileEmulation = new HashMap<>();
                mobileEmulation.put("deviceName", "iPhone 6");

                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
                chromeOptions.addArguments("disable-infobars");

                driver = new ChromeDriver(chromeOptions);

                driver.get("https://better.legendonlineservices.co.uk/Hillingdon_-_Queensm/account/login");

                // Find the text input element by its name
                WebElement login_email = driver.findElement(By.id("login_Email"));

                // Enter something to search for
                login_email.sendKeys("kpboyslondon@gmail.com");

                WebElement password = driver.findElement(By.id("login_Password"));
                password.sendKeys("kpboysrock");

                WebElement login = driver.findElement(By.id("login_Submit"));
                login.submit();

                // Check the title of the page
                System.out.println("Page title is: " + driver.getTitle());

                // Wait for the page to load, timeout after 10 seconds
                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> {
                    //return d.getTitle().toLowerCase().startsWith("cheese!");
                    return d.findElement(By.id("cscNavBookings")).isDisplayed();
                });

                driver.findElement(By.id("cscNavBookings")).click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Make a Booking")).isDisplayed());
                driver.findElement(By.partialLinkText("Make a Booking")).click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Queensmead")).isDisplayed());
                driver.findElement(By.partialLinkText("Queensmead")).click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Court and Pitch Booking")).isDisplayed());
                driver.findElement(By.partialLinkText("Court and Pitch Booking")).click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Badminton Court - 60mins")).isDisplayed());
                driver.findElement(By.partialLinkText("Badminton Court - 60mins")).click();


                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.id("activityTimetableList")).isDisplayed());


                WebElement availableSlot = null;
                boolean found = false;

                Object[] webElements = driver.findElement(By.id("activityTimetableList")).findElements(By.tagName("li")).toArray();

                String bookingTime = BookingSchedule.getTime(LocalDateTime.now().getDayOfWeek());

                for (int j = 0; attempCount < webElements.length; j++) {
                    WebElement webElement = ((WebElement) webElements[webElements.length -1 -j]);

                    try {

                        availableSlot = webElement.findElement(By.partialLinkText(bookingTime));
                        found = true;
                        log.info("found slot for {}", bookingTime);
                    } catch (NoSuchElementException e) {
                    }
                    if (found) break;
                }

                if (!found) {
                    throw new CouldNotFindAvailableSlotException("couldn't find available slot");
                }

                while (LocalTime.now().isBefore(LocalTime.parse("22:00:01"))) {
                    log.info("Waiting for 10 ms");
                    Thread.sleep(10);
                }

                JavascriptExecutor jse = (JavascriptExecutor)driver;
                jse.executeScript("arguments[0].scrollIntoView()", availableSlot);

                availableSlot.click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.id("addToBasketBtn")).isDisplayed());
                driver.findElement(By.id("addToBasketBtn")).click();


                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Pay by credit/debit card")).isDisplayed());

                WebDriverWait wait2 = new WebDriverWait(driver, TIME_OUT_IN_SECONDS);
                wait2.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Use voucher")));

                driver.findElement(By.partialLinkText("Use voucher")).findElements(By.tagName("span")).get(1).click();

                (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until((ExpectedCondition<Boolean>) d -> d.findElement(By.partialLinkText("Confirm booking")).isDisplayed());
                driver.findElement(By.partialLinkText("Confirm booking")).click();


                bookedSucessFully = true;
                log.info("Successfully booked slot for {}", bookingTime);
            }catch (CouldNotFindAvailableSlotException ex){
                bookedSucessFully = true;
                log.error("couldn't find available slot", ex);
            }
            catch (Exception e) {
                bookedSucessFully = false;
                log.error("Couldn't book a slot due to following error", e);
            } finally {
                File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                // Now you can do whatever you need to do with it, for example copy somewhere
                FileUtils.copyFile(scrFile, new File(Paths.get("") + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + attempCount + ".png"));

                if (null != driver) {
                    driver.quit();
                }
            }
        }
        log.info("Ending Booking App");
    }
}