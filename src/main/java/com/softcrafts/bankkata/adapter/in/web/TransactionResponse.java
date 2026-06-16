package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outgoing DTO -- a single transaction in the statement.
 *
 * BigDecimal is serialized as Number (not String), consistent with BalanceResponse (Phase 1)
 * and D9 (brief.md) -- no explicit Jackson config needed.
 *
 * Pure value -- no behaviour, no scaffold needed (nothing to RED-gate).
 */
public record TransactionResponse(String type, BigDecimal amount, Instant timestamp) {
}
