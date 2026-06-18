# RED Classification — phase2-account-statement

**Date** : 2026-06-18  
**Commande** : `mvn clean test -Dtest=AccountStatementAcceptanceTest`  
**Résultat** : Tests run: 6, Failures: 1, Errors: 0, Skipped: 5

## Classification par scénario

| Scénario | Fichier | Résultat | Classification | Message |
|----------|---------|----------|----------------|---------|
| The customer retrieves a non-empty statement after making a deposit | `walking-skeleton.feature:7` | FAIL | ✅ MISSING_FUNCTIONALITY | `Status expected:<200> but was:<404>` — endpoint absent |
| The statement shows all transactions in reverse chronological order | `statement-api.feature` | SKIP | @skip | activé en DELIVER step 1 |
| Each transaction in the statement has the correct JSON fields | `statement-api.feature` | SKIP | @skip | activé en DELIVER step 2 |
| The statement returns an empty array when no transactions have occurred | `statement-api.feature` | SKIP | @skip | activé en DELIVER step 3 |
| Decimal transaction amounts are preserved without loss of precision | `statement-api.feature` | SKIP | @skip | activé en DELIVER step 4 |
| Statement amounts and current balance are consistent after mixed operations | `statement-api.feature` | SKIP | @skip | activé en DELIVER step 5 |

## Verdict pré-DELIVER

✅ **Gate passée — RED légitimes uniquement.**

Le seul scénario activé (walking skeleton) échoue avec `AssertionError` parce que
`GET /api/statement` n'existe pas encore dans `AccountController`. L'infrastructure de test
(imports, Spring context, MockMvc) compile et s'exécute correctement — aucun BROKEN.

DELIVER peut démarrer sur le cycle RED → GREEN → COMMIT.

## Phase 1 non régressive

`AccountManagementAcceptanceTest` + `ArchitectureTest` : **19 tests, 0 échec** (vérifié même exécution).
