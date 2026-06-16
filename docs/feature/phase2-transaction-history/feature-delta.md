<!-- markdownlint-disable MD024 -->
# Feature Delta : phase2-transaction-history

**Projet** : Bank Application — Web Banking
**Wave** : DISCUSS
**Date** : 2026-06-16
**Statut** : Pret pour DESIGN wave — peer review approuvee (1 iteration, 2 issues HIGH remediees)

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
