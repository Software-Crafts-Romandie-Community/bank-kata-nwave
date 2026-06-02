package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/**
 * Outgoing DTO — account balance response.
 *
 * JSON representation: {"balance": 100.50}
 * BigDecimal serialized as Number (not String).
 *
 * Returned by:
 * - GET /api/balance (200 OK)
 * - POST /api/deposit (200 OK)
 * - POST /api/withdraw (200 OK)
 */
public record BalanceResponse(BigDecimal balance) {
}
