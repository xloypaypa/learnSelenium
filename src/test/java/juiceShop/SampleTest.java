package juiceShop;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleTest {

    private WebDriver driver;
    private final String baseUrl = "http://localhost:3000";
    private String emailAddress;

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("disable-gpu");
        driver = new ChromeDriver(chromeOptions);
        driver.get(baseUrl);
        // not fullscreen. because on the server. nobody knows the screen size. (or at least we should not rely on it)
        driver.manage().window().setSize(new Dimension(1920, 1080));

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        this.emailAddress = String.format("abc%s@abc.abc", timestamp);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void just_check_the_page_title_to_make_sure_at_least_there_is_a_test_case_can_pass() {
        assertEquals("OWASP Juice Shop", driver.getTitle());
    }

    @Test
    void register_flow() {
        // just skip the welcome by force. (by force means the xpath.)
        List<WebElement> elements = driver.findElements(By.xpath("//*[@id=\"mat-dialog-0\"]/app-welcome-banner/div/div[2]/button[2]"));
        assertEquals(1, elements.size());
        elements.get(0).click();

        // move to login page.
        driver.findElement(By.id("navbarAccount")).click();
        driver.findElement(By.id("navbarLoginButton")).click();

        // as a demo. ignore the url may different on different case (for example, time, maybe only allow to register on 9am-6pm. other time may jump to other page)
        // so in this case, Just check the url and title with string exact match
        assertEquals(baseUrl + "/#/login", driver.getCurrentUrl());

        WebElement loginForm = driver.findElement(By.id("login-form"));

        // jump to register page.
        loginForm.findElement(By.linkText("Not yet a customer?")).click();
        assertEquals(baseUrl + "/#/register", driver.getCurrentUrl());

        WebElement registrationForm = driver.findElement(By.id("registration-form"));
        // similar to login. check ui basic status
        WebElement emailControl = getAndCheckElementDisplayStatus(registrationForm,By.id("emailControl"), true, "");
        WebElement passwordControl = getAndCheckElementDisplayStatus(registrationForm,By.id("passwordControl"), true, "");
        WebElement repeatPasswordControl = getAndCheckElementDisplayStatus(registrationForm,By.id("repeatPasswordControl"), true, "");
        WebElement securityAnswerControl = getAndCheckElementDisplayStatus(registrationForm,By.id("securityAnswerControl"), true, "");
        WebElement securityQuestion = registrationForm.findElement(By.id("mat-select-2"));
        WebElement registerButton = getAndCheckElementDisplayStatus(registrationForm,By.id("registerButton"), false, "Register");

        // on purpose of testing user register and login journey, I skip some details cases like: check what is repeat password and the password are different
        // also skip the advice button test, in case it will never block the whole flow.
        emailControl.sendKeys(this.emailAddress);
        passwordControl.sendKeys("1qaz!QAZ");
        repeatPasswordControl.sendKeys("1qaz!QAZ");
        securityAnswerControl.sendKeys("1");
        securityQuestion.click();
        driver.findElement(By.id("mat-option-7")).click();

        // trigger register
        assertTrue(registerButton.isEnabled());
        registerButton.click();

        wait_one_sec();

        //should back to login page
        assertEquals(baseUrl + "/#/login", driver.getCurrentUrl());

        loginForm = driver.findElement(By.id("login-form"));
        // check init state.
        // for demo reason, only check the email, password and login button.
        // the Google button looks is always enabled. so only for demo and learn purpose, skip it.
        WebElement email = getAndCheckElementDisplayStatus(loginForm, By.id("email"), true, "");
        WebElement password = getAndCheckElementDisplayStatus(loginForm, By.id("password"), true, "");
        WebElement loginButton = getAndCheckElementDisplayStatus(loginForm, By.id("loginButton"), false, "Log in");
        email.sendKeys(this.emailAddress);
        password.sendKeys("1qaz!QAZ");

        // trigger login
        assertTrue(loginButton.isEnabled());
        loginButton.click();

        wait_one_sec();

        // check login successful or not
        assertEquals(baseUrl + "/#/search", driver.getCurrentUrl());
        driver.findElement(By.id("navbarAccount")).click();
        driver.findElement(By.xpath(String.format("//*[contains(text(), '%s')]", this.emailAddress)));
    }

    // extract a method or not extract is always be a problem.
    // extract the common part out can make code shorter and easier to read.
    // but extract need more context to understand the code.
    // here. in case it's a demo. the context is not huge, so extract.
    private static WebElement getAndCheckElementDisplayStatus(WebElement parent, By filter, boolean enabled, String textContains) {
        WebElement element = parent.findElement(filter);
        assertTrue(element.isDisplayed());
        assertEquals(enabled, element.isEnabled());
        assertTrue(element.getText().contains(textContains));
        return element;
    }

    private void wait_one_sec() {
        synchronized (this) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
