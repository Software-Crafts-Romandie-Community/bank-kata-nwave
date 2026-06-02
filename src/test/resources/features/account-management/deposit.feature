@driving_port @US-S1
Feature: Customer deposits money into their account
  As a banking customer
  I want to deposit money into my account via the banking interface
  So that my balance is updated immediately and I see a confirmation

  # Scenario 1 is the walking skeleton — already covered in walking-skeleton.feature

  @skip
  Scenario: A valid deposit increases the account balance
    Given a new bank account with no transactions
    When the customer deposits 150.00 euros
    Then the balance shown is 150.00 euros

  @skip
  Scenario: A deposit with a decimal amount updates the balance correctly
    Given a new bank account with no transactions
    When the customer deposits 50.50 euros
    Then the balance shown is 50.50 euros

  @skip
  Scenario: Several successive deposits accumulate correctly
    Given the customer has already deposited 200.00 euros
    When the customer deposits 50.00 euros
    Then the balance shown is 250.00 euros

  @skip @error
  Scenario: A deposit of zero is rejected and the balance stays unchanged
    Given a new bank account with no transactions
    When the customer attempts to deposit 0.00 euros
    Then the deposit is refused with an invalid amount message
    And the balance shown is 0.00 euros

  @skip @error
  Scenario: A negative deposit amount is rejected and the balance stays unchanged
    Given the customer has already deposited 100.00 euros
    When the customer attempts to deposit -10.00 euros
    Then the deposit is refused with an invalid amount message
    And the balance shown is 100.00 euros
