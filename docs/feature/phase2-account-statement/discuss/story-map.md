# Story Map : phase2-account-statement

**User** : Marie — cliente bancaire, 35 ans
**Goal** : Consulter l'historique des transactions de sa session bancaire
**Job** : `job-002` — bank-account-statement-consultation
**Date** : 2026-06-18

---

## Scope Assessment : PASS

| Critère | Valeur | Seuil | Résultat |
|---------|--------|-------|----------|
| User Stories | 1 | <= 10 | PASS |
| Bounded Contexts | 2 (Domain + HTTP Adapter/Frontend) | <= 3 | PASS |
| Integration points walking skeleton | 3 (Browser → Spring Boot → Account) | <= 5 | PASS |
| Effort estimé | ~1 jour | <= 2 semaines | PASS |
| Outcomes indépendants séparables | 1 (relevé complet) | — | Non fragmenté |

Feature right-sized. Pas de découpage Elephant Carpaccio nécessaire.

---

## Backbone

| Accéder au relevé | Charger la liste | Lire et vérifier |
|-------------------|-----------------|-----------------|
| Cliquer "Relevé" sur la page principale | GET /api/statement → liste JSON | Parcourir le tableau |
| — | Afficher état vide si 0 transactions | Reconcilier avec solde affiché |

---

### Walking Skeleton

Slice minimale end-to-end connectant toutes les activités :

1. Marie est sur la page principale (Phase 1 acquis)
2. Elle clique sur le bouton "Relevé"
3. Le navigateur envoie `GET /api/statement`
4. La liste des transactions s'affiche avec type + montant + date
5. Le solde actuel est rappelé en bas du relevé

**Slice unique** : `slice-01-statement-api-and-ui` — couvre le walking skeleton ET le seul cas de valeur livrable.

---

### Release 1 : Relevé complet (Walking Skeleton = Release 1 fusionnés)

Cette feature est suffisamment petite pour que le Walking Skeleton et la Release 1 soient le même slice. Fragmenter en deux livraisons serait artificiel.

**Tasks incluses** :
- Endpoint `GET /api/statement` → liste `[{type, amount, date}]` triée chronologique inverse
- Bouton "Relevé" sur la page principale (React)
- Composant React `StatementView` : tableau 3 colonnes (Date / Type / Montant)
- État vide : message "Aucune transaction enregistrée dans cette session."
- Solde actuel rappelé en bas du relevé
- Signe sur le montant : `+ X,XX EUR` (DEPOSIT) / `- X,XX EUR` (WITHDRAWAL)

**Outcome ciblé** : Marie peut consulter l'historique complet de sa session et vérifier chaque opération.

---

## Priority Rationale

| Priorité | Slice | Outcome cible | Rationale |
|----------|-------|---------------|-----------|
| 1 | slice-01 (WS + R1) | Marie consulte son historique complet | Seul slice — walking skeleton ET valeur complète fusionnés. Hypothèse principale : Account.getTransactions() est suffisant sans requête DTO supplémentaire. |

**Tie-breaking appliqué** : Walking Skeleton en premier (règle de la méthode). Ici il n'y a qu'un slice donc le dilemme ne se pose pas.

**Dépendance** : `Account.getTransactions()` existe déjà (Phase 1) — risque technique minimal.
