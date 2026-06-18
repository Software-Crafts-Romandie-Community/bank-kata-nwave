package com.softcrafts.bankkata.acceptance.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cucumber step definitions for the account-statement feature.
 *
 * Tier A acceptance tests — production Spring context via MockMvc.
 * Step methods delegate to the production composition root (AccountUseCase via HTTP).
 * Zero business logic in this class.
 *
 * Walking skeleton is the ONLY enabled scenario.
 * All statement-api scenarios are @skip — enabled one at a time in DELIVER.
 *
 * Shared Given steps (a new bank account, customer deposited) live in AccountManagementSteps.
 * @Before reset is also in AccountManagementSteps and applies to all scenarios.
 */
public class AccountStatementSteps {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions statementResult;

    // -------------------------------------------------------------------------
    // Given steps
    // -------------------------------------------------------------------------

    @Given("the customer has already withdrawn {double} euros")
    public void theCustomerHasAlreadyWithdrawn(double amount) throws Exception {
        mockMvc.perform(post("/api/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // When steps
    // -------------------------------------------------------------------------

    @When("the customer requests the account statement")
    public void theCustomerRequestsTheAccountStatement() throws Exception {
        statementResult = mockMvc.perform(get("/api/statement")
                .accept(MediaType.APPLICATION_JSON));
    }

    // -------------------------------------------------------------------------
    // Then steps
    // -------------------------------------------------------------------------

    @Then("the response status is 200 OK")
    public void theResponseStatusIs200OK() throws Exception {
        statementResult.andExpect(status().isOk());
    }

    @Then("^the statement contains (\\d+) transactions?$")
    public void theStatementContainsTransactions(int count) throws Exception {
        statementResult.andExpect(jsonPath("$.length()").value(count));
    }

    @Then("the statement is empty")
    public void theStatementIsEmpty() throws Exception {
        statementResult.andExpect(jsonPath("$.length()").value(0));
    }

    @Then("each transaction in the statement has type, amount, and date fields")
    public void eachTransactionHasTypeAmountAndDateFields() throws Exception {
        statementResult
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].date").exists());
    }

    @Then("the date field of the first transaction is a non-empty string")
    public void theDateFieldOfFirstTransactionIsANonEmptyString() throws Exception {
        statementResult
                .andExpect(jsonPath("$[0].date").isString())
                .andExpect(jsonPath("$[0].date").isNotEmpty());
    }

    @Then("the statement entry at position {int} has type {string} and amount {double}")
    public void theStatementEntryAtPositionHasTypeAndAmount(int position, String type, double amount) throws Exception {
        statementResult
                .andExpect(jsonPath("$[" + position + "].type").value(type))
                .andExpect(jsonPath("$[" + position + "].amount").value(BigDecimal.valueOf(amount)));
    }
}
