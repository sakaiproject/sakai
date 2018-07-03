package org.sakaiproject.acceptancetests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Runner for @manual tests
 */
@RunWith(Cucumber.class)
@CucumberOptions(tags = "@manual", plugin = { "pretty",
		"json:target/cucumber-report-manual.json" }, monochrome = true, strict = true, dryRun = true)
public class RunManualIT {
}
