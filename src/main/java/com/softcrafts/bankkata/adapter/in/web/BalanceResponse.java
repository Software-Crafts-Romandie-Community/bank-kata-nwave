package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/**
 * Outgoing DTO — account balance response.
 *
 * BigDecimal is serialized as Number (not String) — no explicit Jackson config needed.
 */
public record BalanceResponse(BigDecimal balance) {
}
