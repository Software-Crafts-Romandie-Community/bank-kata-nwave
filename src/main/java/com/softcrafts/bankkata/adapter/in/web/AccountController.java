package com.softcrafts.bankkata.adapter.in.web;

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
 * Contains ZERO business logic — all rules enforced by AccountUseCase / Account domain.
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountUseCase accountUseCase;

    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        return ResponseEntity.ok(new BalanceResponse(accountUseCase.getBalance()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(new BalanceResponse(accountUseCase.deposit(request.amount())));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(new BalanceResponse(accountUseCase.withdraw(request.amount())));
    }
}
