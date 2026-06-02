package com.softcrafts.bankkata.adapter.in.web;

// SCAFFOLD: true
// DTO outgoing — carries the current balance in JSON responses.
// Serialized as a Number (e.g. {"balance": 100.50}) per DISTILL Q3 resolution.

import java.math.BigDecimal;

/**
 * Outgoing DTO — account balance response.
 *
 * JSON representation: {"balance": 100.50}
 * BigDecimal serialized as Number (not String) — DISTILL Q3 resolution.
 *
 * Returned by:
 * - GET /api/balance (200 OK)
 * - POST /api/deposit (200 OK)
 * - POST /api/withdraw (200 OK)
 */
public record BalanceResponse(
    // SCAFFOLD: true
    BigDecimal balance
) {
}
