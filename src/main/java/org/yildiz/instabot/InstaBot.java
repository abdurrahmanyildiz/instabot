package org.yildiz.instabot;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

public class InstaBot {
    private final String INSTA_HOME_PAGE = "https://www.instagram.com";

    private final By usernameLocator = new By.ByCssSelector("input[name='username']");
    private final By passwordLocator = new By.ByCssSelector("input[name='password']");
    private final By loginButtonLocator = new By.ByCssSelector("button[type='submit']");
    private final By postsLocator = new By.ByCssSelector("article a[href*='/p/']");
    private final By profileLocator = new By.ByCssSelector("img[alt*='s profile picture']");

    private WebDriver webDriver = new ChromeDriver();

    public String startBot(String username, String password, int picLimit) {
        try {
            final String USER_PROFILE = INSTA_HOME_PAGE + "/" + username;
            webDriver.manage().window().maximize();
            JavascriptExecutor jse = (JavascriptExecutor) webDriver;

            webDriver.get(INSTA_HOME_PAGE);
            login(username, password);
            waitTo(profileLocator);
            webDriver.get(USER_PROFILE);
            waitTo(postsLocator);

            Set<String> picLinks = collectPicLinks(jse, picLimit);

            HashMap<String, Integer> likeCounter = countLikes(jse, picLinks);

            String fanList = likeCounter.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .toList()
                    .toString();

            jse.executeScript("alert('" + fanList + "');");

            return fanList;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private void login(String username, String password) {
        waitTo(usernameLocator);
        webDriver.findElement(usernameLocator).sendKeys(username);
        webDriver.findElement(passwordLocator).sendKeys(password);
        webDriver.findElement(loginButtonLocator).click();
    }

    private Set<String> collectPicLinks(JavascriptExecutor jse, int picLimit) throws InterruptedException {
        Set<String> picLinks = new HashSet<>();
        int index = 0;
        do {
            index = picLinks.size();
            if (index >= picLimit) {
                break;
            }
            final List<WebElement> pics = webDriver.findElements(postsLocator);
            final int minLimit = Math.min(picLimit, pics.size());
            for (int i = 0; i < minLimit; i++) {
                picLinks.add(pics.get(i).getAttribute("href"));
            }
            scrollBy(jse);
            Thread.sleep(3000l);
        } while (index < picLimit && (index != picLinks.size() || index == 0));

        return picLinks;
    }

    private HashMap<String, Integer> countLikes(JavascriptExecutor jse, Set<String> picLinks) {
        HashMap<String, Integer> likeCounter = new HashMap<>();
        String originalWindow = webDriver.getWindowHandle();
        for (String pLink : picLinks) {
            try {
                webDriver.switchTo().newWindow(WindowType.TAB);
                webDriver.get(pLink + "liked_by/");
                waitTo(profileLocator);
                scrollBy(jse);
                Thread.sleep(3000l);
                List<WebElement> likedUsers = webDriver.findElements(By.cssSelector("main [alt$='profile picture']"));
                for (WebElement u : likedUsers) {
                    String followerUsername = u.getAttribute("alt").replace("'s profile picture", "");
                    if (likeCounter.containsKey(followerUsername)) {
                        likeCounter.put(followerUsername, likeCounter.get(followerUsername) + 1);
                    } else {
                        likeCounter.put(followerUsername, 1);
                    }
                }
                webDriver.close();
                webDriver.switchTo().window(originalWindow);
            } catch (Exception e) {
                System.out.println("ERROR OCCURED FOR: " + pLink);
            }
        }

        return likeCounter;
    }

    private void scrollBy(JavascriptExecutor jse) {
        //jse.executeScript("window.scrollBy(0,document.body.style.zoom = 0.5)");
        jse.executeScript("window.scrollBy(0,document.body.scrollHeight)");
    }

    private void waitTo(By locator) {
        Wait<WebDriver> wait = new FluentWait<>(webDriver)
                .withTimeout(Duration.ofSeconds(15))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class);

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

}
