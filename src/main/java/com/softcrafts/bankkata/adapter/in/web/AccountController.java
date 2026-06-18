package com.softcrafts.bankkata.adapter.in.web;

import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import com.softcrafts.bankkata.domain.InsufficientFundsException;
import com.softcrafts.bankkata.domain.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP driving adapter — AccountController.
 *
 * Endpoints:
 * - GET  /api/balance    → 200 BalanceResponse
 * - GET  /api/statement  → 200 List<TransactionDto>
 * - POST /api/deposit    → 200 BalanceResponse | 400 ProblemDetail (invalid amount)
 * - POST /api/withdraw   → 200 BalanceResponse | 400 ProblemDetail (invalid amount)
 *                                               | 409 ProblemDetail (insufficient funds)
 *
 * Contains ZERO business logic — all rules enforced by AccountUseCase / Account domain.
 * Transaction → TransactionDto mapping is done exclusively in this controller.
 * Exception mapping: IllegalArgumentException → 400, InsufficientFundsException → 409.
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

    @GetMapping("/statement")
    public ResponseEntity<List<TransactionDto>> getStatement() {
        List<TransactionDto> dtos = accountUseCase.getStatement().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        return ResponseEntity.ok(new BalanceResponse(accountUseCase.deposit(request.amount())));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(new BalanceResponse(accountUseCase.withdraw(request.amount())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAmount(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid amount");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientFunds(InsufficientFundsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Insufficient funds");
        problem.setProperty("available", exception.getAvailableBalance());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.type().name(),
                transaction.amount(),
                transaction.timestamp().toString()
        );
    }
}
