package com.softcrafts.bankkata.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable record of a single account operation.
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
