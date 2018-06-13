package org.sakaiproject.acceptancetests.hooks;

import org.sakaiproject.acceptancetests.collaborator.WebDriverManager;

import cucumber.api.Scenario;

public class AutomatedHooks {



	// @Before("@automated")
	public void beforeScenario() {

		// this.driver = new SakaiWebDriver();

		System.out.println("This will run before the Scenario");
	}

	// @After("@automated")
	public void afterScenario(final Scenario result) {
		// embedScreenshot(result);
		WebDriverManager.closeDriver();
	}

	/*
	 * private void embedScreenshot(final Scenario result) { try { final byte[] screenshot = this.driver.getScreenshotAs(OutputType.BYTES);
	 * result.embed(screenshot, "image/png"); } catch (final UnsupportedOperationException somePlatformsDontSupportScreenshots) {
	 * System.err.println(somePlatformsDontSupportScreenshots.getMessage()); } catch (final WebDriverException e) {
	 * result.write("WARNING. Failed take screenshots with exception:" + e.getMessage()); } }
	 */


}

