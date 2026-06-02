package com.softcrafts.bankkata.adapter.in.web;

// SCAFFOLD: true
// Driving port HTTP — @RestController translating REST calls to AccountUseCase.
// Zero business logic. All validation is delegated to the domain via AccountUseCase.
// Translates domain exceptions (InsufficientFundsException) to HTTP status codes.

import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP driving adapter — AccountController.
 *
 * Endpoints:
 * - GET  /api/balance  → 200 BalanceResponse
 * - POST /api/deposit  → 200 BalanceResponse | 400 Problem Details (invalid amount)
 * - POST /api/withdraw → 200 BalanceResponse | 400 Problem Details (invalid amount)
 *                                             | 409 Problem Details (insufficient funds)
 *
 * Error responses follow RFC 7807 Problem Details format.
 * InsufficientFundsException is handled here (or via @ControllerAdvice — DISTILL Q2 resolved
 * to inline @ExceptionHandler for simplicity in Phase 1).
 *
 * Contains ZERO business logic — all rules enforced by AccountUseCase / Account domain.
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    // SCAFFOLD: true
    private final AccountUseCase accountUseCase;

    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
