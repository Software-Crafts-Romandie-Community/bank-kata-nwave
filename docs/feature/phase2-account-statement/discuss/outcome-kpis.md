# Outcome KPIs — phase2-account-statement

**Feature** : `phase2-account-statement`
**Job** : `job-002` — bank-account-statement-consultation
**Date** : 2026-06-18

---

## Objectif

> Marie consulte l'historique complet de sa session bancaire depuis son navigateur
> en moins de 30 secondes, sans aide externe, et peut reconcilier son solde final
> avec toutes les opérations effectuées.

---

## Outcome KPIs

| # | Qui | Fait quoi | De combien | Baseline | Mesure par | Type |
|---|-----|-----------|------------|----------|------------|------|
| 1 | Cliente bancaire (Marie) ayant effectué >= 1 opération | Consulte l'historique sans aide externe | 100 % des appels GET /api/statement retournent la liste exacte | 0 % (greenfield) | Tests UAT end-to-end | Leading |
| 2 | Cliente bancaire (Marie) | Reconcilie son solde avec les transactions visibles | 0 écart entre solde affiché et somme algébrique des transactions | 0 % (greenfield) | Property test + UAT Scenario 5 | Guardrail |
| 3 | Cliente bancaire (Marie) — session sans opération | Comprend l'état initial sans confusion | Message d'état vide visible dans 100 % des relevés vides | 0 % (greenfield) | Test UAT Scenario 3 | Leading |

---

## Metric Hierarchy

- **North Star** : Marie peut vérifier chaque opération de sa session et confirmer que son
  solde est juste — sans appeler la banque.
- **Leading Indicators** :
  - Taux de succès GET /api/statement (cible : 100 % en conditions normales)
  - Exactitude de la liste : len(transactions affichées) == len(Account.getTransactions()) (cible : 100 %)
- **Guardrail Metrics** :
  - Cohérence solde : `sum(+dépôts) - sum(-retraits) == solde affiché` — 0 écart toléré
  - Aucune régression Phase 1 : GET /api/balance, POST /api/deposit, POST /api/withdraw inchangés

---

## Measurement Plan

| KPI | Source de données | Méthode de collecte | Fréquence | Owner |
|-----|------------------|---------------------|-----------|-------|
| Exactitude liste transactions | Tests UAT Cucumber (acceptance) | `assertThat(transactions).hasSize(expectedCount)` | À chaque CI run | software-crafter |
| Cohérence solde | Test UAT Scenario 5 | Comparaison solde relevé vs GET /api/balance | À chaque CI run | software-crafter |
| État vide correct | Test UAT Scenario 3 | Assertion message "Aucune transaction" | À chaque CI run | software-crafter |

---

## Hypothesis

Nous croyons que l'ajout de `GET /api/statement` et d'un composant React `StatementView`
pour les clients bancaires (Marie) permettra d'atteindre : consultation complète de l'historique
sans aide externe, solde toujours reconciliable.

Nous saurons que c'est vrai quand : 100 % des tests UAT passent (liste exacte, ordre décroissant,
état vide géré, solde cohérent) et quand 0 régression est détectée sur les 4 scénarios Phase 1.
