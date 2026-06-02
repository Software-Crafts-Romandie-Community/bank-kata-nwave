# ATDD Infrastructure Policy

Per `nw-distill` § Project Infrastructure Policy. One file per project.
Apply-if-exists; write-if-absent; rewrite with `--policy=fresh`. Git history is the audit trail.

## Driving

| Port | Mechanism | Note |
|------|-----------|------|
| HTTP REST API (`AccountController`) | MockMvc via `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` | No real Tomcat — full Spring context wired, HTTP layer exercised via MockMvc |

## Driven internal (real)

| Port | Mechanism | Note |
|------|-----------|------|
| `AccountRepository` (`InMemoryAccountRepository`) | Real `@Component` bean in Spring test context | Singleton shared between scenarios — state reset in `@BeforeEach` via `reset()` method |

## Driven external / non-deterministic (fake)

| Port | Fake | Note |
|------|------|------|
| (none Phase 1) | n/a | No external dependencies in Phase 1 — no clock, email, or third-party API |
