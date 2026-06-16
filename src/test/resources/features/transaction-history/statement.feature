@driving_port @US-WS
Feature: Full transaction statement with backend pagination
  As a banking customer
  I want to consult my complete transaction statement, page by page
  So that I can verify every recorded operation without contacting my branch

  # The walking skeleton scenario lives in walking-skeleton.feature and is the only
  # active scenario in this slice. All scenarios below are enabled one at a time in DELIVER.

  @skip
  Scenario: The full statement is returned as a single coherent paginated object
    Given the customer has already deposited 100.00 euros
    And the customer has already deposited 50.00 euros
    When the customer opens the statement page
    Then the response "content" holds 2 transactions
    And each line shows the date, type and signed amount

  @skip
  Scenario: The statement is empty when no transaction exists
    Given a new bank account with no transactions
    When the customer opens the statement page
    Then the customer sees the message "Aucune transaction enregistree"
    And no error is displayed

  @skip
  Scenario: The amount and type shown match exactly the domain transaction
    Given the customer has already deposited 200.00 euros
    When the customer opens the statement page
    Then the customer sees a line "DEPOSIT" with amount "+200.00 EUR"
    And the date matches the in-memory transaction timestamp

  @skip
  Scenario: The full statement defaults to a page size of 20
    Given the customer has made 25 transactions in total
    When the customer opens the statement page
    Then the response "content" holds 20 transactions
    And "totalElements" is 25
    And "totalPages" is 2
    And "page" is 0

  @skip
  Scenario: Customer navigates to the next page of the statement
    Given the customer has made 25 transactions in total
    When the customer clicks "Next page"
    Then the response "content" holds 5 transactions
    And "page" is 1

  @skip
  Scenario: Customer selects an allowed page size
    Given the customer has made 12 transactions in total
    When the customer selects a page size of 10
    Then the response "content" holds 10 transactions
    And "totalPages" is 2

  @skip @error
  Scenario: An unsupported page size choice is rejected
    Given a new bank account with no transactions
    When the customer selects a page size of 37
    Then the page size choice is refused with an invalid parameter message

  @skip
  Scenario: A page beyond the last page returns a coherent empty result
    Given the customer has made 5 transactions in total
    When the customer requests page 99
    Then the response "content" is an empty array
    And "totalElements" is 5
    And "totalPages" is 1
    And "page" is 99
