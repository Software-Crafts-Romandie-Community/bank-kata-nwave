package com.softcrafts.bankkata.adapter.in.web;

// SCAFFOLD: true
// DTO incoming — carries the deposit amount from POST /api/deposit request body.

import java.math.BigDecimal;

/**
 * Incoming DTO — deposit request body.
 *
 * JSON representation: {"amount": 150.00}
 *
 * Used by: POST /api/deposit
 */
public record DepositRequest(
    // SCAFFOLD: true
    BigDecimal amount
) {
}
