package ui.auto.examples;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import ui.auto.core.context.PageComponentContext;

public class WebFormDynamicTest {
	PageComponentContext context;
    
	@BeforeTest
	public void init(){
		WebDriver driver=new FirefoxDriver();
		context=new PageComponentContext(driver);
		driver.get("http://goo.gl/gUqDHg");
	}
    
	@Test
	public void fillFormRandom() throws Exception{
		FormPageObject formPageObject=new FormPageObject();
		formPageObject.generateData();
		formPageObject.initPage(context);
		formPageObject.fillForm();
	}
	
	@AfterTest
	public void end(){
		context.getDriver().quit();
	}

}
