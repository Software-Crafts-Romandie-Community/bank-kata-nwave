package com.softcrafts.bankkata.domain;

// SCAFFOLD: true
// Value object (Java Record) — transaction type (DEPOSIT/WITHDRAWAL) + amount + timestamp.

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction value object — immutable record.
 * Captures the type, amount, and timestamp of each account operation.
 *
 * No Spring annotations — pure domain object.
 */
public record Transaction(
    // SCAFFOLD: true
    Type type,
    BigDecimal amount,
    Instant timestamp
) {
    public enum Type {
        DEPOSIT,
        WITHDRAWAL
    }
}
