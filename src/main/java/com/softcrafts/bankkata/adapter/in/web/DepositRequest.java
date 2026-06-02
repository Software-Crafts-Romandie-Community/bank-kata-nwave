package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/**
 * Incoming DTO — deposit request body.
 *
 * JSON representation: {"amount": 150.00}
 *
 * Used by: POST /api/deposit
 */
public record DepositRequest(BigDecimal amount) {
}
