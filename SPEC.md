# Bank Application - Vision & Specification

## Overview

The Bank Application is a standard web banking application that allows customers to manage their bank account through a browser interface. The application is built incrementally using a feature-driven approach, delivering end-to-end slices of value to real users.

## Core Features (Phased Delivery)

### Phase 1: Account Management — Web UI (Current)

- **Display Account Balance**: Customers can view their current balance in a browser
- **Accept Deposits**: Customers can deposit money via a web form
- **Accept Withdrawals**: Customers can withdraw money via a web form, with rejection on insufficient funds
- **Balance Tracking**: System maintains accurate balance after each transaction
- **REST API**: Backend exposes a REST API consumed by the frontend

### Phase 2: Transaction History (Future)

- Account statement showing all transactions with dates
- Filter transactions by date range
- View transaction details (amount, type, date)

### Phase 3: Interest & Advanced Features (Future)

- Calculate and apply interest
- Multiple account types
- Transaction fees

## Design Principles

- **Incremental Development**: Build one slice at a time, keeping features independent and testable
- **User-Centric**: Features deliver value to banking customers, not just technical correctness
- **Test-Driven**: Each feature is validated with acceptance tests before implementation
- **Separation of Concerns**: Domain logic is isolated from web infrastructure (Hexagonal Architecture)

## Current Implementation Focus

For Phase 1, the solution should:

1. Expose a REST API for:
   - `GET /api/balance` — retrieve current account balance
   - `POST /api/deposit` — deposit an amount
   - `POST /api/withdraw` — withdraw an amount (rejects if insufficient funds)

2. Provide a web frontend (HTML + JavaScript) that:
   - Displays the current balance on page load
   - Offers deposit and withdrawal forms
   - Shows confirmation or error messages after each operation

3. Maintain account state correctly:
   - Prevent negative balances
   - Accumulate transactions accurately in memory (Phase 1 — no database)

4. Provide clear feedback to the user:
   - Confirmation of successful transactions with updated balance
   - Error messages for invalid operations (insufficient funds, invalid amount)

## Success Criteria

- ✅ Account balance displayed correctly in the browser
- ✅ Deposits increase balance (visible in UI after operation)
- ✅ Withdrawals decrease balance (visible in UI after operation)
- ✅ Insufficient funds rejected with clear error message
- ✅ All operations logged in memory for future statement generation (Phase 2)
- ✅ REST API testable independently of the frontend
- ✅ Domain logic (rules) testable independently of the REST API
