<!-- markdownlint-disable MD024 -->
# Feature Delta : phase2-transaction-history

**Projet** : Bank Application — Web Banking
**Wave** : DISTILL
**Date** : 2026-06-16
**Statut** : DISTILL termine — Final Wave Review Gate CLEAR (2 approved, 2 conditionally_approved,
0 blocker, 1 high resolu en session). Pret pour handoff DELIVER

---

## Wave: DISCUSS / [REF] JTBD Analysis

### Contexte d'entree

Pas de DISCOVER ni DIVERGE prealable pour cette feature (greenfield sur ce point). Absence
notee comme risque ci-dessous (§ Wave Decisions). `docs/product/` existe deja comme SSOT —
pas de migration necessaire. Aucune contradiction DISCOVER/DISCUSS a resoudre.

### Job Statement (nouveau — job-002)

> When I need to verify that my past deposits and withdrawals were recorded correctly,
> I want to see a chronological statement of my transactions and filter it by date range,
> so I can trust the system's bookkeeping without contacting my branch.

**Job ID** : `job-002` (voir `docs/product/jobs.yaml`) — distinct de `job-001` (action :
deposer/retirer) ; job-002 porte sur la consultation/traçabilite des operations passees.

### Dimensions du Job

| Dimension | Contenu |
|-----------|---------|
| Fonctionnel | Voir la liste chronologique des transactions, filtrer par plage de dates, voir le detail d'une transaction |
| Emotionnel | Se sentir rassuree, faire confiance au systeme, eviter l'anxiete de devoir contacter la banque |
| Social | Justifier ses mouvements financiers sans intermediaire humain, garder un historique personnel consultable |

### Four Forces

| Force | Description |
|-------|-------------|
| Push | Aucune trace consultable des operations passees — Marie doit se souvenir mentalement ou demander un releve papier |
| Pull | Releve instantane et filtrable depuis le navigateur, disponible immediatement apres chaque operation |
| Anxiety | Peur qu'une transaction ait disparu ou soit incorrecte sans moyen de verifier |
| Habit | Habitude du releve papier mensuel ou de l'appel au conseiller — le releve web doit etre au moins aussi complet |

### Opportunity Score

**7/10** — Valeur claire (confiance, autonomie) mais moins critique que job-001 (action) :
consulter un historique est un besoin de verification, pas un besoin transactionnel immediat.

### JTBD-to-Story Bridge

| Step job map | Story candidate |
|---|---|
| Define : savoir si mes operations sont enregistrees | slice-05 : GET /api/statement → releve complet |
| Locate : retrouver une operation precise dans le temps | slice-06 : filtre par plage de dates |
| Confirm : verifier les details exacts d'une operation | slice-07 : detail d'une transaction |
| Monitor : constater que rien n'a disparu | Couvert par slice-05 (tri chronologique + etat vide explicite) |

---

## Wave: DISCUSS / [REF] Scope Assessment

**Resultat : PASS — right-sized, pas de split necessaire**

| Critere | Valeur | Seuil | Resultat |
|---------|--------|-------|----------|
| User Stories | 3 (slice-05, slice-06, slice-07) | <= 10 | PASS |
| Bounded Contexts touches | 1 (extension additive du contexte Account/Transaction existant — pas de nouveau contexte) | <= 3 | PASS |
| Integration points walking skeleton | 2 (Browser → GET /api/statement → Account.getTransactions()) | <= 5 | PASS |
| Effort estime | ~2 jours (0,5 + 1 + 0,5) | <= 2 semaines | PASS |
| Outcomes independants separables | 1 (consultation/traçabilite de l'historique) — releve, filtre et detail sont des increments d'un seul outcome, pas des outcomes separes | — | Non fragmente |

**Walking Skeleton : toujours utile (Decision 2 = Depends, tranchee ici).** Bien que le
projet soit brownfield (Phase 1 livree, infra Spring Boot/React en place), aucun endpoint
d'historique n'existe encore — `AccountUseCase` n'expose pas de methode de consultation
des transactions. Un skeleton minimal (slice-05) reste utile pour valider que l'extension
additive (nouveau service + nouvel endpoint, sans toucher `Account`/`AccountService`/
`AccountController`) fonctionne reellement de bout en bout avant d'ajouter le filtre et le
detail. C'est la meme hypothese a risque que Q5 du brief architecture (extension additive).

Note scope : les 3 slices forment une progression coherente (releve -> filtre -> detail),
chacune end-to-end et demontrable independamment.

---

## Wave: DISCUSS / [REF] Journey Design

### Persona

**Marie** — Cliente bancaire, 35 ans (reutilisee depuis `docs/product/journeys/account-management.yaml`,
aucune nouvelle persona creee — coherent avec Decision 3 = UX Research Depth Lightweight).

### Happy Path (4 etapes)

```
[Cliquer "Historique"] → [Voir releve complet] → [Filtrer par date] → [Voir detail]
  Depuis page compte       GET /api/statement       GET /api/statement     Clic sur ligne
  (Phase 1 existant)        Liste triee desc.        ?from=...&to=...      Vue type/montant/
                                                       Sous-ensemble         date+heure
                                                       filtre
```

### Emotional Arc

```
Doute  →  Recherche guidee  →  Reassurance  →  Confiance totale
(mes operations         (je filtre pour       (la liste filtree    (le detail confirme
sont-elles vraiment      cibler une periode)   correspond exactement  exactement ce que
enregistrees ?)                                a ce que j'attends)    je recherchais)
```

### Mockups (extraits — voir `docs/product/journeys/transaction-history.yaml` pour le detail complet des 4 steps)

```
+-----------------------------------------------+
|   Historique des transactions                 |
|   Filtrer : du [01/06/2026] au [12/06/2026]    |
|   [ Appliquer ]                                |
|   +-------------------------------------+      |
|   | Date       | Type      | Montant    |      |
|   +-------------------------------------+      |
|   | 2026-06-10 | RETRAIT   | -50,00 EUR |      |
|   | 2026-06-01 | DEPOT     | +50,00 EUR |      |
|   +-------------------------------------+      |
|   2 transactions affichees (filtre actif)      |
+-----------------------------------------------+
```

### Shared Artifacts

| Artefact | Source unique | Consommateurs |
|----------|---------------|---------------|
| `transactions` | `Account.getTransactions()` (deja existant, in-memory) | Releve complet (slice-05), releve filtre (slice-06), vue detail (slice-07) |
| `dateRange` | Saisie utilisateur — query params `from`/`to` sur GET /api/statement | Champs de saisie du filtre, requete GET /api/statement, libelle "filtre actif" |
| `balance` | `Account` domain object (deja tracke dans account-management.yaml — Phase 1) | Page de gestion de compte (point d'entree vers l'historique, lien "Voir l'historique") |

Registre complet (avec integration_risk) detaille dans `docs/product/journeys/transaction-history.yaml`
section `integration_validation`.

### Error Paths principaux

| Erreur | Declencheur | Code HTTP | Reponse attendue |
|--------|-------------|-----------|-------------------|
| Releve vide | Aucune transaction effectuee | 200 OK | Tableau vide + message "Aucune transaction enregistree" |
| Filtre sans resultat | Plage de dates sans transaction | 200 OK | Tableau vide + message "Aucune transaction sur cette periode" |
| Plage de dates invalide (from > to) | Saisie utilisateur incoherente | 400 Bad Request | RFC 7807 Problem Details |

---

## Wave: DISCUSS / [REF] User Story Map

### Backbone

| Acceder a l'historique | Consulter le releve | Filtrer par date | Voir le detail |
|-------------------------|----------------------|-------------------|------------------|
| Cliquer "Voir l'historique" depuis la page de compte | Voir toutes les transactions triees | Saisir une plage de dates | Cliquer sur une ligne du releve |
| — | Voir un etat vide si aucune transaction | Voir le releve restreint a la periode | Voir type/montant/date+heure exacts |
| — | — | Voir un etat vide si la periode ne contient rien | Revenir au releve (filtre preserve) |

### Walking Skeleton

Slice minimale end-to-end connectant toutes les activites necessaires pour valider
l'hypothese a risque (extension additive sans toucher au code Phase 1) :

1. Depuis la page de compte (Phase 1), Marie clique sur "Voir l'historique"
2. La page d'historique appelle `GET /api/statement`
3. La reponse contient toutes les transactions de `Account.getTransactions()`, triees du plus recent au plus ancien
4. La page affiche la liste (ou un etat vide explicite si aucune transaction)

= **slice-05**.

### Releases slicees par outcome

| Release | Slice | Outcome cible |
|---------|-------|----------------|
| Walking Skeleton | slice-05 | Marie peut consulter son releve complet et faire confiance a l'enregistrement de ses operations |
| Release 1 | slice-06 | Marie retrouve rapidement une operation precise en filtrant par periode |
| Release 2 | slice-07 | Marie confirme les details exacts (incluant l'heure) d'une operation specifique |

### Priority Rationale

1. **slice-05** (WS) : valide l'hypothese la plus risquee — l'extension additive (nouveau
   service + nouvel endpoint) fonctionne sans modifier `Account`, `AccountService` ni
   `AccountController` (Q5 du brief architecture). Sans cette validation, le reste du
   perimetre Phase 2 est bloque.
2. **slice-06** : ajoute la valeur la plus demandee par le job-002 (force "Locate" du job
   map) — retrouver une operation precise est le scenario d'usage le plus frequent au-dela
   du simple affichage complet.
3. **slice-07** : complete l'experience avec le niveau de detail le plus fin (heure exacte),
   utile mais moins frequent que le filtre — dependance pure sur les donnees deja recues
   par slice-05/06, aucun risque technique additionnel.

---

## Wave: DISCUSS / [REF] System Constraints

- **Extension additive uniquement** : aucune modification de `Account`, `AccountService`,
  `AccountController`, `AccountUseCase` existants — confirme Q5 du brief architecture
- **Domaine reutilise sans changement** : `Account.getTransactions()` et `Transaction`
  (record `type`/`amount`/`timestamp`) restent inchanges en Phase 2 — pas de description
  ni de solde-apres-operation ajoute au domaine pour ce perimetre
- **Framework** : Spring Boot 3.x — nouveau endpoint `GET /api/statement` (driving adapter HTTP)
- **Langage** : Java 21 LTS — inchange
- **Format JSON erreurs** : RFC 7807 Problem Details (coherent Phase 1)
- **Format JSON montants** : **Number** (pas String) — diverge du choix String de Phase 1,
  decision explicite pour Phase 2 (voir Assumption ci-dessous)
- **Codes HTTP** : 200 OK (succes, y compris liste/filtre vide), 400 Bad Request (from > to)
- **Pas d'authentification, pas de base de donnees** : in-memory, mono-utilisateur — inchange
- **Query params** : `from`/`to` au format ISO 8601 (`yyyy-MM-dd`) sur `GET /api/statement`
- **Tri** : ordre chronologique descendant (plus recent en premier) cote serveur

---

## Wave: DISCUSS / [REF] Alternatives Considered

### Alternative 1 : Modifier le domaine (filtrage dans `Account`/`Transaction`)
**Rejetee** — Q5 du brief architecture impose explicitement l'extension additive : "Phase 2
ajoutera StatementService et un endpoint /api/statement sans modifier Account, AccountService
ni AccountController". Filtrer dans le domaine creerait une dependance domaine -> requete HTTP,
contraire a l'architecture hexagonale (le domaine ne doit pas connaitre les query params).

### Alternative 2 : Endpoint de filtre separe (`GET /api/statement/filtered?from=...&to=...`)
**Rejetee** — un seul endpoint avec query params optionnels (`from`/`to`) est plus simple
cote client (une seule logique conditionnelle) et cote serveur (validation et tri partages).
Eviter la duplication de payload et de logique de tri entre deux endpoints.

### Alternative 3 : Endpoint serveur dedie au detail (`GET /api/statement/{id}`)
**Rejetee** — application in-memory mono-utilisateur ; le frontend dispose deja de toutes
les donnees de la transaction via `GET /api/statement` (slice-05/06). Un nouvel endpoint
ajouterait une latence et une complexite sans valeur ajoutee mesurable. L'etat cote client
suffit et reste testable (slice-07).

---

## Wave: DISCUSS / [REF] User Stories

Voir fichiers dedies dans `docs/feature/phase2-transaction-history/slices/`.

### Index des stories

| Slice | Fichier | Titre | Job ID | Release |
|-------|---------|-------|--------|---------|
| WS | slice-05-ws-statement-api.md | GET /api/statement → releve complet affiche | job-002 | Walking Skeleton |
| S1 | slice-06-date-range-filter.md | Filtre par plage de dates | job-002 | Release 1 |
| S2 | slice-07-transaction-detail.md | Detail d'une transaction | job-002 | Release 2 |

---

## Wave: DISCUSS / [REF] Outcome KPIs

### Objectif

> Marie consulte son releve de transactions, filtre par periode et verifie le detail
> d'une operation specifique en moins de 2 minutes, sans contacter sa banque, avec une
> confiance totale dans l'exactitude des donnees affichees.

### Outcome KPIs

| # | Qui | Fait quoi | De combien | Baseline | Mesure par | Type |
|---|-----|-----------|------------|----------|-------------|------|
| 1 | Cliente bancaire (Marie) | Consulte la liste complete de ses transactions sans contacter la banque | 100 % des chargements du releve affichent un etat coherent (liste ou etat vide, jamais d'erreur) | 0 % (greenfield — aucun releve consultable avant Phase 2) | Tests UAT GET /api/statement | Leading |
| 2 | Cliente bancaire (Marie) | Retrouve une transaction precise en filtrant par periode plutot qu'en parcourant la liste complete | 100 % des filtres valides renvoient exactement les transactions de la periode demandee (0 faux positif/negatif) | Non applicable (fonctionnalite absente) | Tests UAT GET /api/statement?from=...&to=... + scenarios bornes inclusives | Leading |
| 3 | Cliente bancaire (Marie) | Confirme les details exacts (incluant l'heure) d'une transaction selectionnee | 100 % des clics sur une ligne du releve affichent un detail coherent avec la ligne source (0 ecart) | Non applicable (fonctionnalite absente) | Tests UAT clic ligne -> assertion detail | Leading |

### North Star

**Marie consulte, filtre et verifie le detail de ses transactions passees sans jamais
contacter sa banque, avec une confiance totale dans l'exactitude de chaque donnee affichee.**

### Guardrails

- Aucune transaction du domaine n'est omise, dupliquee ou alteree dans le releve (0 ecart tolere)
- Aucune modification du domaine `Account`/`Transaction` existant (extension additive stricte)
- Temps de reponse de `GET /api/statement` < 200 ms en conditions normales (coherent Phase 1)

---

## Wave: DISCUSS / [REF] Definition of Ready

| DoR Item | Resultat | Evidence |
|----------|----------|----------|
| Probleme clair, langage domaine | PASS | Persona Marie, pain point "aucune trace consultable des operations passees" explicite dans chaque slice |
| User/persona identifie | PASS | Marie — cliente bancaire, 35 ans, reutilisee depuis account-management.yaml |
| 3+ exemples domaine avec donnees reelles | PASS | Marie, Thomas, Sofia — donnees concretes (dates, montants) dans chaque slice |
| Scenarios UAT Given/When/Then (3-7) | PASS | 4 scenarios (slice-05), 5 scenarios (slice-06), 4 scenarios (slice-07) — tous en GWT |
| AC derives des UAT | PASS | AC extraits directement des scenarios dans chaque slice |
| Right-sized (1-3 j, 3-7 scenarios) | PASS | 3 slices x 0,5-1 j, 4-5 scenarios chacune |
| Notes techniques | PASS | System Constraints + Technical Notes par slice (extension additive, format JSON Number, fuseau horaire) |
| Dependances tracees | PASS | Ordre slice-05 -> slice-06 -> slice-07 documente (Priority Rationale + Technical Notes) |
| Outcome KPIs definis | PASS | Section Outcome KPIs ci-dessus avec North Star et Guardrails |

**Statut DoR global : PASSED**

Chaque story porte un `job_id: job-002` (traçabilite JTBD obligatoire — Decision 1 et
Decision 4 de ce wave). Chaque story non-`@infrastructure` (les 3 le sont) porte un
Elevator Pitch Before/After/Decision-enabled avec endpoint reel.

---

## Wave Decisions

### Decisions interactives (fournies en entree)

| # | Decision | Valeur | Application |
|---|----------|--------|--------------|
| 1 | Feature Type | User-facing | Toutes les stories sont user-facing, aucune `@infrastructure` |
| 2 | Walking Skeleton | Depends | Evalue : OUI, un skeleton minimal reste utile malgre le brownfield — voir Scope Assessment |
| 3 | UX Research Depth | Lightweight | Persona Marie reutilisee, pas de nouvelle recherche utilisateur, pas de multi-persona |
| 4 | JTBD obligatoire | Oui | job-002 cree, chaque story trace vers `job_id: job-002` |

### Risques notes

| Risque | Description | Mitigation |
|--------|-------------|------------|
| Absence DISCOVER/DIVERGE | Aucune validation amont (entretiens clients, opportunites) pour ce perimetre precis | Perimetre directement derive de SPEC.md (deja valide produit) — risque accepte, a revalider si Phase 2 s'elargit au-dela du perimetre SPEC |

### Assumptions documentees (faute de pouvoir interroger l'utilisateur)

| # | Assumption | Justification | Impact si invalide |
|---|------------|----------------|----------------------|
| A1 | Format JSON des montants en **Number** (pas String) pour `/api/statement`, alors que Phase 1 utilisait String pour `/api/balance` etc. | Instruction explicite du contexte fourni ("BigDecimal serialise en Number JSON (pas String)") — traite comme decision deliberee pour Phase 2, pas comme oubli | Si invalide, contrat API a corriger en DESIGN avant implementation — pas de breaking change cote domaine |
| A2 | Bornes du filtre `from`/`to` interpretees comme journee complete UTC (`from` = 00:00:00 UTC, `to` = 23:59:59.999 UTC) car `Transaction.timestamp` est un `Instant` (UTC) alors que `from`/`to` sont des dates sans heure | Choix le plus simple et le moins surprenant pour une cliente grand public sans notion de fuseau horaire ; documente explicitement pour validation DESIGN | Si Marie est dans un fuseau horaire different, une transaction en fin/debut de journee locale pourrait sembler exclue a tort — a verifier en DESIGN/DISTILL |
| A3 | Le lien "Voir l'historique" est ajoute a la page de compte Phase 1 existante (pas une nouvelle page independante sans navigation) | Coherence UX avec le parcours account-management.yaml deja valide ; SPEC.md ne precise pas l'emplacement exact | Si le produit attend une route/page totalement separee, ajustement mineur en DESIGN (pas d'impact sur les contrats API) |

---

## Wave: DISCUSS / [HANDOFF] DESIGN Wave Package

### Artefacts produits

| Artefact | Chemin | Destinataire |
|----------|--------|---------------|
| Jobs YAML (mis a jour, job-002 ajoute) | `docs/product/jobs.yaml` | solution-architect, acceptance-designer |
| Journey YAML (nouveau, Gherkin embarque) | `docs/product/journeys/transaction-history.yaml` | solution-architect, acceptance-designer |
| Feature delta DISCUSS (ce fichier) | `docs/feature/phase2-transaction-history/feature-delta.md` | solution-architect |
| Slices/stories LeanUX (3) | `docs/feature/phase2-transaction-history/slices/slice-05-*.md`, `slice-06-*.md`, `slice-07-*.md` | solution-architect, acceptance-designer, platform-architect (KPIs) |

### Validation effectuee

- Peer review via `nw-product-owner-reviewer` : **conditionally_approved** en iteration 1
  (0 blocking, 2 issues HIGH non-bloquantes : AC techniques melangees + alternatives manquantes)
- Remediation appliquee : AC techniques (slice-05 #5-6, slice-07 #5) deplacees vers
  Technical Notes / Non-Functional Requirements ; section "Alternatives Considered" ajoutee
  au feature-delta.md
- DoR 9 items : PASSED (voir section ci-dessus)
- Statut final : **PRET POUR HANDOFF DESIGN**

---

## Wave: DESIGN / [REF] Mode et contexte

**Mode** : Propose — options presentees ou un vrai choix existait (voir Decisions Table),
decisions DISCUSS deja verrouillees confirmees sans re-discussion (extension additive,
filtrage applicatif, format Number, pas de pagination).

**Architecte** : Morgan (solution-architect nWave), premier architecte sur ce brief (pas de
section System Architecture/Domain Model prealable de Titan/Hera a integrer).

**Outcome Collision Check** : SKIP — `docs/product/outcomes/registry.yaml` absent. Ce projet
est un kata pedagogique, pas un repo nWave avec registry outcomes complet. Verifie avant de
lancer la CLI — fichier inexistant, check non applicable.

---

## Wave: DESIGN / [REF] DDD List

Pas de nouveau bounded context. Extension du contexte existant **Account/Transaction**.

| Concept | Statut | Notes |
|---------|--------|-------|
| Account (agregat) | Inchange | Racine d'agregat, seule source de Transaction |
| Transaction (value object) | Inchange | Record domaine, aucun champ ajoute |
| Statement (concept) | Nouveau — **pas un agregat ni une entite domaine** | Vue projetee (read model applicatif) construite a la volee par `StatementService` a partir de `Account.getTransactions()`. N'existe dans aucune persistance — recalculee a chaque requete |
| DateRange (concept) | Nouveau — **value object applicatif, pas domaine** | Paire `from`/`to` (LocalDate ou equivalent), validee dans `StatementService`/`StatementController`, jamais transmise au domaine |

---

## Wave: DESIGN / [REF] Component Decomposition

Voir `docs/product/architecture/brief.md` section `## Application Architecture — Phase 2`,
tableau "Component decomposition (nouveaux composants)". Resume :

`StatementUseCase` (port) -> `StatementService` (application) -> `AccountRepository` (port
reutilise) -> `Account.getTransactions()` (domaine inchange) -> `StatementController` (adapter
HTTP, mapping vers `TransactionResponse`/`StatementResponse`) -> Frontend (`StatementPage`,
`DateRangeFilter`, `TransactionList`, `TransactionDetail`).

---

## Wave: DESIGN / [REF] Driving Ports

**`StatementUseCase`** (nouveau, `application/port/in/`) — read-only, voir ADR-004 pour la
justification de la separation d'avec `AccountUseCase`. Aucune methode de mutation exposee.

---

## Wave: DESIGN / [REF] Driven Ports + Adapters

Aucun nouveau port driven. `AccountRepository` (existant) reutilise en lecture seule par
`StatementService` — voir ADR-004 Alternative B (rejetee) pour la justification de ne pas
etendre `AccountUseCase`, et brief.md D3 pour la justification de ne pas creer de second port
driven.

---

## Wave: DESIGN / [REF] Technology Choices

Aucun ajout. Stack Phase 1 reutilisee a l'identique (Java 21, Spring Boot 3.x, JUnit 5,
AssertJ, Mockito, ArchUnit, React 18 + TypeScript + Vite). Voir brief.md "Stack technologique —
Phase 2 (delta)". Decision explicite de ne PAS ajouter react-router (D7) ni bibliotheque de
pagination (D8).

---

## Wave: DESIGN / [REF] Decisions Table

Voir `docs/product/architecture/brief.md` section "Décisions de conception (table — [REF]
Tier-1)" pour le detail complet (D1 a D9). Resume des decisions ou un choix reel existait :

| # | Decision | Mode | Resultat |
|---|----------|------|----------|
| D2 | Nouveau port `StatementUseCase` vs extension `AccountUseCase` | Propose (options dans ADR-004) | Nouveau port separe, read-only |
| D3 | Nouveau port driven vs reutilisation `AccountRepository` | Propose | Reutilisation, pas de nouveau port |
| D5 | Type d'exception pour `from > to` | Propose | `InvalidDateRangeException` dediee |
| D7 | Routage frontend | Propose (justifie par simplicite) | Pas de router, etat local |

Decisions D1, D4, D6, D8, D9 : verrouillees ou confirmees sans alternative reelle (contraintes
DISCUSS directes ou correction factuelle du code existant).

---

## Wave: DESIGN / [REF] Reuse Analysis

Voir `docs/product/architecture/brief.md` section "Reuse Analysis (Phase 2) — HARD GATE" —
tableau complet couvrant tous les composants existants (Account, Transaction, AccountUseCase,
AccountRepository, AccountController, AccountService, BalanceResponse, App.tsx, bankApi.ts,
types/index.ts, BalanceDisplay, OperationForm) et tous les nouveaux composants avec contract
shape pour chaque composant a recoupement.

---

## Wave: DESIGN / [REF] Open Questions

| # | Question | Impacte |
|---|----------|---------|
| Q6 | Volume de transactions avant que l'absence de pagination (D8) devienne un probleme UX | Hors perimetre Phase 2 — a surveiller Phase 3 |
| Q7 | Fuseau horaire utilisateur si l'application devient multi-utilisateur (D6/A2) | Hors perimetre Phase 2 (pas d'auth) — a revisiter Phase 3 |

---

## Changed Assumptions

| # | Assumption DISCUSS | Statut DESIGN | Detail |
|---|---------------------|----------------|--------|
| A1 | Format JSON montants en Number "alors que Phase 1 utilisait String" | **CORRIGEE — factuellement fausse** | Lecture du code source confirme que `BalanceResponse` (Phase 1) serialise deja `BigDecimal` en Number natif, sans config Jackson explicite. Phase 1 n'a jamais utilise String. `TransactionResponse.amount` suit donc la convention Number deja en place, pas un changement de convention. Aucun impact sur les stories (le contrat API attendu — Number — est inchange), simple correction de la justification documentee dans le DISCUSS |
| A2 | Bornes from/to = journee complete UTC (00:00:00.000Z / 23:59:59.999Z) | **CONFIRMEE sans changement** | Voir brief.md D6. Coherent avec `Transaction.timestamp` en `Instant` UTC et l'absence de notion de fuseau utilisateur dans le perimetre (pas d'auth, mono-utilisateur). Aucun impact sur les stories |
| A3 | Lien "Voir l'historique" ajoute a la page de compte Phase 1 existante | **CONFIRMEE sans changement** | Voir brief.md "Frontend — composants et état (sans router)", D7. `App.tsx` etendu avec un etat de navigation local, pas de nouvelle page/route independante. Aucun impact sur les stories |

Aucune assumption n'impacte le contenu des stories elles-memes (slice-05/06/07 restent valides
tel que redigees) — `docs/feature/phase2-transaction-history/design/upstream-changes.md` non
necessaire pour ce perimetre.

> **Note (amendement 2026-06-16)** : la phrase ci-dessus ("upstream-changes.md non necessaire")
> etait vraie au moment de l'iteration DESIGN 1 (peer review approuvee, 0 issue). Elle est
> **supersedee** par l'amendement ci-dessous : une nouvelle exigence produit post-review a rendu
> `upstream-changes.md` necessaire. Voir section suivante.

---

## Wave: DESIGN / [REF] Amendement — Pagination et tri backend

**Date** : 2026-06-16 (post peer-review iteration 1, approuvee 0 issue)
**Declencheur** : exigence produit explicite du product owner (Sylvain Chabert), exprimee apres
l'approbation de l'iteration DESIGN 1 — "affichage paginé (avec tri), avec une pagination
provenant du backend (même si le stockage est in-memory)".
**Mode** : Propose — le besoin lui-meme est verrouille (pas de re-discussion), les options
presentees portent sur le COMMENT (style API, comportement page hors limites, validation `size`).

### Contexte

L'iteration DESIGN 1 avait verrouille D8 ("aucune pagination") en se basant sur un volume de
transactions faible (mono-utilisateur, in-memory, dizaines a centaines de transactions par
session de demo). Cette decision etait correcte pour le perimetre alors connu — **ce n'est pas
une erreur corrigee, c'est un changement de perimetre pilote par le produit**. Voir
`docs/product/architecture/adr-005-backend-pagination-sorting.md` pour l'analyse complete.

### Decisions deja tranchees par le product owner (non re-debattues)

- Champs triables : `timestamp` (date) et `amount` (montant) — tri par defaut date decroissante
- Style API : DTO custom leger (`PageResponse<T>`), pas Spring Data `Pageable`/`Page`
- Taille de page : defaut 20, configurable parmi `{10, 20, 50}` cote UI

### Nouvelles decisions (D10-D13)

| # | Decision | Mode | Resultat |
|---|----------|------|----------|
| D10 | Pagination backend — style API | Verrouille produit (options de style evaluees) | DTO custom `PageResponse<T>` — pas Spring Data `Pageable`/`Page` (dependance `spring-data-commons` non justifiee, absente du projet) |
| D11 | Ordre des operations dans `StatementService` | Propose -> Retenu | Filtre (date) -> Tri -> Pagination, ordre strict — garantit `totalElements`/`totalPages` coherents avec le filtre actif |
| D12 | Comportement page hors limites (ex. `page=99` sur 2 pages) | Propose -> Retenu | 200 OK, `content: []`, metadonnees coherentes — pas de 400, coherent avec "jamais d'erreur pour absence de resultat" (slice-05/06) |
| D13 | Validation de `size` | Propose -> Retenu | Whitelist stricte `{10, 20, 50}` -> 400 si hors liste — le PO a fixe une liste fermee, pas un plafond ouvert |

Detail complet (alternatives evaluees, rationale) : voir
`docs/product/architecture/adr-005-backend-pagination-sorting.md`.

### Contrat API amende — `GET /api/statement`

Nouveaux query params : `page` (defaut 0), `size` (defaut 20, `{10,20,50}`), `sortBy`
(`date`|`amount`, defaut `date`), `sortDir` (`asc`|`desc`, defaut `desc`) — en plus de `from`/`to`
(slice-06, inchanges). Nouvelle reponse : `PageResponse<TransactionResponse>` (`content`, `page`,
`size`, `totalElements`, `totalPages`) remplace l'enveloppe `StatementResponse` (liste plate).

Detail complet du contrat : voir `docs/product/architecture/brief.md`, section
`### Pagination et tri — Amendement Phase 2`.

### Impact sur les composants

`StatementUseCase` (contrat elargi, toujours read-only), `StatementService` (pipeline etendu
filtre->tri->pagination), `StatementController` (parsing/validation des 4 nouveaux parametres),
nouveau DTO `PageResponse<T>`. Cote frontend : `PageSizeSelector` (nouveau), `PaginationControls`
(nouveau, precedent/suivant + numero page), `TransactionList` etendu (en-tetes de colonnes
cliquables pour le tri), `StatementPage` (etat local elargi : filtre + page + tri). Aucune
nouvelle dependance npm. Detail complet : voir brief.md, tableau "Reuse Analysis — composants
impactes par l'amendement".

### Impact sur les stories DISCUSS (Document Update / Back-Propagation)

Voir `docs/feature/phase2-transaction-history/design/upstream-changes.md` pour le detail complet
(AC amendes, nouveaux scenarios Gherkin). Resume :

- **slice-05** : 3 AC amendes (forme de reponse paginee), 4 AC ajoutes (pagination de base), 5
  scenarios Gherkin ajoutes (page par defaut, page suivante, taille choisie, taille invalide,
  page hors limites)
- **slice-06** : 3 AC amendes (total coherent avec le filtre), 3 AC ajoutes (total post-filtre,
  tri combine au filtre), 4 scenarios Gherkin ajoutes (total filtre, tri croissant/decroissant,
  from>to avec params de pagination presents)
- **slice-07** : 1 AC amende (etat preserve elargi a filtre+page+tri, pas seulement filtre), 1
  scenario Gherkin ajoute

Les 3 fichiers slice ont ete amendes directement dans cette session (autorisation explicite du
product owner du projet pour cette extension de perimetre post-review).

### D8 — Supersession explicite

**D8 ("Aucune pagination", iteration DESIGN 1) est SUPERSEDEE par D10/ADR-005.** D8 reste
documentee intacte dans le brief (on ne reecrit pas l'historique) et marquee SUPERSEDED dans la
table des decisions de `brief.md`. Ce n'etait pas une erreur : c'etait la bonne decision pour le
perimetre connu au moment ou elle a ete prise (mono-utilisateur, in-memory, volumes faibles). Le
changement de contexte (exigence produit explicite post-review) justifie la supersession, pas une
invalidation retroactive.

### Quality Gates — auto-validation de l'amendement

- [x] Nouvelle exigence tracee vers composants nommes (StatementService/Controller/UseCase + 2
      nouveaux composants frontend)
- [x] ADR cree avec 3 alternatives evaluees et rejetees/retenue (ADR-005)
- [x] C4 Container — note explicite "inchange, voir diagramme iteration 1" (aucun nouveau
      composant conteneur)
- [x] Contrat API coherent de bout en bout (brief.md, ADR-005, slices amendees, frontend)
- [x] Document Update / Back-Propagation effectue (`upstream-changes.md` + slices amendees)
- [x] D8 marquee SUPERSEDED, pas effacee — tracabilite preservee
- [x] Aucune nouvelle dependance OSS introduite (DTO custom, pas Spring Data, pas de lib JS)
- [x] Aucun fichier Phase 1 (`Account`, `Transaction`, `AccountUseCase`, `AccountService`,
      `AccountRepository`, `AccountController`) modifie

---

## Wave: DISTILL / [REF] Wave-Decision Reconciliation

**Resultat : PASS — 0 contradiction.** Les 3 fichiers `wave-decisions.md` disponibles
(DISCUSS — embarque dans ce fichier ; DESIGN — `design/wave-decisions.md`) ont ete relus.
L'amendement DESIGN (pagination/tri, supersede D8) a deja ete reconcilie avec DISCUSS dans la
meme session via `design/upstream-changes.md` (3 slices amendees en place, pas de contradiction
residuelle). Aucun fichier `devops/wave-decisions.md` n'existe pour cette feature — degradation
gracieuse appliquee (voir section Pre-requisites ci-dessous, meme traitement que Phase 1).

## Wave: DISTILL / [REF] Upstream Findings (Document Update)

2 findings documentes dans `docs/feature/phase2-transaction-history/distill/upstream-issues.md`,
0 bloquant :

1. **Backdating des transactions** — le domaine (`Account.deposit()`/`withdraw()`, inchange)
   horodate toujours avec `Instant.now()`. Resolu par une fixture de test utilisant la reflexion
   pour reecrire l'horodatage de la derniere transaction ajoutee (zero modification de
   `Account.java`). Voir `StatementSteps.java` javadoc + upstream-issues.md Finding 1.
2. **2 scenarios frontend-only** — "from > to rejete cote client" (slice-06) et l'integralite de
   slice-07 (detail, sans driving port backend) sont exclus du perimetre Cucumber et documentes
   pour couverture Vitest en DELIVER (meme partage de responsabilite que Phase 1, ou les tests
   frontend `App.test.tsx`/`OperationForm.test.tsx` ont ete ecrits en DELIVER, pas en DISTILL).

## Wave: DISTILL / [REF] Scenario List

| # | Feature file | Scenario title | Tags | Active? |
|---|---|---|---|---|
| 1 | walking-skeleton.feature | Customer views the full transaction statement sorted from most recent to oldest | `@walking_skeleton @real-io @driving_port @US-WS` | YES |
| 2 | statement.feature | The full statement is returned as a single coherent paginated object | `@driving_port @US-WS @skip` | no |
| 3 | statement.feature | The statement is empty when no transaction exists | `@driving_port @US-WS @skip` | no |
| 4 | statement.feature | The amount and type shown match exactly the domain transaction | `@driving_port @US-WS @skip` | no |
| 5 | statement.feature | The full statement defaults to a page size of 20 | `@driving_port @US-WS @skip` | no |
| 6 | statement.feature | Customer navigates to the next page of the statement | `@driving_port @US-WS @skip` | no |
| 7 | statement.feature | Customer selects an allowed page size | `@driving_port @US-WS @skip` | no |
| 8 | statement.feature | An unsupported page size choice is rejected | `@driving_port @US-WS @skip @error` | no |
| 9 | statement.feature | A page beyond the last page returns a coherent empty result | `@driving_port @US-WS @skip` | no |
| 10 | date-range-filter.feature | The date filter restricts the statement to the requested period | `@driving_port @US-S1 @skip` | no |
| 11 | date-range-filter.feature | A filter with no results shows an explicit empty state | `@driving_port @US-S1 @skip` | no |
| 12 | date-range-filter.feature | The date range bounds are inclusive | `@driving_port @US-S1 @skip` | no |
| 13 | date-range-filter.feature | A filter with an end date before the start date is rejected | `@driving_port @US-S1 @skip @error` | no |
| 14 | date-range-filter.feature | The date filter total matches the filtered subset, not the full account | `@driving_port @US-S1 @skip` | no |
| 15 | date-range-filter.feature | Ascending amount sort applies to a date-filtered statement | `@driving_port @US-S1 @skip` | no |
| 16 | date-range-filter.feature | Descending amount sort applies to a date-filtered statement | `@driving_port @US-S1 @skip` | no |
| 17 | date-range-filter.feature | An invalid date range is rejected even when pagination and sort parameters are present | `@driving_port @US-S1 @skip @error` | no |

**Error path ratio** (tag `@error` strict) : 3/17 = 17.6%. **Error-or-edge ratio** (incluant les
etats vides, bornes inclusives et page hors limites — scenarios 3, 9, 11, 12) : 9/17 = 52.9% —
au-dessus du seuil 40% cible. Coherent avec le ratio strict plus bas que Phase 1 (50%) car
plusieurs AC de pagination sont des comportements par defaut/positifs (D11-D13), pas des refus.

**Hors perimetre Cucumber (frontend-only, voir upstream-issues.md Finding 2)** : 1 scenario
slice-06 ("from > to rejete cote client, aucune requete envoyee") + 5 scenarios slice-07
(detail transaction) — couverture Vitest en DELIVER, pas comptes dans le ratio ci-dessus.

**Tier A only** — feature journey-rich (filtre + tri + pagination combinables) mais sans entree
libre/texte (dates ISO contraintes, montants numeriques) — pas de richesse d'espace d'entree
justifiant Tier B PBT (Mandate 10). Coherent avec le choix Phase 1.

---

## Wave: DISTILL / [REF] WS Strategy

| Dimension | Decision |
|---|---|
| Strategy | Architecture of Reference (inchangee depuis Phase 1) : MockMvc comme driving adapter HTTP ; `InMemoryAccountRepository` reutilisee en lecture seule comme driven bean reel |
| Walking Skeleton | Scenario 1 — "Customer views the full transaction statement sorted from most recent to oldest" — ferme la boucle : test -> MockMvc -> `StatementController` -> `StatementUseCase` -> `AccountRepository.load()` -> assertion |
| Litmus test | Un non-technicien confirme : "Oui, c'est ce dont j'ai besoin pour retrouver mes operations passees" |
| Tagging | `@walking_skeleton @real-io @driving_port` |
| Infrastructure | Meme `CucumberSpringConfiguration` que Phase 1 (`@SpringBootTest(webEnvironment = MOCK)`) — aucun nouveau contexte Spring, glue partage entre `AccountManagementSteps` et `StatementSteps` |

---

## Wave: DISTILL / [REF] Adapter Coverage

| Adapter | Driven port | @real-io scenario | Coverage |
|---|---|---|---|
| `InMemoryAccountRepository` | `AccountRepository` (reutilise, lecture seule) | YES — Walking Skeleton (Scenario 1) | Meme bean Spring reel que Phase 1, aucun nouveau driven adapter introduit |

Aucun nouveau driven adapter en Phase 2 (D3, brief.md). Coverage : complete (heritee de Phase 1).

---

## Wave: DISTILL / [REF] Scaffolds

Tous les fichiers scaffold portent le marqueur `// SCAFFOLD: true`. Les methodes a comportement
(non-DTO) levent `AssertionError("Not yet implemented -- RED scaffold")`. Les DTO purs
(`TransactionResponse`, `PageResponse`) n'ont aucun comportement a RED-gater (pure-value, meme
traitement que `BalanceResponse` en Phase 1).

| File | Type | Layer | RED behaviour |
|---|---|---|---|
| `src/main/java/com/softcrafts/bankkata/application/port/in/StatementUseCase.java` | Primary port (interface) | application/port/in | n/a (interface, signature illustrative — voir javadoc) |
| `src/main/java/com/softcrafts/bankkata/application/StatementService.java` | Application service | application | `AssertionError` sur `getStatement()` |
| `src/main/java/com/softcrafts/bankkata/application/InvalidDateRangeException.java` | Application exception | application | n/a (exception pure donnee, ecrite complete — pas de comportement a faker) |
| `src/main/java/com/softcrafts/bankkata/adapter/in/web/StatementController.java` | HTTP driving adapter | adapter/in/web | `AssertionError` sur `getStatement()` |
| `src/main/java/com/softcrafts/bankkata/adapter/in/web/TransactionResponse.java` | DTO sortant (record) | adapter/in/web | n/a (pure-value, ecrit complet) |
| `src/main/java/com/softcrafts/bankkata/adapter/in/web/PageResponse.java` | DTO sortant generique (record) | adapter/in/web | n/a (pure-value, ecrit complet) |

`BankApplication.java` edite (pas un scaffold) pour declarer le bean `statementUseCase` — le
constructeur `new StatementService(accountRepository)` ne leve jamais d'exception (gate Walking
Skeleton : demarrage du contexte Spring, separe du RED au niveau methode).

Detecter les scaffolds : `grep -r "SCAFFOLD: true" src/main/`

---

## Wave: DISTILL / [REF] Test Placement

| Artefact | Path |
|---|---|
| Feature files | `src/test/resources/features/transaction-history/` |
| Step definitions | `src/test/java/com/softcrafts/bankkata/acceptance/steps/StatementSteps.java` |
| Cucumber Spring config | Reutilisee — `src/test/java/com/softcrafts/bankkata/acceptance/config/CucumberSpringConfiguration.java` (Phase 1, inchangee) |
| Cucumber runner | `src/test/java/com/softcrafts/bankkata/acceptance/TransactionHistoryAcceptanceTest.java` |

Precedent : meme layout Maven/Spring Boot + Cucumber-JVM que Phase 1. Glue package identique
(`com.softcrafts.bankkata.acceptance`) — `AccountManagementSteps` et `StatementSteps` partagent
le meme contexte Spring et le meme hook `@Before` (`AccountManagementSteps.resetAccountState()`,
reutilise sans duplication).

---

## Wave: DISTILL / [REF] Driving Adapter Coverage

| Driving adapter entry point | Protocol | Covered by |
|---|---|---|
| `GET /api/statement` (sans parametre) | MockMvc HTTP GET | Walking Skeleton (Scenario 1), Scenarios 2-4 |
| `GET /api/statement?page&size` | MockMvc HTTP GET | Scenarios 5-9 (pagination) |
| `GET /api/statement?from&to[&page&size&sortBy&sortDir]` | MockMvc HTTP GET | Scenarios 10-17 (filtre + tri) |

Seul nouveau driving adapter de Phase 2 (`StatementController`) entierement couvert. Zero point
d'entree non couvert. `AccountController` (Phase 1) reste couvert par `AccountManagementAcceptanceTest`,
inchange.

---

## Wave: DISTILL / [REF] Pre-requisites

| Pre-requisite | Source | Status |
|---|---|---|
| Stack Phase 1 inchangee (Spring Boot 3.x, Cucumber-JVM 7.18, JUnit Platform Suite) | brief.md "Stack technologique — Phase 2 (delta)" | Deja sur le classpath — aucun ajout |
| `StatementUseCase` bean declare dans `BankApplication` | Scaffold (cette session) | Fait — voir edition `BankApplication.java` |
| `StatementController` mappe `/api/statement` et demarre sans erreur de contexte | Scaffold (cette session) | Verifie — `mvn test` confirme RED (AssertionError), pas BROKEN |
| Reflexion test-only pour backdating des transactions | Resolution DISTILL (upstream-issues.md Finding 1) | Implementee dans `StatementSteps.seedTransaction()` |
| `InMemoryAccountRepository.reset()` (Phase 1) | Scaffold Phase 1, deja implemente | Reutilise sans modification |

**Integration build frontend (action Forge — Final Wave Review Gate)** : `frontend-maven-plugin`
(deja configure dans `pom.xml`, phases `install-node-and-npm` / `npm-install` / `npm-build` /
`npm-test`) couvre automatiquement les nouveaux composants frontend de DELIVER (`StatementPage`,
`DateRangeFilter`, `TransactionList`, `TransactionDetail`, `PageSizeSelector`,
`PaginationControls`) — aucune configuration additionnelle requise, `mvn test`/`mvn package`
declenche `npm test`/`npm run build` sans etape manuelle separee.

DEVOPS environment : aucun wave DEVOPS dedie pour cette feature (meme situation que Phase 1) —
degradation gracieuse appliquee, matrice par defaut : environnement `local` uniquement (voir
`docs/feature/phase1-account-management/environments.yaml` pour la matrice projet partagee).

---

## Wave: DISTILL / [REF] Mandate-12 Step Reuse (informational)

```
TOTAL_OCCURRENCES (nouveaux .feature, transaction-history) = 72
UNIQUE_DECORATORS (StatementSteps.java, 32 nouveaux + 2 reutilises d'AccountManagementSteps) = 34
RATIO = 72 / 34 ~= 2.12x — plafond naturel pour cette feature (filtre x tri x pagination
combinables, beaucoup d'assertions JSON distinctes : totalElements/totalPages/page/content)
```

Conformite mecanique (criteres 1-3, substance — pas le ratio) : pas de module `domain_types.py`
equivalent introduit (paradigme OOP Java, pas de DSL parametre par enum type — les step
decorators delegent directement a MockMvc, zero logique metier inline dans les corps de step,
chaque step est <= 3 lignes). Critere 4 (ratio) documente ci-dessus, informational uniquement.

---

## Wave: DISTILL / [HANDOFF] DELIVER Wave Package

### Artefacts produits par DISTILL wave

| Artefact | Chemin | Statut |
|---|---|---|
| ATDD Infrastructure Policy | `docs/architecture/atdd-infrastructure-policy.md` | Mise a jour — ligne `StatementController` ajoutee (apply-if-exists) |
| Upstream issues | `docs/feature/phase2-transaction-history/distill/upstream-issues.md` | Cree — 2 findings, 0 bloquant |
| Red classification | `docs/feature/phase2-transaction-history/distill/red-classification.md` | Cree — RED confirme pour la bonne raison |
| Feature file — walking skeleton | `src/test/resources/features/transaction-history/walking-skeleton.feature` | Cree — 1 scenario actif |
| Feature file — releve pagine | `src/test/resources/features/transaction-history/statement.feature` | Cree — 8 scenarios @skip |
| Feature file — filtre par date | `src/test/resources/features/transaction-history/date-range-filter.feature` | Cree — 8 scenarios @skip |
| Step definitions | `src/test/java/com/softcrafts/bankkata/acceptance/steps/StatementSteps.java` | RED verifie |
| Cucumber runner | `src/test/java/com/softcrafts/bankkata/acceptance/TransactionHistoryAcceptanceTest.java` | RED verifie |
| Scaffolds production (6 fichiers) | `src/main/java/com/softcrafts/bankkata/` | Voir [REF] Scaffolds |
| `BankApplication.java` | `src/main/java/com/softcrafts/bankkata/` | Edite — bean `statementUseCase` ajoute |

### Contrat pour le software-crafter (DELIVER wave)

- **Scenario actif** : Scenario 1 (walking-skeleton.feature) — deja actif, implementer
  `StatementService.getStatement()` + `StatementController.getStatement()`, GREEN, commit
- **Sequence DELIVER** : WS (deja ecrit) -> statement.feature (pagination, S2-S9) -> date-range-filter.feature (filtre+tri, S10-S17)
- **Un scenario a la fois** : retirer `@skip`, RED -> GREEN -> COMMIT, repeter
- **Pipeline obligatoire** (D11, ADR-005) : filtre par date -> tri -> pagination, dans cet ordre strict
- **Page hors limites** (D12) : 200 OK, `content: []`, metadonnees coherentes — jamais 400
- **Validation `size`** (D13) : whitelist stricte `{10, 20, 50}` -> 400 RFC 7807 si hors liste
- **`from > to`** (D5) : `InvalidDateRangeException` dediee -> 400 RFC 7807, handler local a `StatementController`
- **Mandate 12** : `StatementService` ne doit JAMAIS appeler `AccountRepository.save()` — test
  recommande au crafter : mock `AccountRepository`, `verify(never()).save(any())`
- **Signature `StatementUseCase`** : illustrative seulement (DESIGN a deliberement laisse le HOW
  au crafter) — peut etre raffinee librement en DELIVER tant que le contrat reste read-only
- **Frontend (DELIVER, hors Cucumber)** : `StatementPage`, `DateRangeFilter`, `TransactionList`,
  `TransactionDetail`, `PageSizeSelector`, `PaginationControls` + tests Vitest pour les 6
  scenarios frontend-only identifies dans upstream-issues.md Finding 2
- **Reset** : `InMemoryAccountRepository.reset()` (Phase 1, reutilise) — deja appele par le hook
  `@Before` partage d'`AccountManagementSteps`, aucune duplication necessaire dans `StatementSteps`

### Validation effectuee

- `mvn test` (TransactionHistoryAcceptanceTest + ArchitectureTest + AccountManagementAcceptanceTest) :
  RED confirme pour la bonne raison sur le scenario WS, 0 regression Phase 1 (16/16 PASS),
  ArchUnit 3/3 PASS (frontieres hexagonales respectees par les nouveaux composants)
- Reconciliation wave-decisions : 0 contradiction
- 2 findings upstream documentes et resolus dans le perimetre DISTILL (pas de blocage utilisateur requis)

---

## Final Wave Review Gate (4 reviewers, 2026-06-16)

| Reviewer | Wave couvert | Verdict | Blockers | High | Low |
|---|---|---|---|---|---|
| `nw-product-owner-reviewer` (Eclipse) | DISCUSS | **approved** | 0 | 0 | 1 (informational, deja resolu en DESIGN) |
| `nw-solution-architect-reviewer` (Atlas) | DESIGN | **approved** | 0 | 0 | 0 |
| `nw-platform-architect-reviewer` (Forge) | DEVOPS | **conditionally_approved** | 0 | 1 | 2 |
| `nw-acceptance-designer-reviewer` (Sentinel) | DISTILL | **conditionally_approved** | 0 | 0 | 4 (informational) |

### Actions issues du gate

| # | Reviewer | Severite | Finding | Action |
|---|---|---|---|---|
| 1 | Forge | high | `ci.yml` ne lancait que `AccountManagementAcceptanceTest`, jamais le nouveau runner | **APPLIQUEE immediatement** — `ci.yml` etape "Unit Tests" exclut desormais les 2 runners Cucumber (`!AccountManagementAcceptanceTest,!TransactionHistoryAcceptanceTest`), etape "Acceptance Tests" liste les 2 explicitement. Verifie localement (`mvn -o test`) : Unit Tests = ArchitectureTest seul (3/3 PASS) ; Acceptance Tests = les 2 suites (16/16 + 17 dont 1 RED attendu) |
| 2 | Forge | medium | Pas de gate CI defensif validant la presence du scenario `@walking_skeleton` | Differee — action DELIVER scope, non bloquante (le scenario WS est deja verifie present et RED dans `red-classification.md`) |
| 3 | Forge | medium | Integration build frontend non documentee dans Pre-requisites | **APPLIQUEE immediatement** — note ajoutee section [REF] Pre-requisites (frontend-maven-plugin deja configure, aucune action additionnelle) |
| 4 | Sentinel | low x4 | Signature `StatementUseCase` (List vs PageResponse), marqueurs RED defensifs, ratio step-reuse, tracabilite scenarios frontend-only | Informational — deja couvertes par le Contrat DELIVER ci-dessus (signature illustrative explicitement signalee, frontend Vitest scope documente) |

**Statut gate : CLEAR.** 0 blocker, 1 high resolu dans cette session, le reste documente comme
action items DELIVER (conforme a la regle "APPROVED ou CONDITIONALLY_APPROVED avec action items
documentes" du gate). **Handoff DELIVER autorise.**
