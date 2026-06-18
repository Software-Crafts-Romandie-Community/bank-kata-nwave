@walking_skeleton @real-io @driving_port @US-WS2
Feature: Account statement — end-to-end API wiring
  As a banking customer
  I want to request my account statement via GET /api/statement
  So that I can verify my transaction history end-to-end

  Scenario: The customer retrieves a non-empty statement after making a deposit
    Given a new bank account with no transactions
    And the customer has already deposited 200.00 euros
    When the customer requests the account statement
    Then the response status is 200 OK
    And the statement contains 1 transaction
