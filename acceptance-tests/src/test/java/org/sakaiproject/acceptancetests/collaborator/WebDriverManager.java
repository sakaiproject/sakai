package org.sakaiproject.acceptancetests.collaborator;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebDriverManager {

	private static WebDriver driver;

	public static WebDriver getDriver() {
		if (driver == null) {
			driver = createDriver();
		}
		return driver;
	}

	private static WebDriver createDriver() {
		switch (getSeleniumProfile()) {
			// case "chrome":
			// driver = new ChromeDriver();
			// break;
			// case "phantom-js":
			// driver = getPhantomJsDriver();
			// break;
			// case "selenium-grid":
			// driver = getSeleniumGridRemoteDriver();
			// break;
			default:
				driver = new FirefoxDriver();
				break;
		}

		return driver;
	}

	public static void closeDriver() {
		driver.close();
		driver.quit();
	}

	private static String getSeleniumProfile() {
		final String seleniumProfile;
		if (StringUtils.isNotEmpty(System.getProperty("selenium.profile"))) {
			seleniumProfile = System.getProperty("selenium.profile");
		} else {
			seleniumProfile = "";
		}
		return seleniumProfile;
	}
}


