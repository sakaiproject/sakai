package org.sakaiproject.acceptancetests;

import org.junit.runner.RunWith;

/**
 * Standard test runner for generating missing step defs.
 *
 * Run via 'mvn clean verify'
 */
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Run this via `mvn test` to generate missing cucumber stepdefs for all feature files
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", dryRun = true)
public class RunCucumberTest {
}