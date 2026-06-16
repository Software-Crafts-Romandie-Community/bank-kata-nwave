package com.softcrafts.bankkata.adapter.in.web;

// SCAFFOLD: true -- created by DISTILL (phase2-transaction-history). RED until DELIVER.

import com.softcrafts.bankkata.application.port.in.StatementUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP driving adapter -- StatementController.
 *
 * Endpoint:
 * - GET /api/statement?from&to&page&size&sortBy&sortDir
 *     -> 200 PageResponse&lt;TransactionResponse&gt;
 *     -> 400 ProblemDetail (RFC 7807) -- from &gt; to, unparsable date, page &lt; 0,
 *        size/sortBy/sortDir outside their whitelist (D10-D13, ADR-005)
 *
 * Dedicated controller (D1) -- AccountController is never touched (additive extension
 * constraint, confirmed Q5 architecture brief). Contains ZERO business logic -- delegates
 * to StatementUseCase. Exception mapping (InvalidDateRangeException -> 400, etc.) is added
 * by the crafter in DELIVER alongside the real implementation.
 */
@RestController
@RequestMapping("/api")
public class StatementController {

    private final StatementUseCase statementUseCase;

    public StatementController(StatementUseCase statementUseCase) {
        this.statementUseCase = statementUseCase;
    }

    @GetMapping("/statement")
    public PageResponse<TransactionResponse> getStatement(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
