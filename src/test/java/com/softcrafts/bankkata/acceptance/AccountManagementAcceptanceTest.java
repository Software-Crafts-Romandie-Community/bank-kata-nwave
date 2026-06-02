package com.softcrafts.bankkata.acceptance;

// SCAFFOLD: true

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber JUnit Platform runner for the account-management acceptance suite.
 *
 * Feature files: src/test/resources/features/account-management/
 * Step definitions: com.softcrafts.bankkata.acceptance.steps
 * Spring configuration: com.softcrafts.bankkata.acceptance.config
 *
 * Walking skeleton scenario is the only active scenario.
 * All others carry @skip in their Gherkin tag — enabled one at a time in DELIVER.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/account-management")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.softcrafts.bankkata.acceptance"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/cucumber-reports/account-management.html"
)
@ConfigurationParameter(
    key = Constants.FILTER_TAGS_PROPERTY_NAME,
    value = "not @skip"
)
public class AccountManagementAcceptanceTest {
    // Annotation-driven runner — no body needed.
}
