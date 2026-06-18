# slice-01 : Relevé de compte — GET /api/statement → tableau des transactions dans l'UI

**job_id** : `job-002`
**Release** : Walking Skeleton (= Release 1 fusionnés — scope atomique)
**Effort estimé** : 1 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si `Account.getTransactions()` ne suffit pas à alimenter
le relevé sans modification du domaine (hypothèse : domaine prêt, adaptateur + UI suffisants).

---

## Problem

Marie est cliente bancaire. Elle a effectué plusieurs dépôts et retraits dans sa session.
Elle voit son solde final mais ne sait pas quelles opérations ont été enregistrées.
Elle doit appeler sa banque pour obtenir un relevé — alors qu'elle vient de faire les
opérations elle-même depuis le navigateur.

## Who

- Cliente bancaire (grand public, 35 ans)
- Contexte : fin de session bancaire, elle a effectué 2 à 5 opérations, veut vérifier
- Motivation : reconcilier le solde final avec les opérations effectuées, sans aide externe

## Solution

Un nouveau bouton "Relevé" sur la page principale déclenche `GET /api/statement`.
La liste de toutes les transactions de la session s'affiche dans un tableau à trois colonnes
(Date, Type, Montant) triées par ordre chronologique inverse (plus récente en premier).
Le solde actuel est rappelé en bas du tableau. Un état vide guide l'utilisateur si aucune
opération n'a eu lieu.

### Elevator Pitch

- **Before** : Marie a un solde de 550,00 EUR mais ne sait pas si c'est la somme de 3 dépôts
  ou si un retrait a été pris en compte — elle ne voit que le chiffre final.
- **After** : Marie clique sur "Relevé" et voit un tableau lisible :
  `18/06/2026 14:32 | Dépôt | +300,00 EUR` — chaque opération est identifiable.
- **Decision enabled** : Marie décide si son compte est exact avant de fermer le navigateur,
  ou si elle doit contacter le support pour une anomalie.

---

## Domain Examples

### 1 : Session avec 4 transactions (happy path)
Marie a effectué dans l'ordre : dépôt 200 EUR (13:45), dépôt 100 EUR (14:01),
retrait 50 EUR (14:15), dépôt 300 EUR (14:32). Solde : 550,00 EUR.
Elle clique "Relevé" et voit 4 lignes dans l'ordre décroissant :
- 18/06/2026 14:32 | Dépôt   | +300,00 EUR
- 18/06/2026 14:15 | Retrait |  -50,00 EUR
- 18/06/2026 14:01 | Dépôt   | +100,00 EUR
- 18/06/2026 13:45 | Dépôt   | +200,00 EUR
Le solde rappelé en bas : 550,00 EUR. Elle reconcilie mentalement : 200+100-50+300 = 550 ✓

### 2 : Session sans aucune opération (état vide)
Thomas démarre le serveur et ouvre le relevé immédiatement.
Le compte n'a aucune transaction. La page affiche :
"Aucune transaction enregistrée dans cette session."
Aucune ligne de tableau n'est affichée. Thomas comprend l'état initial.

### 3 : Transaction avec montant décimal (edge case)
Sofia a effectué un dépôt de 149,99 EUR et un retrait de 0,01 EUR.
Le relevé affiche :
- 18/06/2026 10:05 | Retrait | -0,01 EUR
- 18/06/2026 10:03 | Dépôt   | +149,99 EUR
Solde : 149,98 EUR. Tous les centimes sont conservés et affichés correctement.

---

## UAT Scenarios (BDD)

### Scenario : Le relevé affiche toutes les transactions dans l'ordre chronologique inverse
```gherkin
Given Marie a effectué un dépôt de 200,00 EUR
And Marie a effectué un dépôt de 100,00 EUR après le premier dépôt
And Marie a effectué un retrait de 50,00 EUR après les deux dépôts
When Marie consulte son relevé de compte
Then la liste contient 3 transactions
And la première ligne affiche le retrait de 50,00 EUR (plus récent)
And la dernière ligne affiche le premier dépôt de 200,00 EUR (plus ancien)
And chaque ligne affiche un type (Dépôt ou Retrait), un montant signé et une date
```

### Scenario : GET /api/statement renvoie la liste JSON structurée
```gherkin
Given le serveur Spring Boot est démarré avec 3 transactions enregistrées en mémoire
When Thomas envoie GET /api/statement
Then la réponse HTTP a le statut 200 OK
And le corps contient une liste JSON de 3 objets
And chaque objet a les champs "type", "amount" et "date"
And les transactions sont triées par date décroissante
```

### Scenario : L'état vide est affiché explicitement si aucune transaction n'existe
```gherkin
Given la session vient de démarrer et aucune opération n'a été effectuée
When Marie consulte son relevé de compte
Then la page affiche le message "Aucune transaction enregistrée dans cette session."
And aucune ligne de tableau n'est affichée
```

### Scenario : Les montants décimaux sont affichés sans perte de précision
```gherkin
Given Sofia a effectué un dépôt de 149,99 EUR
And Sofia a effectué un retrait de 0,01 EUR
When Sofia consulte son relevé de compte
Then la liste affiche "+149,99 EUR" pour le dépôt
And la liste affiche "-0,01 EUR" pour le retrait
```

### Scenario : Le solde rappelé dans le relevé est cohérent avec la page principale
```gherkin
Given Marie a un solde de 550,00 EUR affiché sur la page principale
When Marie consulte son relevé de compte
Then le solde affiché en bas du relevé est 550,00 EUR
```

---

## Acceptance Criteria

- [ ] GET /api/statement répond 200 OK avec une liste JSON `[{type, amount, date}]`
- [ ] La liste est triée par date décroissante (transaction la plus récente en premier)
- [ ] Si aucune transaction : liste vide `[]` en JSON, message "Aucune transaction..." dans l'UI
- [ ] Chaque transaction affiche : type traduit (Dépôt/Retrait), montant signé (+/-), date lisible (dd/MM/yyyy HH:mm)
- [ ] Le solde rappelé en bas du relevé est identique au solde renvoyé par GET /api/balance
- [ ] Les montants décimaux sont conservés avec 2 décimales (ex : 149,99 EUR)
- [ ] Le bouton "Relevé" est présent sur la page principale et accessible au clavier (WCAG 2.1 AA)
- [ ] GET /api/statement répond en moins de 200 ms pour une session avec jusqu'à 100 transactions (cohérence guardrail Phase 1)

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie) ayant effectué au moins une opération dans la session
- **Fait quoi** : Consulte l'historique complet de sa session sans aide externe
- **De combien** : 100 % des appels GET /api/statement retournent la liste exacte des transactions enregistrées
- **Mesure par** : Tests UAT end-to-end (navigateur → API → domaine)
- **Baseline** : 0 % (fonctionnalité absente en Phase 1)

---

## Technical Notes

- **Domaine** : `Account.getTransactions()` retourne `List<Transaction>` — REUSE AS-IS (Phase 1)
- **Transaction record** : `type` (DEPOSIT/WITHDRAWAL) + `amount` (BigDecimal) + `timestamp` (Instant)
- **Nouvel endpoint** : `GET /api/statement` dans `AccountController` — mapping : `AccountUseCase.getStatement()` ou équivalent
- **Tri** : côté domaine ou côté adaptateur — tri par `timestamp` décroissant (`Comparator.reverseOrder()`)
- **Sérialisation JSON** : `amount` en Number (cohérence avec Phase 1 — BigDecimal → Number 2 décimales)
- **Format date JSON** : ISO 8601 (`Instant.toString()`) — formatage en `dd/MM/yyyy HH:mm` côté React
- **Frontend React** : nouveau composant `StatementView` — tableau 3 colonnes + état vide + solde bas de page
- **Navigation** : bouton "Relevé" sur `App.tsx` → router vers `StatementView` (ou section conditionnelle — décision DESIGN)
- **Persistance** : en mémoire (cohérence Phase 1 — pas de base de données)
- **CORS** : non requis (frontend servi par Spring Boot — cohérence Phase 1)
- **Dépendances** : slice-01 de Phase 1 (walking skeleton) doit être GREEN — `Account.getTransactions()` doit exister et être accessible via `AccountUseCase`
