package com.softcrafts.bankkata.application;

import java.time.Instant;

/**
 * Application exception signalling that a requested date range is invalid (from after to).
 *
 * Raised by StatementService, never by the domain (D5, brief.md) -- the date range is a
 * transport/presentation concept (query params), not a business rule of Account/Transaction.
 * Distinct from IllegalArgumentException (Phase 1, invalid amount) to avoid a false-positive
 * shared exception handler.
 *
 * No Spring annotations -- pure application object.
 */
public class InvalidDateRangeException extends RuntimeException {

    private final Instant from;
    private final Instant to;

    public InvalidDateRangeException(Instant from, Instant to) {
        super("Invalid date range: from=" + from + " is after to=" + to);
        this.from = from;
        this.to = to;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
