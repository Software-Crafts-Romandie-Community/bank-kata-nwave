package com.softcrafts.bankkata.adapter.in.web;

// SCAFFOLD: true
// DTO incoming — carries the withdrawal amount from POST /api/withdraw request body.

import java.math.BigDecimal;

/**
 * Incoming DTO — withdrawal request body.
 *
 * JSON representation: {"amount": 80.00}
 *
 * Used by: POST /api/withdraw
 */
public record WithdrawRequest(
    // SCAFFOLD: true
    BigDecimal amount
) {
}
