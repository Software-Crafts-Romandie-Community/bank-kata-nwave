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
And le corps de la reponse est un tableau JSON de 2 objets avec type, amount, timestamp
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

---

## Acceptance Criteria

- [ ] GET /api/statement repond 200 OK avec un tableau JSON des transactions
- [ ] Les transactions sont triees du plus recent au plus ancien
- [ ] Chaque transaction affiche type (DEPOT/RETRAIT), montant signe, date/heure
- [ ] Un compte sans transaction renvoie un tableau vide et la page affiche un etat vide explicite

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
- **Tri** : ordre chronologique descendant (plus recent en premier) — a appliquer cote serveur
- **Persistance** : aucune — `Account.getTransactions()` deja disponible en memoire (Phase 1)
- **Dependances** : aucune — premier slice du walking skeleton Phase 2
