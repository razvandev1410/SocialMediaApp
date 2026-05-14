package com.example.demo.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SocialMediaE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:5173";

    private static final String TEST_USERNAME = "e2e_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_EMAIL = TEST_USERNAME + "@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PHONE = "+4070" + System.currentTimeMillis() % 10000000;

    private static String createdPostTitle;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        options.setExperimentalOption("prefs", java.util.Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false
        ));

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


    private void logout() {
        driver.get(BASE_URL + "/login");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.localStorage.clear();");
        js.executeScript("window.sessionStorage.clear();");
        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
    }

    private void loginAsTestUser() {
        logout();

        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passField = driver.findElement(By.id("password"));

        userField.clear();
        passField.clear();

        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        userField.sendKeys(TEST_USERNAME);
        passField.sendKeys(TEST_PASSWORD);

        String typedUser = userField.getAttribute("value");
        String typedPass = passField.getAttribute("value");

        driver.findElement(By.tagName("body")).click();

        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        submitBtn.click();

        try {
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        } catch (TimeoutException e) {
            List<WebElement> alerts = driver.findElements(By.cssSelector(".alert, .text-danger, .error-message"));
            if (!alerts.isEmpty()) {
                System.err.println("BACKEND-UL A RESPINS LOGAREA: " + alerts.get(0).getText());
            }
            throw e;
        }
    }

    @Test
    @Order(1)
    void testRegister() {
        driver.get(BASE_URL + "/register");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        driver.findElement(By.id("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.id("email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("phone")).sendKeys(TEST_PHONE);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // App does login(user) then navigate('/') after register
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        assertTrue(driver.getCurrentUrl().endsWith("/"));
    }


    @Test
    @Order(2)
    void testLoggedInAfterRegister() {
        // should still be on home page, logged in from register
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("navbar")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains(TEST_USERNAME));
    }

    @Test
    @Order(3)
    void testCreatePost() {
        driver.get(BASE_URL + "/create-post");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        createdPostTitle = "E2E Test Post " + System.currentTimeMillis();
        driver.findElement(By.id("title")).sendKeys(createdPostTitle);
        driver.findElement(By.id("text")).sendKeys("This is an automated E2E test post content.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // After create, navigates to /
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        String pageSource = driver.getPageSource();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), createdPostTitle));
    }

    @Test
    @Order(4)
    void testViewPostDetail() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));

        WebElement postTitleLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card h5.card-title"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", postTitleLink);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", postTitleLink);

        // Should navigate to /posts/{id}
        wait.until(ExpectedConditions.urlContains("/posts/"));
        assertTrue(driver.getCurrentUrl().contains("/posts/"));
    }

    @Test
    @Order(5)
    void testAddComment() {
        driver.get(BASE_URL + "/");

        WebElement postTitleLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card h5.card-title"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", postTitleLink);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", postTitleLink);

        wait.until(ExpectedConditions.urlContains("/posts/"));

        WebElement commentInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea[placeholder*='comment']"))
        );

        String commentText = "E2E test comment " + System.currentTimeMillis();
        commentInput.sendKeys(commentText);

        WebElement submitBtn = driver.findElement(By.xpath("//button[contains(text(),'Post Comment')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), commentText));
        assertTrue(driver.getPageSource().contains(commentText));
    }

    @Test
    @Order(6)
    void testVoteButtonsPresent() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));

        List<WebElement> voteButtons = driver.findElements(By.cssSelector(".btn-outline-secondary, .btn-primary"));
        assertFalse(voteButtons.isEmpty(), "Vote buttons should be present on posts");
    }


    @Test
    @Order(7)
    void testLoginWrongPassword() {
        logout();

        driver.findElement(By.id("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.id("password")).sendKeys("wrongpassword123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Should show error alert
        WebElement alert = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger"))
        );
        assertNotNull(alert);
        assertTrue(alert.getText().length() > 0);

        // Should still be on login page
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }


    @Test
    @Order(8)
    void testRegisterDuplicateUsername() {
        driver.get(BASE_URL + "/register");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        driver.findElement(By.id("username")).sendKeys(TEST_USERNAME); // already exists
        driver.findElement(By.id("email")).sendKeys("dup_" + TEST_EMAIL);
        driver.findElement(By.id("phone")).sendKeys("+4071" + System.currentTimeMillis() % 10000000);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Should show error
        WebElement alert = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger"))
        );
        assertTrue(alert.getText().length() > 0);
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }

    @Test
    @Order(9)
    void testProtectedRouteRedirect() {
        driver.get(BASE_URL + "/login");
        ((JavascriptExecutor) driver).executeScript("localStorage.clear()");

        driver.get(BASE_URL + "/create-post");

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(10)
    void testSearchPosts() {
        String searchUser = "search_" + UUID.randomUUID().toString().substring(0, 8);
        String searchEmail = searchUser + "@test.com";
        String searchPhone = "+4072" + System.currentTimeMillis() % 10000000;

        logout();

        driver.get(BASE_URL + "/register");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        driver.findElement(By.id("username")).sendKeys(searchUser);
        driver.findElement(By.id("email")).sendKeys(searchEmail);
        driver.findElement(By.id("phone")).sendKeys(searchPhone);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);

        WebElement regBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", regBtn);

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder*='earch']"))
        );
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys("E2E Test Post");

        WebElement searchBtn = driver.findElement(By.xpath("//button[contains(text(),'Search')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);

        // lasam backend-ul sa raspunda
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        boolean isFound = driver.getPageSource().contains("E2E Test Post");
        assertTrue(isFound, "Couldn't find post!");
    }
}