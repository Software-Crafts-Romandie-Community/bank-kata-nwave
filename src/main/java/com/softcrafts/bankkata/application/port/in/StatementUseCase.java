package com.softcrafts.bankkata.application.port.in;

// SCAFFOLD: true -- created by DISTILL (phase2-transaction-history).

import com.softcrafts.bankkata.domain.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Primary port (driving port) -- strictly read-only contract for the transaction
 * statement (Mandate 12: AccountUseCase remains the sole write-capable driving port).
 *
 * No Spring annotations -- the interface is independent of the framework.
 *
 * The method signature below is illustrative only (DESIGN deliberately deferred the
 * exact "how" to the crafter -- see docs/product/architecture/brief.md, "Driving port --
 * StatementUseCase"). The crafter may refine it during DELIVER as long as the
 * implementation never calls AccountRepository.save().
 */
public interface StatementUseCase {

    List<Transaction> getStatement(Instant from, Instant to, int page, int size, String sortBy, String sortDir);
}
