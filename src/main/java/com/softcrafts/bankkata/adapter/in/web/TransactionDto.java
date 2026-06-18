package com.softcrafts.bankkata.adapter.in.web;

import java.math.BigDecimal;

/**
 * HTTP response DTO for a single transaction in the account statement.
 *
 * Mapping from domain Transaction happens exclusively in AccountController.
 * This record is NEVER imported by domain or application layers.
 *
 * @param type   transaction type (DEPOSIT or WITHDRAWAL)
 * @param amount transaction amount
 * @param date   ISO 8601 timestamp string
 */
public record TransactionDto(
        String type,
        BigDecimal amount,
        String date
) {
}
