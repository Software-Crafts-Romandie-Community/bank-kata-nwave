# Bank Application — nWave Project

## Development Paradigm

This project follows the **object-oriented programming** paradigm.
Use @nw-software-crafter for implementation.

**Language**: Java 21 (LTS)
**Architecture**: Hexagonal (Ports & Adapters)
**Framework**: Spring Boot 3.x

## Mutation Testing Strategy

This project uses **nightly-delta** mutation testing. CI runs PIT on files modified each day via GitHub Actions. NOT run during feature delivery — runs as a separate nightly job on main branch only (job `mutation-nightly` in `.github/workflows/ci.yml`).

- **Tool**: PIT (`org.pitest:pitest-maven`)
- **Scope**: `com.softcrafts.bankkata.domain.*` + `com.softcrafts.bankkata.application.*`
- **Excluded**: `adapter.*`, `BankApplication`
- **Trigger**: `push: [main]` uniquement — pas sur PR
- **Feedback delay**: ~12h (acceptable pour cadence de livraison quotidienne)
