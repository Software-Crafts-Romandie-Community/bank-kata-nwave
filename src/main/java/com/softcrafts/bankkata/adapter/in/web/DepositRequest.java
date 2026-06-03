package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/** Incoming DTO for POST /api/deposit request body. */
public record DepositRequest(BigDecimal amount) {
}
