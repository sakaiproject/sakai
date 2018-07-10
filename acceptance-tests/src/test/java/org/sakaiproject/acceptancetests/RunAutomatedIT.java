package org.sakaiproject.acceptancetests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Runner for @automated tests
 */
@RunWith(Cucumber.class)
@CucumberOptions(tags = "@Automated", plugin = { "pretty", "json:target/cucumber-report.json" }, monochrome = true, strict = true)
public class RunAutomatedIT {

}
