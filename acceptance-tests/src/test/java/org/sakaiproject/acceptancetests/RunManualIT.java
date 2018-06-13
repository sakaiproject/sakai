package org.sakaiproject.acceptancetests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Runner for manual tests.
 * Tag all tests
 */
@RunWith(Cucumber.class)
@CucumberOptions(tags = { "@manual" }, plugin = { "pretty", "html:target/cukes", "usage:target/usage.json",
		"json:target/cucumber-report.json" }, monochrome = true, strict = true, dryRun = true)
public class RunManualIT {
}
