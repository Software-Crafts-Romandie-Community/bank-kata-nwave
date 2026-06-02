package com.softcrafts.bankkata.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction value object — immutable record.
 * Captures the type, amount, and timestamp of each account operation.
 *
 * No Spring annotations — pure domain object.
 */
public record Transaction(
    Type type,
    BigDecimal amount,
    Instant timestamp
) {
    public enum Type {
        DEPOSIT,
        WITHDRAWAL
    }
}
