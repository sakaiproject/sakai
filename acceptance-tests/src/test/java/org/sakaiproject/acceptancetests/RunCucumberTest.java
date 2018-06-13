package org.sakaiproject.acceptancetests;

import org.junit.runner.RunWith;

/**
 * Standard test runner for generating missing step defs.
 *
 * Run via 'mvn clean verify'
 */
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"})
public class RunCucumberTest {
}