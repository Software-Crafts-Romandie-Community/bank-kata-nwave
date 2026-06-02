package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/**
 * Incoming DTO — withdrawal request body.
 *
 * JSON representation: {"amount": 80.00}
 *
 * Used by: POST /api/withdraw
 */
public record WithdrawRequest(BigDecimal amount) {
}
