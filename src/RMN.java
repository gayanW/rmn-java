import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;



public class RMN {

	/**
	 * @param args
	 */
	
	static FirefoxDriver driver;
	static WebDriverWait wait;
	
	static String parentHandle;
	static WebElement userFile;
	static WebElement uploadBtn;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		driver = new FirefoxDriver();
		wait = new WebDriverWait(driver, 20); // wait up to 20 seconds for a condition
		String desktopPath = System.getProperty("user.home") + "\\Desktop";
		System.out.println(desktopPath);
		
		
		String baseUrl = "http://www.retailmenot.com/community/login/";
		
		// log in
		driver.get(baseUrl);
		Assert.assertEquals(driver.getTitle(), "Log in");
		
		WebElement email = driver.findElement(By.name("email"));
		email.clear();
	    email.sendKeys("reversiblean@live.com");
	    
		WebElement password = driver.findElement(By.name("password"));
		password.clear();
		password.sendKeys("SecretOfKells7");
		
		WebElement loginBtn = driver.findElement(By.xpath("(//button[@type='submit'])[2]"));
		loginBtn.click();
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("userfile")));
		Assert.assertEquals(driver.getTitle(), "reversiblean's Community Profile - RetailMeNot.com");
		
		parentHandle = driver.getWindowHandle();
		
		// upload coupon data 
		userFile = driver.findElement(By.name("userfile"));
		
		userFile.sendKeys(desktopPath + "\\sample.csv");
		
		WebElement uploadBtn = driver.findElement(By.xpath("(//button[@type='submit'])[2]"));
		uploadBtn.click();
		
		
		
		// switch focus of WebDriver to newly opened window
		for (String newWinHandle : driver.getWindowHandles()) {
			driver.switchTo().window(newWinHandle);
		}
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h4")));
		Assert.assertEquals(driver.findElementByCssSelector("h4").getText(), "Done.");
		
		driver.close(); // close the current window
		driver.switchTo().window(parentHandle);
		userFile.sendKeys(desktopPath + "\\sample1.csv");
		//driver.quit();	// shutdown the WebDriver
		

	}

}
