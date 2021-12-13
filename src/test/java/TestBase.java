package com.saucelabs.Tests;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.UnexpectedException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;

public class TestBase  {
	
	/*********************************************************************/

    public static boolean IS_FAIL = false; 						// Fail IE tests for Failure Analysis 

    public static String MANUAL_BUILD = "jenkins-MyJenkinsDemo-984"; 	// When running manually 

	/*********************************************************************/

    public String buildTag = System.getenv("BUILD_TAG");

    public String username = System.getenv("SAUCE_USERNAME");

    public String accesskey = System.getenv("SAUCE_ACCESS_KEY");
    
	public String SAUCE_URL = "http://" + username + ":" + accesskey + "@ondemand.saucelabs.com:80/wd/hub";
	
	public static final String EDGE = "MicrosoftEdge", 
							   IE = "internet explorer",
							   FIREFOX = "firefox",
							   CHROME = "chrome",
							   SAFARI = "safari";
	
	public static final String MACOS = "macOS 10.15",
							   WIN10 = "Windows 10";
	
	public static final String LATEST = "latest",
							   PERF = "perf";
	
	/*********************************************************************/

	private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

	/*********************************************************************/

    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
    	
        return new Object[][]{
        	
            new Object[]{PERF, LATEST, MACOS},
            new Object[]{PERF, LATEST, WIN10},

            new Object[]{EDGE, LATEST, WIN10},
            new Object[]{IE, LATEST, WIN10},
            new Object[]{FIREFOX, LATEST, WIN10},
            new Object[]{CHROME, LATEST, WIN10},

            new Object[]{SAFARI, LATEST, MACOS},
            new Object[]{EDGE, LATEST, MACOS},
            new Object[]{FIREFOX, LATEST, MACOS},
            new Object[]{CHROME, LATEST, MACOS}

        };
        
    }

	/*********************************************************************/

    public WebDriver getWebDriver() {
        return webDriver.get();
    }

	/*********************************************************************/
    
    public String getSessionId() {
        return sessionId.get();
    }

	/*********************************************************************/

    protected void createDriver(String browser, String version, String os, String methodName) throws MalformedURLException, UnexpectedException {
    	
        DesiredCapabilities capabilities = new DesiredCapabilities();

        if (browser.equals("perf")) {
            capabilities.setCapability("capturePerformance", true);
            browser = "chrome";
            methodName = "Performance Test";
        } 

        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        capabilities.setCapability(CapabilityType.VERSION, version);
        capabilities.setCapability(CapabilityType.PLATFORM, os);
        capabilities.setCapability("name", methodName);
        
        capabilities.setCapability("extendedDebugging", true);
        
        if (buildTag == null) {
        	buildTag = MANUAL_BUILD;
        }

        if (buildTag != null) {
            capabilities.setCapability("build", buildTag);
        }

        webDriver.set(new RemoteWebDriver(new URL(SAUCE_URL),capabilities));

        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);
        
    }

	/*********************************************************************/

    @AfterMethod
    public void tearDown(ITestResult result) throws Exception {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
        webDriver.get().quit();
    }

	/*********************************************************************/

    protected void annotate(String text) {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:context=" + text);
    }
	
}
