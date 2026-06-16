# slice-05 : Walking Skeleton — GET /api/statement → releve complet affiche

**job_id** : `job-002`
**Release** : Walking Skeleton
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si l'extension additive (nouveau endpoint, nouveau DTO,
nouveau service) suffit a exposer `Account.getTransactions()` sans modifier `Account`,
`AccountService` ni `AccountController` (confirme Q5 du brief architecture).

---

## Problem

Marie a effectue plusieurs depots et retraits depuis la page de gestion de compte. Elle n'a
aucun moyen de revoir ces operations passees : si elle a un doute sur un montant ou une date,
elle devrait contacter sa banque pour verifier, ce qui est lent et frustrant.

## Who

- Cliente bancaire (grand public, 35 ans) — meme persona Marie que Phase 1
- Contexte : a deja effectue au moins un depot ou retrait en Phase 1
- Motivation : verifier que ses operations passees sont correctement enregistrees

## Solution

Un nouveau lien "Voir l'historique" depuis la page de compte ouvre une vue qui interroge
`GET /api/statement`. Toutes les transactions (depots et retraits) sont affichees, triees
du plus recent au plus ancien, avec date, type et montant.

### Elevator Pitch

- **Before** : Marie a effectue un depot de 150 EUR hier mais n'a aucun moyen de le revoir
  dans l'application — elle doit se fier a sa memoire ou appeler sa banque.
- **After** : Marie clique sur "Voir l'historique" et `GET /api/statement` lui montre
  immediatement "15/06/2026 — DEPOT — +150,00 EUR" en tete de liste.
- **Decision enabled** : Marie decide si elle peut faire confiance au solde affiche
  (toutes ses operations sont bien tracees) ou si elle doit signaler une anomalie.

---

## Domain Examples

### 1 : Releve avec plusieurs transactions (happy path)
Marie a depose 50 EUR le 1er juin, retire 50 EUR le 10 juin, depose 150 EUR le 15 juin.
Le releve affiche 3 lignes, triees du 15 juin au 1er juin.

### 2 : Releve vide — aucune transaction encore effectuee
Sofia vient de creer son acces et n'a jamais depose ni retire d'argent.
`GET /api/statement` renvoie une liste vide ; la page affiche "Aucune transaction enregistree".

### 3 : Releve avec une seule transaction
Thomas a uniquement effectue un depot de 200 EUR ce matin.
Le releve affiche une seule ligne : "DEPOT — +200,00 EUR" avec l'horodatage exact.

---

## UAT Scenarios (BDD)

### Scenario : Le releve affiche toutes les transactions triees du plus recent au plus ancien
```gherkin
Given Marie a effectue un depot de 50,00 EUR le 2026-06-01, un retrait de 50,00 EUR
  le 2026-06-10, puis un depot de 150,00 EUR le 2026-06-15
When Marie ouvre la page d'historique
Then elle voit 3 transactions triees du 2026-06-15 au 2026-06-01
And chaque ligne affiche la date, le type et le montant signe
```

### Scenario : GET /api/statement retourne le releve complet en JSON
```gherkin
Given le serveur Spring Boot est demarre et Account contient 2 transactions
When un client envoie GET /api/statement
Then la reponse HTTP a le statut 200 OK
And le corps de la reponse est un objet pagine dont "content" contient 2 objets avec type, amount, timestamp
And le Content-Type est application/json
```

### Scenario : Le releve est vide quand aucune transaction n'existe
```gherkin
Given Sofia n'a effectue aucun depot ni retrait
When Sofia ouvre la page d'historique
Then elle voit le message "Aucune transaction enregistree"
And aucune erreur n'est affichee
```

### Scenario : Le montant et le type affiches correspondent exactement au domaine
```gherkin
Given Thomas a depose 200,00 EUR ce matin
When Thomas ouvre la page d'historique
Then il voit une ligne "DEPOT" avec le montant "+200,00 EUR"
And la date correspond a l'horodatage de la transaction en memoire
```

### Scenario : Le releve complet est pagine avec une taille de page par defaut de 20
*(Ajoute — amendement pagination/tri backend, 2026-06-16, voir `design/upstream-changes.md`)*
```gherkin
Given Marie a effectue 25 transactions au total
When Marie ouvre la page d'historique sans preciser de taille de page
Then la reponse contient 20 transactions dans "content"
And "totalElements" vaut 25
And "totalPages" vaut 2
And "page" vaut 0
```

### Scenario : Marie navigue vers la page suivante du releve
*(Ajoute — amendement pagination/tri backend)*
```gherkin
Given Marie a effectue 25 transactions au total et consulte la page 0 (taille 20)
When Marie clique sur "Page suivante"
Then la reponse contient les 5 transactions restantes dans "content"
And "page" vaut 1
```

### Scenario : Marie choisit une taille de page parmi les valeurs autorisees
*(Ajoute — amendement pagination/tri backend)*
```gherkin
Given Marie a effectue 12 transactions au total
When Marie selectionne une taille de page de 10
Then la reponse contient 10 transactions dans "content"
And "totalPages" vaut 2
```

### Scenario : Une taille de page non autorisee est rejetee
*(Ajoute — amendement pagination/tri backend)*
```gherkin
Given le serveur Spring Boot est demarre
When un client envoie GET /api/statement?size=37
Then la reponse HTTP a le statut 400 Bad Request
And le corps suit le format RFC 7807 Problem Details
```

### Scenario : Une page au-dela du nombre total de pages renvoie un resultat vide coherent
*(Ajoute — amendement pagination/tri backend)*
```gherkin
Given Marie a effectue 5 transactions au total (taille de page 20 -> 1 page)
When Marie demande la page 99
Then la reponse HTTP a le statut 200 OK
And "content" est un tableau vide
And "totalElements" vaut 5
And "totalPages" vaut 1
And "page" vaut 99
```

---

## Acceptance Criteria

- [ ] GET /api/statement repond 200 OK avec un objet pagine (`PageResponse`) dont `content` contient les transactions de la page courante
- [ ] Par defaut (sans `sortBy`/`sortDir`), les transactions de la page sont triees du plus recent au plus ancien
- [ ] Chaque transaction affiche type (DEPOT/RETRAIT), montant signe, date/heure
- [ ] Un compte sans transaction renvoie `content: []` avec `totalElements: 0`/`totalPages: 0`, et la page affiche un etat vide explicite
- [ ] *(Ajoute — amendement)* Le releve complet est retourne par page de 20 transactions par defaut (pas la totalite en un seul appel) si `size` n'est pas precise
- [ ] *(Ajoute — amendement)* La reponse inclut les metadonnees `page`, `size`, `totalElements`, `totalPages` coherentes avec le nombre reel de transactions du compte
- [ ] *(Ajoute — amendement)* Un client peut demander une taille de page parmi `{10, 20, 50}` via le parametre `size`
- [ ] *(Ajoute — amendement)* Une taille de page hors de cette liste est rejetee avec 400 Bad Request (RFC 7807)

---

## Non-Functional Requirements

- **Performance** : le temps de reponse de `GET /api/statement` est inferieur a 200 ms en conditions normales (coherent avec le guardrail Phase 1)
- **Compatibilite ascendante** : aucune modification de `Account`, `AccountService` ni `AccountController` existants — contrainte architecturale verifiee en DESIGN/DISTILL (extension additive), pas un critere d'acceptation utilisateur

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Consulte la liste complete de ses transactions passees sans contacter la banque
- **De combien** : 100 % des chargements du releve affichent un etat coherent avec le domaine (liste ou etat vide, jamais d'erreur)
- **Mesure par** : Tests UAT end-to-end (navigateur → GET /api/statement → Account.getTransactions())
- **Baseline** : 0 % (greenfield — aucun releve consultable avant Phase 2)

---

## Technical Notes

- **Extension additive** : nouveau `StatementService` (ou methode dediee), nouveau endpoint
  `GET /api/statement` sur un controller dedie ou `AccountController` etendu — sans toucher
  aux signatures existantes de `AccountUseCase` (Q5 du brief architecture)
- **Mapping domaine -> DTO** : `Transaction` (record domaine) -> DTO de reponse JSON
  (`type`, `amount`, `timestamp`) — pas de re-utilisation directe du record domaine en DTO HTTP
- **Format JSON** : montant serialise en **Number** (pas String), coherent avec la decision
  Phase 2 (diverge du choix String de Phase 1 — a documenter comme changement de contrat)
- **Tri** : ordre chronologique descendant (plus recent en premier) par defaut — parametrable
  depuis l'amendement pagination/tri (`sortBy`/`sortDir`, voir ci-dessous)
- **Persistance** : aucune — `Account.getTransactions()` deja disponible en memoire (Phase 1)
- **Dependances** : aucune — premier slice du walking skeleton Phase 2

### Amendement — Pagination et tri backend (2026-06-16, post peer-review DESIGN)

Le contrat `GET /api/statement` est etendu avec `page` (defaut 0), `size` (defaut 20, valeurs
autorisees `{10, 20, 50}`), `sortBy` (`date`|`amount`, defaut `date`), `sortDir`
(`asc`|`desc`, defaut `desc`). La reponse devient un DTO `PageResponse<TransactionResponse>`
(`content`, `page`, `size`, `totalElements`, `totalPages`) au lieu d'un tableau JSON nu. Voir
`docs/product/architecture/adr-005-backend-pagination-sorting.md` et
`docs/feature/phase2-transaction-history/design/upstream-changes.md` pour le detail complet de
l'impact et la justification. Supersede D8 (pas de pagination, itere DESIGN 1).
