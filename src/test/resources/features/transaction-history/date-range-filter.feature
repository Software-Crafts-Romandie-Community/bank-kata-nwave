@driving_port @US-S1
Feature: Date range filter on the transaction statement
  As a banking customer
  I want to filter my statement by date range
  So that I can find a specific operation without scrolling through the entire history

  # Without an active filter, the default behaviour (page 0, size 20, sort date desc) is
  # shared with statement.feature -- no scenario is duplicated here.
  #
  # Out of scope (frontend-only, no backend driving port -- see
  # docs/feature/phase2-transaction-history/distill/upstream-issues.md, Finding 2):
  # "A date filter with from after to is rejected before any request is sent" is covered by
  # a Vitest test in DELIVER, not by this Cucumber suite.

  @skip
  Scenario: The date filter restricts the statement to the requested period
    Given the customer has a transaction on "2026-06-01"
    And the customer has a transaction on "2026-06-10"
    And the customer has a transaction on "2026-06-15"
    When the customer filters from "2026-06-01" to "2026-06-12"
    Then the customer sees 2 transactions dated "2026-06-01" and "2026-06-10"
    And the transaction dated "2026-06-15" does not appear

  @skip
  Scenario: A filter with no results shows an explicit empty state
    Given the customer has a transaction on "2026-06-05"
    When the customer filters from "2026-01-01" to "2026-01-31"
    Then the customer sees the message "Aucune transaction sur cette periode"
    And no error is displayed

  @skip
  Scenario: The date range bounds are inclusive
    Given the customer has a transaction on "2026-06-01"
    When the customer filters from "2026-06-01" to "2026-06-01"
    Then the transaction dated "2026-06-01" appears in the result

  @skip @error
  Scenario: A filter with an end date before the start date is rejected
    Given a new bank account with no transactions
    When the customer filters from "2026-06-15" to "2026-06-01"
    Then the filter is refused with an invalid date range message

  @skip
  Scenario: The date filter total matches the filtered subset, not the full account
    Given the customer has 7 transactions outside the period "2026-06-01" to "2026-06-12"
    And the customer has 3 transactions inside the period "2026-06-01" to "2026-06-12"
    When the customer filters from "2026-06-01" to "2026-06-12"
    Then "totalElements" is 3
    And "totalPages" is 1

  @skip
  Scenario: Ascending amount sort applies to a date-filtered statement
    Given the customer has transactions of 50.00, 200.00 and 10.00 euros between "2026-06-01" and "2026-06-12"
    When the customer filters that period and sorts by amount ascending
    Then "content" lists the transactions in the order 10.00, 50.00, 200.00 euros

  @skip
  Scenario: Descending amount sort applies to a date-filtered statement
    Given the customer has transactions of 50.00, 200.00 and 10.00 euros between "2026-06-01" and "2026-06-12"
    When the customer filters that period and sorts by amount descending
    Then "content" lists the transactions in the order 200.00, 50.00, 10.00 euros

  @skip @error
  Scenario: An invalid date range is rejected even when pagination and sort parameters are present
    Given a new bank account with no transactions
    When the customer filters from "2026-06-15" to "2026-06-01" with pagination and sorting parameters set
    Then the filter is refused with an invalid date range message
