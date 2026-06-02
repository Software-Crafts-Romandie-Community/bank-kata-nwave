<!-- markdownlint-disable MD024 -->
# Feature Delta : phase1-account-management

**Projet** : Bank Application — Web Banking
**Wave** : DISCUSS
**Date pivot** : 2026-06-02
**Statut** : Pret pour DESIGN wave

**Pivot** : Fusionne depuis phase2-web-ui (2026-06-02)
**Remplace** : phase1-account-management (CLI kata) + phase2-web-ui (demo hexagonale)

---

## Changed Assumptions

### Source des anciens artefacts

| Artefact supersede | Chemin |
|--------------------|--------|
| Feature delta CLI kata | `docs/feature/phase1-account-management/feature-delta.md` (ce fichier avant pivot) |
| Feature delta web demo | `docs/feature/phase2-web-ui/feature-delta.md` (marquee SUPERSEDED) |

### Changements de fond

| Dimension | Avant (kata pedagogique) | Apres (web app bancaire) |
|-----------|--------------------------|--------------------------|
| Persona | Developpeur kata (atelier Software Crafts Romandie) | Marie — cliente bancaire grand public, 35 ans |
| JTBD | Pratiquer les patterns de conception | Gerer son compte bancaire depuis le navigateur |
| Interface | CLI stdin/stdout (`CLIAdapter`) | Navigateur + REST API (`HTTPAdapter` Spring Boot 3.x) |
| Driving port | `CLIAdapter` | `@RestController` Spring Boot |
| Framework | Aucun (Java pur) | Spring Boot 3.x |
| Persistance | En memoire (Phase 1) | En memoire (Phase 1 — inchange) |
| Perimetre | Phase 1 (domaine CLI) + Phase 2 (web UI separee) | Fusionne en Phase 1 unique |

### Rationale du pivot

Le projet a ete requalifie de kata pedagogique en application web bancaire standard.
Le JTBD "pratiquer les patterns de conception" (job-001 kata) et "demonstrer l'hexagonal en atelier"
(job-002 kata) sont obsoletes. Le nouveau job-001 cible une cliente bancaire reelle qui veut gerer
son compte depuis un navigateur — cas d'usage standard avec valeur utilisateur directe.

La fusion des deux phases simplifie la livraison : domaine + adaptateur HTTP + frontend sont
une seule unite coherente, non deux phases separees avec une dependance de pipeline.

---

## Wave: DISCUSS / [REF] JTBD Analysis

### Job Statement

> When I need to manage my bank account,
> I want to perform deposits and withdrawals and view my balance from a browser,
> so I can stay in control of my finances without visiting a branch.

**Job ID** : `job-001` (voir `docs/product/jobs.yaml`)

### Dimensions du Job

| Dimension | Contenu |
|-----------|---------|
| Fonctionnel | Voir le solde, deposer, retirer, voir confirmation ou erreur — tout depuis le navigateur |
| Emotionnel | Sentiment de controle, retour immediat apres chaque action, confiance que les montants sont justes |
| Social | Gerer ses finances de facon autonome sans passer par une agence |

### Four Forces

| Force | Description |
|-------|-------------|
| Push | Pas d'acces web au compte — obligation d'appeler ou de se deplacer en agence |
| Pull | Acces instantane au solde et aux operations depuis n'importe quel appareil |
| Anxiety | Peur de saisir un mauvais montant sans retour avant confirmation ; doute sur le succes de l'operation |
| Habit | Habitude du guichet automatique ou de l'appel telephonique — la web UI doit inspirer confiance |

### JTBD-to-Story Bridge

| Step job map | Story candidate |
|---|---|
| Define : savoir quel est mon solde | slice-01 : GET /api/balance → page HTML affiche le solde |
| Execute : effectuer une transaction | slice-02 : depot / slice-03 : retrait |
| Monitor : voir le resultat | Confirmation inline apres chaque POST (solde mis a jour) |
| Confirm : valider les regles metier | slice-04 : rejet 409 fonds insuffisants — domaine enforced |

---

## Wave: DISCUSS / [REF] Scope Assessment

**Resultat : PASS**

| Critere | Valeur | Seuil | Resultat |
|---------|--------|-------|----------|
| User Stories | 4 | <= 10 | PASS |
| Bounded Contexts | 2 (Domain + HTTP Adapter/Frontend) | <= 3 | PASS |
| Integration points walking skeleton | 3 (Browser → Spring Boot → Account) | <= 5 | PASS |
| Effort estime | ~2 jours | <= 2 semaines | PASS |
| Outcomes independants separables | 1 (application web complete Phase 1) | — | Non fragmente |

Note scope : les 4 slices forment une progression coherente et non fragmentee.
Chaque slice est end-to-end (navigateur → API → domaine → reponse → UI).

---

## Wave: DISCUSS / [REF] Journey Design

### Persona

**Marie** — Cliente bancaire, 35 ans.
Utilise ses applications web pour gerer ses finances au quotidien.
Ne connait pas la programmation. Attend un retour immediat apres chaque operation.
A peur de faire une erreur (mauvais montant, operation irreversible).

### Happy Path (4 etapes)

```
[Ouvrir navigateur]  →  [Voir solde]  →  [Deposer]   →  [Retirer]
  Charge :               Affiche :         Confirme :      Confirme :
  page HTML              "0,00 EUR"        + nouveau       + nouveau
  + 2 boutons            solde visible     solde affi.     solde affi.
  (Deposer/Retirer)      sans action       inline          inline
                         manuelle
```

### Emotional Arc

```
Curiosite  →  Orientation  →  Action  →  Satisfaction  →  Confiance
(page load)   (solde + UI)   (saisie   (confirmation    (solde correct,
                              montant)  immediate)       controle total)
```

### Shared Artifacts

| Artefact | Source unique | Consommateurs |
|----------|---------------|---------------|
| `balance` | `Account` domain object | Page HTML (fetch GET /api/balance), reponse POST /api/deposit, reponse POST /api/withdraw, message d'erreur 409 |
| `account_id` | `InMemoryAccountRepository` (singleton Spring) | Toutes les routes HTTP — compte unique en Phase 1 |

### Error Paths principaux

| Erreur | Declencheur | Code HTTP | Reponse attendue |
|--------|-------------|-----------|-----------------|
| Fonds insuffisants | retrait > solde | 409 Conflict | `{"error": "insufficient_funds", "balance": "<solde>"}` + message UI |
| Montant invalide (<= 0 ou non numerique) | saisie formulaire | 400 Bad Request | `{"error": "invalid_amount"}` + message UI inline |
| Serveur non demarre | acces avant demarrage | — | Erreur reseau navigateur (hors scope UI Phase 1) |

---

## Wave: DISCUSS / [REF] User Story Map

### Backbone

| Acceder a la page | Consulter le solde | Deposer | Retirer |
|-------------------|--------------------|---------|---------|
| Ouvrir le navigateur | Voir solde courant | Saisir montant depot | Saisir montant retrait |
| Page HTML chargee | Solde rafraichi apres operation | Voir confirmation + solde | Voir confirmation + solde |
| — | — | Voir erreur montant invalide | Voir erreur fonds insuffisants |

### Walking Skeleton

Slice minimale end-to-end connectant toutes les activites :

1. Demarrer le serveur Spring Boot
2. Ouvrir `http://localhost:8080` → la page HTML se charge
3. La page fait `GET /api/balance` → affiche "Solde : 0,00 EUR"
4. Deux boutons "Deposer" / "Retirer" sont visibles et cliquables

### Releases slicees par outcome

| Release | Slice | Outcome cible |
|---------|-------|---------------|
| Walking Skeleton | slice-01 | La pile navigateur → HTTP → domaine fonctionne end-to-end |
| Release 1 | slice-02 | Marie peut deposer un montant et voir le solde mis a jour |
| Release 2 | slice-03 | Marie peut retirer un montant valide et voir le solde decrementé |
| Release 3 | slice-04 | La regle "fonds insuffisants" est enforced par le domaine, traduite en 409 par l'API |

### Priority Rationale

1. **slice-01** (WS) : valide l'hypothese la plus risquee — la pile navigateur/HTTP/domaine fonctionne sans modification du domaine
2. **slice-02** : valide le circuit complet d'ecriture — POST → domaine → reponse JSON → mise a jour UI
3. **slice-03** : valide le retrait — symetrique au depot, valide aussi le cas solde = 0 (cas limite critique)
4. **slice-04** : valide la regle metier dans le domaine (isolation du rejet de la couche HTTP)

---

## Wave: DISCUSS / [REF] System Constraints

- **Framework** : Spring Boot 3.x — driving adapter HTTP (`@RestController`)
- **Langage** : Java 21 LTS (Temurin, Apache 2.0) — inchange depuis ADR-002
- **Frontend** : HTML vanilla + JavaScript fetch API — pas de framework JS (React, Vue hors scope Phase 1)
- **Persistance** : InMemoryAccountRepository — singleton Spring — en memoire uniquement (Phase 1)
- **Type monetaire** : `BigDecimal` pour tous les calculs (ADR-002 conserve)
- **Format JSON** : montants serialises en String avec 2 decimales (`"150.00"`)
- **Codes HTTP** : 200 OK (succes), 400 Bad Request (montant invalide), 409 Conflict (fonds insuffisants)
- **Solde = 0 apres retrait total** : autorise (decision conservee du DESIGN wave precedent)
- **Pas de persistance cross-session** : redemarrer le serveur remet le solde a zero
- **Port serveur** : 8080 par defaut
- **CORS** : non requis si HTML servi par le meme serveur Spring Boot (static resources)
- **Devise** : EUR — "X,XX EUR" dans l'UI, "X.XX" en JSON (separateur decimal anglais)
- **CLIAdapter** : supprime — n'est plus le driving port primaire

---

## Wave: DISCUSS / [REF] User Stories

Voir fichiers dedies dans `docs/feature/phase1-account-management/slices/`.

### Index des stories

| Slice | Fichier | Titre | Job ID | Release |
|-------|---------|-------|--------|---------|
| WS | slice-01-ws-balance-api.md | Walking Skeleton — GET /api/balance → page HTML | job-001 | Walking Skeleton |
| S1 | slice-02-deposit-api.md | POST /api/deposit → solde mis a jour dans l'UI | job-001 | Release 1 |
| S2 | slice-03-withdrawal-api.md | POST /api/withdraw → solde mis a jour OU erreur | job-001 | Release 2 |
| S3 | slice-04-insufficient-funds-api.md | Regle fonds insuffisants — 409 Conflict | job-001 | Release 3 |

### Stories supprimees (pivot)

Les 4 slices CLI kata suivantes sont remplacees par les slices web ci-dessus :

| Slice supprimee | Raison |
|-----------------|--------|
| slice-01-display-initial-balance.md | Persona et interface CLI obsoletes |
| slice-02-deposit.md | Persona et interface CLI obsoletes |
| slice-03-withdrawal.md | Persona et interface CLI obsoletes |
| slice-04-insufficient-funds.md | Migree vers slice-04-insufficient-funds-api.md (web) |

---

## Wave: DISCUSS / [REF] Outcome KPIs

### Objectif

> Marie consulte son solde, effectue un depot et un retrait depuis son navigateur
> en moins de 3 minutes, sans aide externe, avec une confirmation visuelle apres chaque operation.

### Outcome KPIs

| # | Qui | Fait quoi | De combien | Baseline | Mesure par | Type |
|---|-----|-----------|------------|----------|------------|------|
| 1 | Cliente bancaire (Marie) | Consulte son solde dans le navigateur sans action manuelle | 100 % des chargements de page affichent un solde valide | 0 % (greenfield) | Tests UAT end-to-end | Leading |
| 2 | Cliente bancaire (Marie) | Effectue un depot et voit le nouveau solde sans rechargement | 100 % des depots valides mettent a jour l'UI | 0 % (greenfield) | Tests UAT POST /api/deposit | Leading |
| 3 | Cliente bancaire (Marie) | Comprend immediatement pourquoi un retrait est refuse | 0 occurrence de solde negatif | Non mesure | Scenarios BDD 409 + guardrail | Guardrail |

### North Star

**Marie effectue un depot et un retrait complets dans le navigateur, avec confirmation visuelle a chaque etape, en moins de 3 minutes.**

### Guardrails

- Solde jamais negatif apres une operation (0 occurrence tolere)
- Aucune logique metier dans le controller HTTP (delegation totale a `AccountUseCase`)
- Temps de reponse API < 200 ms pour chaque endpoint en conditions normales

---

## Wave: DISCUSS / [REF] Definition of Ready

| DoR Item | Resultat | Evidence |
|----------|----------|----------|
| Probleme clair, langage domaine | PASS | Persona Marie, pain point "pas d'acces web au compte" explicit |
| User/persona identifie | PASS | Marie — cliente bancaire, 35 ans, grand public |
| 3+ exemples domaine avec donnees reelles | PASS | Marie, Thomas, Sofia — donnees dans chaque slice |
| Scenarios UAT Given/When/Then (3-7) | PASS | 3-5 scenarios par slice, tous en GWT |
| AC derives des UAT | PASS | AC extraits des scenarios dans chaque slice |
| Right-sized (1-3 j, 3-7 scenarios) | PASS | 4 slices x 0,5 j, 3-5 scenarios chacune |
| Notes techniques | PASS | System Constraints + Technical Notes dans chaque slice |
| Dependances tracees | PASS | Ordre de livraison WS → S1 → S2 → S3 documente |
| Outcome KPIs definis | PASS | Section KPIs ci-dessus avec North Star et Guardrails |

**Statut DoR global : PASSED**

---

## Wave: DISCUSS / [HANDOFF] DESIGN Wave Package

### Artefacts produits

| Artefact | Chemin | Destinataire |
|----------|--------|-------------|
| Jobs YAML (mis a jour) | `docs/product/jobs.yaml` | solution-architect, acceptance-designer |
| Journey YAML web | `docs/product/journeys/account-management.yaml` | solution-architect, acceptance-designer |
| Feature delta (ce fichier) | `docs/feature/phase1-account-management/feature-delta.md` | solution-architect |
| Slice-01 | `docs/feature/phase1-account-management/slices/slice-01-ws-balance-api.md` | solution-architect |
| Slice-02 | `docs/feature/phase1-account-management/slices/slice-02-deposit-api.md` | solution-architect |
| Slice-03 | `docs/feature/phase1-account-management/slices/slice-03-withdrawal-api.md` | solution-architect |
| Slice-04 | `docs/feature/phase1-account-management/slices/slice-04-insufficient-funds-api.md` | solution-architect |
| Phase2 superseded | `docs/feature/phase2-web-ui/SUPERSEDED.md` | solution-architect (information) |

### Decisions ouvertes pour DESIGN wave (Morgan)

| # | Question | Impacte |
|---|----------|---------|
| D1 | Format JSON exact des reponses (snake_case, champs obligatoires, enveloppe vs plat) | Contrat API frontend/backend — doit etre stable avant implementation |
| D2 | Strategie de gestion des exceptions Spring Boot (HandlerExceptionResolver vs @ControllerAdvice) | Structure de l'adaptateur HTTP et des tests d'integration |
| D3 | Serveur statique : Spring Boot sert le HTML vanilla depuis `src/main/resources/static/` | Evite le probleme CORS — a confirmer ou infirmer en DESIGN |
| D4 | Format d'affichage du solde dans l'UI ("0,00 EUR" vs "EUR 0.00" vs autre) | Experience utilisateur — coherence entre les 4 slices |

### Risques identifies

| Risque | Probabilite | Impact | Mitigation |
|--------|-------------|--------|------------|
| Logique metier copiee dans le controller HTTP | Moyenne | Eleve (regle "clean architecture" violee) | AC explicite slice-04 : zero condition dans l'adaptateur — enforced par ArchUnit |
| Perte de solde entre requetes (repository non singleton) | Moyenne | Eleve (demo cassee) | Spring scope singleton par defaut — a verifier en DESIGN |
| Frontend JS ne gere pas les codes 409 (traite comme erreur generique) | Faible | Moyen | AC explicite slice-04 : message d'erreur structuree attendu dans l'UI |
| Regression : tests CLI existants casses par ajout Spring Boot | Faible | Faible | CLIAdapter supprime — aucun test CLI a conserver |

---

## Wave: DESIGN / [REF] Architecture Overview

**Pivot** : Spring Boot 3.x remplace le kata CLI (Java pur, CLIAdapter).

Le projet est une application web bancaire standard. Le driving port primaire est desormais
`AccountController` (@RestController Spring Boot) au lieu de `CLIAdapter`. Le frontend HTML
vanilla est servi par Spring Boot depuis `src/main/resources/static/`. L'architecture hexagonale
(ADR-001) est confirmee et renforcee : le risque de contamination Spring dans le domaine est
adresse par des regles ArchUnit etendues.

La composition root passe de `Main.java` (instanciation manuelle) a `BankApplication.java`
(@SpringBootApplication — contexte IoC Spring). Le reste du domaine (Account, Transaction,
InsufficientFundsException, AccountUseCase, AccountRepository, AccountService) est inchange.

**ADRs impliques** : ADR-001 (confirme), ADR-002 (inchange), ADR-003 (nouveau — Spring Boot 3.x).

---

## Wave: DESIGN / [REF] Component Decomposition

| Composant | Couche | Pivot Decision | Justification |
|---|---|---|---|
| `Account` | domain | REUSE AS-IS | Regle "solde >= 0" independante du transport |
| `Transaction` | domain | REUSE AS-IS | Record immuable, independant du transport |
| `InsufficientFundsException` | domain | REUSE AS-IS | Exception metier pure |
| `AccountUseCase` | application/port/in | REUSE AS-IS | Spring Boot injecte l'implementation via l'interface |
| `AccountRepository` | application/port/out | REUSE AS-IS | Interface independante du framework |
| `AccountService` | application | REUSE AS-IS | Aucune dependance CLI ni Spring dans le service |
| `InMemoryAccountRepository` | adapter/out | EXTEND — @Component | Ajouter `@Component` pour l'injection Spring, singleton gere par le conteneur |
| `CLIAdapter` | adapter/in | **REMOVE** | Remplace par `AccountController` |
| `Main` | composition root | **REPLACE** | `@SpringBootApplication` remplace le `main()` manuel |
| `AccountController` | adapter/in/web | CREATE NEW | Driving port HTTP (@RestController) — 3 endpoints REST |
| `DepositRequest` | adapter/in/web | CREATE NEW | DTO entrant — montant du depot |
| `WithdrawRequest` | adapter/in/web | CREATE NEW | DTO entrant — montant du retrait |
| `BalanceResponse` | adapter/in/web | CREATE NEW | DTO sortant — solde courant |
| `BankApplication` | composition root | CREATE NEW | @SpringBootApplication — remplace Main |
| `index.html` + `app.js` | static resources | CREATE NEW | Frontend HTML vanilla |

---

## Wave: DESIGN / [REF] Driving Ports

**`AccountController`** (@RestController) — adaptateur driving HTTP, package `adapter.in.web`

| Endpoint | Methode HTTP | Corps | Reponse succes | Reponse erreur |
|---|---|---|---|---|
| `GET /api/balance` | GET | — | 200 + `BalanceResponse` JSON | — |
| `POST /api/deposit` | POST | `DepositRequest` JSON | 200 + `BalanceResponse` JSON | 400 montant invalide |
| `POST /api/withdraw` | POST | `WithdrawRequest` JSON | 200 + `BalanceResponse` JSON | 400 invalide / 409 fonds insuffisants |

Le controller ne contient aucune logique metier. Toute validation metier est deleguee a
`AccountUseCase`. Les exceptions domaine (`InsufficientFundsException`) sont traduites en
codes HTTP par le controller (ou un `@ControllerAdvice` — decision ouverte Q2).

**Static resources** : `index.html` + `app.js` servis par Spring Boot sur `GET /`.
Aucune logique metier dans les ressources statiques.

---

## Wave: DESIGN / [REF] Driven Ports & Adapters

**`AccountRepository`** (interface) → **`InMemoryAccountRepository`** (@Component, singleton Spring)

- Singleton Spring : etat du compte preserve entre les requetes HTTP pour la duree du processus
- Redemarrer le serveur remet le solde a zero (comportement documente Phase 1)
- `@Component` = seul changement par rapport au kata CLI — l'interface `AccountRepository` reste inchangee
- Probe au demarrage via `ApplicationRunner` Spring : valide creation, chargement, mutation,
  rechargement coherent ; echec = refus de demarrage avec evenement structure `health.startup.refused`

---

## Wave: DESIGN / [REF] Technology Stack

| Composant | Technologie | Version | Licence | Rationale |
|---|---|---|---|---|
| Runtime | Java | 21 LTS | GPL v2 + Classpath Exception | Inchange (ADR-002) |
| Framework web | Spring Boot | 3.x | Apache 2.0 | Standard industrie, REST natif, MockMvc pour tests (ADR-003) |
| Tests REST | MockMvc | Spring Boot Starter Test | Apache 2.0 | Tests integration REST sans serveur complet |
| Tests unitaires | JUnit 5 | 5.x | EPL 2.0 | Standard de facto Java |
| Assertions | AssertJ | 3.x | Apache 2.0 | Assertions fluides |
| Mocking | Mockito | 5.x | MIT | Isolation ports driven |
| Build | Maven ou Gradle | derniere stable | Apache 2.0 | Standard Java |
| Frontend | HTML + JavaScript | Vanilla | — | Minimalisme — pas de framework JS Phase 1 |
| Enforcement | ArchUnit | 1.x | Apache 2.0 | Verifie regles hexagonales ET absence imports Spring dans domain |

---

## Wave: DESIGN / [REF] Decisions Table

| # | Decision | Statut | ADR |
|---|---|---|---|
| D1 | Architecture hexagonale Ports & Adapters | Confirme et renforce (pivot web) | ADR-001 |
| D2 | Java 21 LTS | Inchange | ADR-002 |
| D3 | Spring Boot 3.x comme framework web | Nouveau — pivot 2026-06-02 | ADR-003 |
| D4 | CLIAdapter supprime | Confirme (DISCUSS) | — |
| D5 | Persistance en memoire Phase 1 (@Component singleton) | Confirme | ADR-001 |
| D6 | BigDecimal pour les montants | Inchange | ADR-002 |
| D7 | Pas d'authentification Phase 1 | Explicit — hors scope | ADR-003 |

---

## Wave: DESIGN / [REF] Reuse Analysis

| Composant existant (kata) | Fichier | Overlap | Decision | Justification |
|---|---|---|---|---|
| `Account` | `domain/Account.java` | Logique domaine | REUSE AS-IS | La regle "solde >= 0" est independante du transport |
| `Transaction` | `domain/Transaction.java` | Value object | REUSE AS-IS | Record immuable, independant du transport |
| `InsufficientFundsException` | `domain/` | Exception domaine | REUSE AS-IS | Exception metier pure |
| `AccountUseCase` | `application/port/in/` | Port primaire | REUSE AS-IS | Spring Boot injecte l'implementation via l'interface |
| `AccountRepository` | `application/port/out/` | Port secondaire | REUSE AS-IS | Interface independante du framework |
| `AccountService` | `application/` | Orchestration | REUSE AS-IS | Aucune dependance CLI ni Spring dans le service |
| `InMemoryAccountRepository` | `adapter/out/` | Stockage memoire | EXTEND (@Component) | Ajouter `@Component` — interface inchangee |
| `CLIAdapter` | `adapter/in/` | Driving port CLI | **REMOVE** | Remplace par `AccountController` |
| `Main` | `Main.java` | Point d'entree | **REPLACE** | `@SpringBootApplication` remplace le `main()` manuel |

---

## Wave: DESIGN / [REF] Open Questions

| # | Question | Impacte | Priorite |
|---|---|---|---|
| Q1 | Format JSON des reponses d'erreur (RFC 7807 Problem Details ?) | Contrat API frontend/backend | Avant implementation |
| Q2 | Gestion exceptions Spring : `@ControllerAdvice` vs logique dans le controller | Structure adapter HTTP et tests | Avant implementation |
| Q3 | Serialisation BigDecimal en JSON : String `"150.00"` vs Number `150.00` | Coherence frontend/backend | Avant implementation |
| Q4 | CORS si frontend sur port different | Hors scope Phase 1 (static resources meme serveur) | Phase 2 |
| Q5 | Authentification et sessions | Hors scope Phase 1 | Phase 3 |

---

## Wave: DESIGN / [HANDOFF] DISTILL Wave Package

### Destinataire
**acceptance-designer** (DISTILL wave)

### Artefacts produits par DESIGN wave

| Artefact | Chemin | Statut |
|---|---|---|
| Architecture brief (pivot) | `docs/product/architecture/brief.md` | Mis a jour — section "Application Architecture" reecrite |
| ADR-001 (confirme + renforce) | `docs/product/architecture/adr-001-hexagonal-oop.md` | Mis a jour — section "Update 2026-06-02" ajoutee |
| ADR-002 (inchange) | `docs/product/architecture/adr-002-java21.md` | Inchange |
| ADR-003 (nouveau) | `docs/product/architecture/adr-003-spring-boot.md` | Cree — Spring Boot 3.x |
| Feature delta (ce fichier) | `docs/feature/phase1-account-management/feature-delta.md` | Sections DESIGN ajoutees |

### Contrat pour l'acceptance-designer

L'acceptance-designer peut maintenant ecrire les acceptance tests pour les 4 slices en sachant :
- Le domaine (`Account`, `AccountService`) est testable sans Spring — JUnit 5 pur
- L'API REST est testable via MockMvc (`@WebMvcTest(AccountController.class)`) sans demarrer Tomcat
- Les endpoints sont : `GET /api/balance`, `POST /api/deposit`, `POST /api/withdraw`
- Les codes HTTP sont : 200 (succes), 400 (montant invalide), 409 (fonds insuffisants)
- La structure de reponse JSON est a confirmer (Q1, Q3 ci-dessus) avant implementation

### Points d'attention pour la DISTILL wave

1. **Q1 (format JSON erreur)** doit etre resolu avant d'ecrire les AC de slice-04 — la structure
   exacte de `{"error": "insufficient_funds", "balance": "X.XX"}` doit etre dans l'AC, pas dans
   l'implementation
2. **Q2 (@ControllerAdvice vs controller)** impacte la testabilite des erreurs en MockMvc —
   a documenter dans les notes techniques des slices
3. Les AC des slices doivent rester comportementaux (WHAT) — jamais de reference a
   `AccountController`, `@RestController`, ni a des methodes privees

### Paradigme de developpement

OOP — pour information du software-crafter (IMPLEMENT wave).
