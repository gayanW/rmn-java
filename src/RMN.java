import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import junit.framework.AssertionFailedError;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;





public class RMN {

	/**
	 * @param args
	 */
	
	static FirefoxDriver driver;
	static WebDriverWait wait;
	
	static String parentHandle;
	static WebElement userFile;
	static WebElement uploadBtn;
	
	static String userHomeFolder;
	
	static Document xmlDoc;
	
	// required xml data
	static String code;
	static String site;
	static String description;
	static String startDate;
	static String expiryDate;
	
	static String couponln;
	
	// output file
	static File outputFile;
	static String file_name = "output_";
	static String extention = ".csv";
	static int index = 0;
	
	static int MAX_FILE_SIZE = 300 * 128; /* 1 Kilobit = 128 Byte */ 
	static int numOfFilesCreated;
		
	static PrintWriter writer;
	
	// icodes request urls
	static String requestParam;
	static String fullRequestParam = "&RequestType=Codes&Action=Full";
	static String newCodesParam = "&RequestType=Codes&Action=New&Sort=StartDateHigh&PageSize=50";
	
	// variables used to control the programs flow
	static int REQUEST_TYPE;
	static int MAX_WAIT;
	
	static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		// conditioning the app
		System.out.print("RequestType: [FULL 0] [NEW 1] > ");
		int requestType = scanner.nextInt();
		conditioningApp(requestType, 40);
				
		// create output file
		initOutputFile();
		
		System.out.println("Downloading XML Data..");
		
		try {
			// download xml data
			URL url = new URL("http://webservices.icodes-us.com/ws2_us.php"
					+ "?UserName=isiblean&SubscriptionID=876f1f9954de0aa402d91bb988d12cd4" 
					+ requestParam);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = dBuilder.parse(url.openStream());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		parseXML();
		
		driver = new FirefoxDriver();
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		wait = new WebDriverWait(driver, 40); // wait up to 40 seconds for a condition
					
		String baseUrl = "http://www.retailmenot.com/community/login/";
		
		// log in
		System.out.println("Log in..");
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
		
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.name("userfile")));
		}
		catch (Exception e) {
			driver.quit();
			System.exit(1);
		}
		
		try {
			Assert.assertEquals(driver.getTitle(), "reversiblean's Community Profile - RetailMeNot.com");
		}
		catch (AssertionFailedError e) {
			driver.quit();
			System.exit(1);
		}
		
		parentHandle = driver.getWindowHandle();
		
		// upload coupon data 
		userFile = driver.findElement(By.name("userfile"));
		
		uploadFiles(numOfFilesCreated);
		
		// shutdown WebDriver
		System.out.println("Shutdown WebDriver");
		driver.quit();
		

	}
	
	public static void parseXML()
	{
		System.out.println("Parsing XML..");
		
		NodeList itemsList = xmlDoc.getElementsByTagName("item");
		
		// iterate through all the items
		for (int i = 0; i < itemsList.getLength(); ++i)
		{
			Node item = itemsList.item(i);
			
			Element element = (Element) item;
			
			description = element.getElementsByTagName("description").item(0).getTextContent();
			code = element.getElementsByTagName("voucher_code").item(0).getTextContent();
			startDate = element.getElementsByTagName("start_date").item(0).getTextContent();
			expiryDate = element.getElementsByTagName("expiry_date").item(0).getTextContent();
			site = element.getElementsByTagName("merchant_url").item(0).getTextContent();
			
			// tranquate start and expiry date
			startDate = startDate.substring(0, 10);
			expiryDate = expiryDate.substring(0, 10);
						
			// tranquate site url
			try {
				URL siteUrl = new URL(site);
				site = siteUrl.getHost();
				site = site.replace("www.", "");
				
				alterKnownUrls();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			couponln = code + "," + site + ",\"" + description + "\"," + startDate + "," + expiryDate;
			
			// write coupon data into the file
			writer.println(couponln);
			writer.flush();
			
			if (outputFile.length() >= MAX_FILE_SIZE)
				createNewOutputFile();
		}
		
		writer.close();
		
		numOfFilesCreated = index + 1;
		System.out.println("Data has been written to " + numOfFilesCreated + " file(s) in the user's home directory.");
		
		
	}
	
	public static void uploadFiles(int numberOfFiles)
	{
		for (int i = 0; i < numberOfFiles; ++i)
		{
			userFile.sendKeys(userHomeFolder + "\\" + file_name + Integer.toString(i) + extention);
			
			WebElement uploadBtn = driver.findElement(By.xpath("(//button[@type='submit'])[2]"));
			uploadBtn.click();
			
			System.out.println("Uploading " + file_name + i + extention);
			
			// switch focus of WebDriver to newly opened window
			for (String newWinHandle : driver.getWindowHandles()) {
				driver.switchTo().window(newWinHandle);
			}
			
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h4")));
			}
			catch (Exception e) {
				driver.quit();
				System.exit(1);
			}
			
			try {
				Assert.assertEquals(driver.findElementByCssSelector("h4").getText(), "Done.");
				driver.close();
			}
			catch (AssertionFailedError e) {
				driver.quit();
				System.exit(1);
			}
			
			
			System.out.println("Upload Complete.");
			
			//driver.close(); // close the current window
			driver.switchTo().window(parentHandle);
		}
	}
	
	public static void initOutputFile()
	{
		userHomeFolder = System.getProperty("user.home");
		outputFile = new File(userHomeFolder, file_name + index + extention);
		
		try {
			writer = new PrintWriter(outputFile, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// write the header data
		writer.println("Code,Site,Description,StartDate,Expires");
	}
	
	public static void createNewOutputFile()
	{
		writer.close();
		
		++index;
		outputFile = new File(userHomeFolder, file_name + index + extention);
		
		try {
			writer = new PrintWriter(outputFile, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// write the header data
		writer.println("Code,Site,Description,StartDate,Expires");
	}
	
	public static void alterKnownUrls()
	{
		if (site.equals("shop.usa.canon.com"))
			site = "canon.com";
		
	}
	
	public static void conditioningApp(int requestType, int maxWait)
	{
		REQUEST_TYPE = requestType;
		MAX_WAIT = maxWait;
		
		if (REQUEST_TYPE == RequestType.FULL) requestParam = fullRequestParam;
		else if (REQUEST_TYPE == RequestType.NEW) requestParam = newCodesParam;
	}

}
