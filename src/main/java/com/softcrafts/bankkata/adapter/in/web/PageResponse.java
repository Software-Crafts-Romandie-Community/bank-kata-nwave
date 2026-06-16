package com.softcrafts.bankkata.adapter.in.web;

import java.util.List;

/**
 * Outgoing DTO -- a generic page envelope (D10, ADR-005).
 *
 * Custom record -- deliberately not Spring Data's Pageable/Page (spring-data-commons is not
 * a project dependency and was not justified for this scope, see ADR-005 Alternative rejected).
 *
 * content       : the current page, already filtered/sorted (D11: filter -> sort -> paginate)
 * page          : requested page index (0-based), echoed back even out of bounds (D12)
 * size          : effective page size
 * totalElements : total AFTER filtering, BEFORE pagination
 * totalPages    : ceil(totalElements / size)
 *
 * Pure value -- no behaviour, no scaffold needed (nothing to RED-gate).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
