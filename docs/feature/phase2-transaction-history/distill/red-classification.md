# Red Gate Snapshot — DISTILL — phase2-transaction-history

**Wave** : DISTILL
**Date** : 2026-06-16
**Commande exécutée** : `mvn -o test -Dtest=TransactionHistoryAcceptanceTest,ArchitectureTest,AccountManagementAcceptanceTest`

---

## Classification — scénario actif

| # | Scénario | Statut | Classification | Preuve |
|---|---|---|---|---|
| 1 | Customer views the full transaction statement sorted from most recent to oldest (`walking-skeleton.feature`) | FAIL | **MISSING_FUNCTIONALITY (RED correct)** | `jakarta.servlet.ServletException: Handler dispatch failed: java.lang.AssertionError: Not yet implemented -- RED scaffold` levé par `StatementController.getStatement()` (ligne 43). Les 3 `Given` (seed par réflexion) et le `When` se sont exécutés sans erreur — l'échec survient exactement au point d'implémentation manquante, pas avant. |

## Scénarios @skip (16) — non exécutés, conforme à la stratégie one-at-a-time

`statement.feature` (8) + `date-range-filter.feature` (8) — tagués `@skip`, ignorés par le filtre `not @skip` de `TransactionHistoryAcceptanceTest`. Seront activés un par un en DELIVER.

## Non-régression vérifiée

| Suite | Résultat |
|---|---|
| `ArchitectureTest` (ArchUnit) | 3/3 PASS — `StatementController`/`StatementService`/`StatementUseCase` respectent les frontières hexagonales sans modification des règles existantes |
| `AccountManagementAcceptanceTest` (Phase 1) | 16/16 PASS — `BankApplication` (édité pour ajouter le bean `StatementUseCase`) ne casse aucun scénario Phase 1 |

## Verdict

**RED confirmé pour la bonne raison.** Aucun scénario en catégorie `IMPORT_ERROR` / `FIXTURE_BROKEN` / `SETUP_FAILURE` / `WRONG_ASSERTION`. Handoff DELIVER autorisé pour le scénario walking skeleton.
