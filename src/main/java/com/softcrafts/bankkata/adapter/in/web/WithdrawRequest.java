package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/** Incoming DTO for POST /api/withdraw request body. */
public record WithdrawRequest(BigDecimal amount) {
}
