# Upstream Issues — DISTILL — phase2-transaction-history

**Wave** : DISTILL
**Date** : 2026-06-16
**Statut** : 2 findings documentés, 0 bloquant — résolus dans cette même session

---

## Finding 1 — Le domaine ne permet pas de dater une transaction dans le passé

### Constat

`Account.deposit()` / `Account.withdraw()` (inchangés Phase 1, contrainte additive stricte)
horodatent systématiquement avec `Instant.now()` :

```java
transactions.add(new Transaction(Transaction.Type.DEPOSIT, amount, Instant.now()));
```

Or plusieurs scénarios UAT de `slice-05`/`slice-06` (hérités du DISCUSS, confirmés DESIGN)
supposent des transactions situées à des dates calendaires précises et distinctes
(ex. "Marie a des transactions le 2026-06-01, le 2026-06-10 et le 2026-06-15", filtre
`from=2026-06-01&to=2026-06-12`). Sans mécanisme de contrôle du temps, un test automatisé
exécuté en quelques millisecondes ne peut produire que des transactions toutes horodatées
"aujourd'hui" (jour d'exécution du test) — il est donc impossible de vérifier mécaniquement
qu'une transaction du 15 juin est exclue d'un filtre `[01/06, 12/06]` sans un moyen de
contrôler l'horodatage.

### Pourquoi ce n'est pas une contradiction de wave-decision

Aucune décision DISCUSS/DESIGN n'est contredite : DESIGN confirme explicitement
("Reuse Analysis" + Wave Decisions D1-D13) qu'`Account`/`Transaction` restent **non modifiés**.
Le gap est mécanique (comment fabriquer un fixture de test), pas une divergence de
décision produit. Pas de blocage du handoff DESIGN→DISTILL requis — pas de retour
utilisateur nécessaire, résolution dans le périmètre DISTILL (fixtures de test).

### Options évaluées

| Option | Modifie `Account.java` ? | Retenue |
|---|---|---|
| Ajouter un constructeur/méthode `Account.replay(transactions)` pour les tests | OUI — viole la contrainte additive stricte (9 composants REUSE AS-IS) | Rejetée |
| Introduire une abstraction `Clock` injectée | OUI (nouveau champ/dépendance) + DESIGN n'a jamais mentionné de port `Clock` (D8 amendement ne porte que sur pagination) | Rejetée — hors périmètre DESIGN, ajouterait une dépendance non négociée |
| Réflexion test-only pour réécrire le champ privé `transactions` après un dépôt/retrait réel | NON — `Account.java` n'est touché d'aucune façon, seul le test (même processus JVM) accède au champ privé via `Field.setAccessible(true)` | **Retenue** |

### Résolution appliquée

`StatementSteps.java` expose un helper privé `seedTransaction(type, amount, date)` qui :
1. Appelle `account.deposit(amount)` / `account.withdraw(amount)` (le vrai chemin domaine —
   règles métier appliquées normalement, balance mise à jour correctement) ;
2. Réécrit, par réflexion, l'horodatage de la dernière transaction ajoutée (le champ privé
   `Account.transactions` reste une `ArrayList` mutable même si l'accesseur public renvoie une
   vue non modifiable) ;
3. Sauvegarde le compte via `AccountRepository.save()`.

Zéro ligne de `src/main/java` modifiée par cette résolution. Risque documenté : fragile au
renommage du champ `transactions` — acceptable, cantonné aux fixtures de test, repéré
immédiatement par l'échec de compilation du test (pas un risque de régression silencieuse en
production).

---

## Finding 2 — Deux scénarios UAT sont frontend-only, sans port driving backend

### Constat

1. **slice-06**, scénario "Une date de fin avant la date de debut est rejetee" : l'assertion clé
   est *"And aucune requete invalide n'est envoyee au serveur"* — il s'agit d'une validation
   **locale** (cote client), explicitement conçue pour **éviter** un appel à `GET /api/statement`.
   Aucun comportement backend n'est exercé.
2. **slice-07** (intégralité) : "Detail d'une transaction" est, par décision DESIGN
   (Alternative 3 rejetée du DISCUSS, confirmée par le Component Decomposition), **purement
   frontend** — aucun nouvel endpoint, les données sont déjà en mémoire côté client depuis
   `GET /api/statement`. Il n'existe structurellement aucun driving port HTTP à exercer pour ces
   5 scénarios (4 de base + 1 amendement pagination/tri).

### Pourquoi ce n'est pas un blocage

Le Mandate "chaque AC nomme le driving port" reste respecté : le driving port de ces
comportements **est l'interaction utilisateur dans le navigateur**, pas un endpoint HTTP. Le
précédent Phase 1 (`docs/feature/phase1-account-management/feature-delta.md`, section
`Wave: DISTILL`) confirme que le périmètre des tests d'acceptance Cucumber de ce projet est
**strictement backend** (driving port = HTTP REST) ; les comportements frontend (`App.tsx`,
`OperationForm.tsx`) ont été couverts par des tests Vitest écrits pendant **DELIVER**, pas par
DISTILL. Aucune régression de méthode — ce DISTILL applique le même partage de responsabilité.

### Résolution appliquée

- Le scénario "from > to rejeté côté client" et les 5 scénarios `slice-07` sont **exclus** du
  périmètre des fichiers `.feature` Cucumber de cette session.
- Ils sont documentés dans `feature-delta.md` (section `[REF] Scenario List`) comme
  **"frontend-only — couverture Vitest en DELIVER"**, avec un renvoi explicite vers cette
  section pour que le crafter ne les oublie pas (ce ne sont pas des AC abandonnées, seulement
  déplacées vers la couche de test appropriée).
- Aucun scaffold RED Java n'est requis pour ces 5+1 scénarios — le scaffold concerné
  (`StatementPage`, `DateRangeFilter`, `TransactionDetail` côté frontend) sera créé par le
  crafter en DELIVER, au même titre que `OperationForm.tsx` en Phase 1.

---

## Synthèse

| Finding | Bloquant ? | Résolution | Fichier impacté |
|---|---|---|---|
| 1 — Backdating des transactions | Non | Réflexion test-only dans `StatementSteps.java` | `src/test/java/.../acceptance/steps/StatementSteps.java` |
| 2 — Scénarios frontend-only (1 de slice-06 + 5 de slice-07) | Non | Exclus des `.feature` backend, documentés pour couverture Vitest en DELIVER | `feature-delta.md` § Scenario List |

Aucune clarification utilisateur requise — les deux findings sont mécaniques (comment tester),
pas des contradictions de décision produit/architecture.
