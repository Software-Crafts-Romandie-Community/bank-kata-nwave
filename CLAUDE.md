# Bank Application — nWave Project

## Project Status

- **Phase 1 — Account Management (Web UI)**: ✅ Terminée, finalisée via `/nw-finalize` → résumé dans `docs/evolution/2026-06-16-phase1-account-management.md` (le workspace `docs/feature/phase1-account-management/` est conservé comme historique, conformément à la convention nWave).
- **Phase 2 — Transaction History**: pas démarrée (voir `SPEC.md` §Phase 2 — relevé de compte, filtre par date, détail des transactions). Prochain incrément candidat.
- **Phase 3 — Interest & Advanced Features**: pas démarrée (voir `SPEC.md` §Phase 3).

## Development Paradigm

This project follows the **object-oriented programming** paradigm.
Use @nw-software-crafter for implementation.

**Language**: Java 21 (LTS)
**Architecture**: Hexagonal (Ports & Adapters)
**Framework**: Spring Boot 3.x

## Tooling — JetBrains MCP (Priority)

This project is open in IntelliJ IDEA. **Always prefer JetBrains MCP tools** over
native Bash/grep equivalents — they use IDE indexes and are context-aware.

| Task | Prefer (MCP) | Avoid |
|------|-------------|-------|
| List directory | `mcp__jetbrains__list_directory_tree` | `ls`, `find` |
| Find files (glob) | `mcp__jetbrains__find_files_by_glob` | `find`, Glob tool |
| Find files (name) | `mcp__jetbrains__find_files_by_name_keyword` | `find -name` |
| Search text | `mcp__jetbrains__search_in_files_by_text` | `grep` |
| Search regex | `mcp__jetbrains__search_in_files_by_regex` | `grep -E` |
| Rename symbol | `mcp__jetbrains__rename_refactoring` | Edit (manual) |
| Reformat file | `mcp__jetbrains__reformat_file` | `mvn formatter:format` |
| File errors/warnings | `mcp__jetbrains__get_file_problems` | — |
| Symbol info | `mcp__jetbrains__get_symbol_info` | — |
| Project deps | `mcp__jetbrains__get_project_dependencies` | `Read pom.xml` |

> `Read` and `Edit` (native) remain preferred for targeted single-file read/write
> (better diff tracking in the harness).

## Frontend

Voir `frontend/CLAUDE.md` — règles de développement, cohérence du design system.

## Mutation Testing Strategy

This project uses **nightly-delta** mutation testing. CI runs PIT on files modified each day via GitHub Actions. NOT run during feature delivery — runs as a separate nightly job on main branch only (job `mutation-nightly` in `.github/workflows/ci.yml`).

- **Tool**: PIT (`org.pitest:pitest-maven`)
- **Scope**: `com.softcrafts.bankkata.domain.*` + `com.softcrafts.bankkata.application.*`
- **Excluded**: `adapter.*`, `BankApplication`
- **Trigger**: `push: [main]` uniquement — pas sur PR
- **Feedback delay**: ~12h (acceptable pour cadence de livraison quotidienne)
- **Rationale du choix nightly-delta**: projet en phase de démarrage (< 50k LOC estimées pour Phase 1) — voir `docs/feature/phase1-account-management/feature-delta.md` §DEVOPS/Mutation Testing Strategy
- **À revisiter** : si la base de code dépasse ~50k LOC (probable après Phase 2 — Transaction History), réévaluer cette stratégie (ex. passage à un seuil de kill-rate bloquant en CI plutôt qu'informatif)
