@walking_skeleton @real-io @driving_port @US-WS
Feature: Transaction statement display via the banking interface
  As a banking customer
  I want to view a chronological statement of my past transactions
  So that I can trust the system's bookkeeping without contacting my branch

  Scenario: Customer views the full transaction statement sorted from most recent to oldest
    Given the customer has deposited 50.00 euros on "2026-06-01"
    And the customer has withdrawn 50.00 euros on "2026-06-10"
    And the customer has deposited 150.00 euros on "2026-06-15"
    When the customer opens the statement page
    Then the statement shows 3 transactions ordered from "2026-06-15" to "2026-06-01"
    And each line shows the date, type and signed amount
