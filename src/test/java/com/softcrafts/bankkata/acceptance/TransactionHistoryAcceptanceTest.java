package com.softcrafts.bankkata.acceptance;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@Tag("acceptance")
@IncludeEngines("cucumber")
@SelectClasspathResource("features/transaction-history")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.softcrafts.bankkata.acceptance"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/cucumber-reports/transaction-history.html"
)
@ConfigurationParameter(
    key = Constants.FILTER_TAGS_PROPERTY_NAME,
    value = "not @skip"
)
public class TransactionHistoryAcceptanceTest {
    // Annotation-driven runner -- no body needed. Shares glue (and CucumberSpringConfiguration)
    // with AccountManagementAcceptanceTest -- see StatementSteps javadoc.
}
