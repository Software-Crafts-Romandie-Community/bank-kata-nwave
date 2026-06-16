# ATDD Infrastructure Policy

Per `nw-distill` § Project Infrastructure Policy. One file per project.
Apply-if-exists; write-if-absent; rewrite with `--policy=fresh`. Git history is the audit trail.

## Driving

| Port | Mechanism | Note |
|------|-----------|------|
| HTTP REST API (`AccountController`) | MockMvc via `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` | No real Tomcat — full Spring context wired, HTTP layer exercised via MockMvc |
| HTTP REST API (`StatementController`, phase2-transaction-history) | Same mechanism as `AccountController` — same `CucumberSpringConfiguration`, same Spring context | Same port class (HTTP REST API), no new mechanism negotiated — appended by DISTILL per "apply-if-exists" |

## Driven internal (real)

| Port | Mechanism | Note |
|------|-----------|------|
| `AccountRepository` (`InMemoryAccountRepository`) | Real `@Component` bean in Spring test context | Singleton shared between scenarios — state reset in `@BeforeEach` via `reset()` method. Reused read-only by `StatementService` (phase2-transaction-history) — no new driven port, no new mechanism |

## Driven external / non-deterministic (fake)

| Port | Fake | Note |
|------|------|------|
| (none Phase 1) | n/a | No external dependencies in Phase 1 — no clock, email, or third-party API |
