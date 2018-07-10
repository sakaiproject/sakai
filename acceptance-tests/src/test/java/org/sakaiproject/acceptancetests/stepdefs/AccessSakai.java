package org.sakaiproject.acceptancetests.stepdefs;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sakaiproject.acceptancetests.collaborator.WebDriverManager;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class AccessSakai {

	WebDriver driver;

	@Given("^I am on the \"([^\"]*)\" page$")
	public void i_am_on_the_page(final String url) {
		this.driver = WebDriverManager.getDriver();
		this.driver.navigate().to(url);
	}

	@When("^I enter \"([^\"]*)\" for username$")
	public void i_enter_for_username(final String username) {
		new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#eid"))).sendKeys(username);
	}

	@When("^I enter \"([^\"]*)\" for password$")
	public void i_enter_for_password(final String password) {
		new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#pw"))).sendKeys(password);
	}

	@When("^I click the \"([^\"]*)\" button$")
	public void i_click_the_button(final String buttonId) {
		new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#" + buttonId))).click();
	}

	@Then("^I should see \"([^\"]*)\" site$")
	public void i_should_see_site(final String siteName) {
		Assert.assertTrue(StringUtils.contains(this.driver.getTitle(), siteName));
	}

	@Then("^I am logged in$")
	public void i_am_logged_in() {
		new WebDriverWait(this.driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
		final WebElement body = this.driver.findElement(By.tagName("body"));
		Assert.assertEquals("Mrphs-portalBody workspace", StringUtils.trim(body.getAttribute("class")));
	}

	@Given("^I am logged out$")
	public void i_am_logged_out() {

	}

}
