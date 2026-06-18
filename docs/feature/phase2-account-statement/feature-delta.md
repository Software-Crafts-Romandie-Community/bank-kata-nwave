<!-- markdownlint-disable MD024 -->
# Feature Delta : phase2-account-statement

**Projet** : Bank Application — Web Banking
**Wave** : DISCUSS
**Date** : 2026-06-18
**Statut** : Prêt pour DESIGN wave

---

## Changed Assumptions

### Périmètre décidé en session (2026-06-18)

| Question | Décision | Rationale |
|----------|----------|-----------|
| Scope (filtres ?) | Afficher toutes les transactions sans filtre | Scope minimal livrable — filtre par date déféré à Phase 3+ |
| Format | REST endpoint + UI React | Cohérence avec Phase 1 — même stack |
| Running balance | Non — montant + type + date uniquement | Simplicité, pas de complexité de cumul |
| JTBD | Nouveau `job-002` distinct de `job-001` | Job distinct : consulter l'historique ≠ gérer le compte |

---

## Wave: DISCUSS / [REF] JTBD Analysis

### Job Statement

> When I need to understand what happened on my bank account,
> I want to see all my transactions with their type and date from a browser,
> so I can verify my account history and reconcile my finances without calling my bank.

**Job ID** : `job-002` (voir `docs/product/jobs.yaml`)

### Dimensions du Job

| Dimension | Contenu |
|-----------|---------|
| Fonctionnel | Voir toutes les transactions (dépôt/retrait) avec montant, type et date — depuis le même navigateur que Phase 1 |
| Émotionnel | Rassurance (les chiffres correspondent), confiance (rien n'est perdu), lisibilité (tableau clair) |
| Social | Reconcilier son compte de façon autonome sans devoir contacter la banque |

### Four Forces

| Force | Description |
|-------|-------------|
| Push | Après plusieurs opérations, Marie voit seulement le solde final — elle ne peut pas vérifier ce qui s'est passé |
| Pull | Un relevé lisible dans le navigateur : chaque opération avec sa date, son type et son montant |
| Anxiety | Peur que la liste affichée ne corresponde pas aux opérations réellement enregistrées |
| Habit | Habitude du relevé papier ou PDF de sa banque traditionnelle — l'affichage doit être aussi clair |

### JTBD-to-Story Bridge

| Step job map | Story candidate |
|---|---|
| Define : savoir quelles opérations ont eu lieu | slice-01 : afficher la liste via GET /api/statement |
| Locate : trouver une transaction spécifique | Hors scope Phase 2 (pas de filtre) — déféré |
| Confirm : vérifier que le solde correspond | slice-01 : solde rappelé en bas du relevé |
| Monitor : surveiller la cohérence | Property : `sum(transactions) == balance` — guardrail UAT |

---

## Wave: DISCUSS / [REF] Scope Assessment

**Résultat : PASS** (voir aussi `discuss/story-map.md`)

| Critère | Valeur | Seuil | Résultat |
|---------|--------|-------|----------|
| User Stories | 1 | <= 10 | PASS |
| Bounded Contexts | 2 (Domain + HTTP Adapter/Frontend) | <= 3 | PASS |
| Integration points walking skeleton | 3 (Browser → Spring Boot → Account) | <= 5 | PASS |
| Effort estimé | ~1 jour | <= 2 semaines | PASS |
| Outcomes indépendants séparables | 1 | — | Non fragmenté |

---

## Wave: DISCUSS / [REF] Journey Design

### Persona

**Marie** — Cliente bancaire, 35 ans.
Utilise ses applications web pour gérer ses finances au quotidien.
Ne connaît pas la programmation. Attend un relevé lisible et fidèle à ses opérations.
Veut reconcilier son solde avant de fermer le navigateur.

### Happy Path

```
[Page principale]  →  [Clic "Relevé"]  →  [Tableau transactions]  →  [Reconciliation]
  Phase 1 acquis       Déclenche             Affiché :                 Marie confirme :
  Solde visible        GET /api/statement    Date | Type | Montant      total = solde ✓
```

### Emotional Arc

```
Curiosité   →   Attention   →   Lecture   →   Rassurance
(je veux       (la page        (je parcours    (tout correspond,
vérifier)       charge ?)       les lignes)     mon compte est juste)
```

### Shared Artifacts

| Artefact | Source unique | Consommateurs |
|----------|---------------|---------------|
| `balance` | `Account` domain object | Page principale (Phase 1), pied de page relevé (Phase 2) |
| `transactions` | `Account.getTransactions()` | Tableau du relevé (toutes les lignes) |
| `transaction.type` | `Transaction.type` (enum) | Colonne Type + signe du montant |
| `transaction.amount` | `Transaction.amount` (BigDecimal) | Colonne Montant |
| `transaction.timestamp` | `Transaction.timestamp` (Instant) | Colonne Date + tri décroissant |

### Error Paths

| Erreur | Déclencheur | Comportement attendu |
|--------|-------------|----------------------|
| Aucune transaction | Session démarrée sans opération | Message "Aucune transaction enregistrée dans cette session." |
| Serveur non démarré | Accès avant boot | Erreur réseau navigateur (même traitement que Phase 1) |

---

## Wave: DISCUSS / [REF] User Story Map

### Backbone

| Accéder au relevé | Charger la liste | Lire et vérifier |
|-------------------|-----------------|-----------------|
| Cliquer "Relevé" | GET /api/statement → tableau | Reconcilier avec solde |
| — | État vide si 0 transactions | — |

### Walking Skeleton = Release 1 (fusionnés)

1. Marie clique "Relevé" sur la page principale
2. `GET /api/statement` retourne la liste JSON triée
3. Tableau affiché : Date | Type | Montant
4. Solde actuel rappelé en bas
5. État vide géré

### Priority Rationale

1. **slice-01** (WS = R1) : seul slice — walking skeleton ET valeur complète fusionnés.
   Hypothèse à invalider : `Account.getTransactions()` suffit sans modification du domaine.

---

## Wave: DISCUSS / [REF] System Constraints

- **Framework** : Spring Boot 3.x — cohérence Phase 1
- **Langage** : Java 21 LTS (inchangé)
- **Frontend** : React 18 + TypeScript + Vite — nouveau composant `StatementView` dans `frontend/`
- **Persistance** : InMemoryAccountRepository — en mémoire uniquement (cohérence Phase 1)
- **Type monétaire** : `BigDecimal` pour tous les calculs (ADR-002 conservé)
- **Format JSON** : montants sérialisés en Number avec 2 décimales (cohérence Phase 1)
- **Nouvel endpoint** : `GET /api/statement` → `List<TransactionDto>` triée chronologique inverse
- **Codes HTTP** : 200 OK (liste, même vide) — pas de 404 si liste vide
- **Signe du montant** : `+` pour DEPOSIT, `-` pour WITHDRAWAL — rendu côté React
- **Format date UI** : `dd/MM/yyyy HH:mm` (timezone locale navigateur) — formatage côté React
- **Pas de filtre** : toutes les transactions de la session, sans paramètre de requête
- **Pas de running balance** : montant + type + date uniquement (décision 2026-06-18)
- **Rétrocompatibilité** : Phase 1 inchangée (GET /api/balance, POST /api/deposit, POST /api/withdraw)

---

## Wave: DISCUSS / [REF] User Stories

Voir fichier dédié dans `docs/feature/phase2-account-statement/slices/`.

### Index des stories

| Slice | Fichier | Titre | Job ID | Release |
|-------|---------|-------|--------|---------|
| WS+R1 | slice-01-statement-api-and-ui.md | Relevé de compte — GET /api/statement → tableau transactions | job-002 | Walking Skeleton (= Release 1) |

---

## Wave: DISCUSS / [REF] Outcome KPIs

Voir `docs/feature/phase2-account-statement/discuss/outcome-kpis.md`

### North Star

**Marie peut vérifier chaque opération de sa session et confirmer que son solde est juste
— sans appeler la banque.**

### Guardrails

- Cohérence solde : `sum(transactions) == balance` — 0 écart toléré
- 0 régression Phase 1 : les 4 scénarios Phase 1 restent GREEN
- Aucune logique métier dans le controller HTTP (ADR-001 conservé)

---

## Wave: DISCUSS / [REF] Definition of Ready

| DoR Item | Résultat | Evidence |
|----------|----------|----------|
| Problème clair, langage domaine | PASS | Pain point "voit seulement le solde final" explicite — domaine bancaire |
| User/persona identifié | PASS | Marie — cliente bancaire, 35 ans, fin de session, veut reconcilier |
| 3+ exemples domaine avec données réelles | PASS | Marie (4 transactions), Thomas (état vide), Sofia (montants décimaux) |
| Scénarios UAT Given/When/Then (3-7) | PASS | 5 scénarios — liste complète, JSON, état vide, décimales, cohérence solde |
| AC dérivés des UAT | PASS | 7 AC extraits directement des scénarios |
| Right-sized (1-3 j, 3-7 scénarios) | PASS | 1 slice, ~1 jour, 5 scénarios |
| Notes techniques | PASS | Technical Notes dans slice-01, System Constraints ici |
| Dépendances tracées | PASS | Phase 1 walking skeleton requis (documenté), `Account.getTransactions()` existant |
| Outcome KPIs définis | PASS | outcome-kpis.md avec North Star et Guardrails |

**Statut DoR global : PASSED**

---

## Wave: DISCUSS / [HANDOFF] DESIGN Wave Package

### Artefacts produits

| Artefact | Chemin | Destinataire |
|----------|--------|-------------|
| Jobs YAML (mis à jour) | `docs/product/jobs.yaml` | solution-architect, acceptance-designer |
| Journey YAML relevé | `docs/product/journeys/account-statement.yaml` | solution-architect, acceptance-designer |
| Journey visuel | `docs/feature/phase2-account-statement/discuss/journey-account-statement-visual.md` | solution-architect |
| Story map | `docs/feature/phase2-account-statement/discuss/story-map.md` | solution-architect |
| Shared artifacts registry | `docs/feature/phase2-account-statement/discuss/shared-artifacts-registry.md` | solution-architect, acceptance-designer |
| Outcome KPIs | `docs/feature/phase2-account-statement/discuss/outcome-kpis.md` | solution-architect, platform-architect |
| Feature delta (ce fichier) | `docs/feature/phase2-account-statement/feature-delta.md` | solution-architect |
| Slice-01 | `docs/feature/phase2-account-statement/slices/slice-01-statement-api-and-ui.md` | solution-architect |

### Décisions ouvertes pour DESIGN wave

| # | Question | Impacte |
|---|----------|---------|
| D1 | DTO de sortie : `TransactionDto` ou réutilisation de `Transaction` record directement ? | Structure de l'adaptateur HTTP — isolation domaine/DTO |
| D2 | Tri côté domaine (`AccountService.getStatement()`) ou côté adaptateur HTTP ? | Responsabilité du tri — clean architecture |
| D3 | Navigation React : router dédié (`react-router`) ou affichage conditionnel (`showStatement` state) ? | Complexité frontend — cohérence avec architecture Phase 1 |
| D4 | Format date JSON : ISO 8601 (Instant.toString()) ou Long (epoch ms) ? | Formatage côté React — timezone handling |
| D5 | Accessibilité WCAG 2.1 AA : niveau cible confirmé ? Contrastes couleurs, balises `<th>` sémantiques, attributs `aria-label` ? | AC #7 du slice — décision DESIGN |
| D6 | Perspective support : procédure de réclamation si un client signale une transaction manquante ? | Hors scope Phase 2 (en mémoire) — à documenter pour Phase 3 avec persistance |

### Risques identifiés

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Régression Phase 1 — ajout endpoint rompt les tests existants | Faible | Moyen | Tests Phase 1 tournent dans le même contexte Spring — à vérifier en DESIGN |
| Tri incohérent si deux transactions ont le même timestamp (Instant) | Faible | Faible | Décision DESIGN : ordre stable par insertion si timestamps égaux |
| Formatage date timezone : Instant UTC affiché incorrectement | Moyen | Moyen | Formatage côté React avec `Intl.DateTimeFormat` — décision DESIGN |

---

## Wave: DESIGN / [REF] DDD Decision List

| # | Question | Verdict | Rationale |
|---|----------|---------|-----------|
| D1 | DTO de sortie : `TransactionDto` (nouveau record) ou `Transaction` directement ? | **TransactionDto** — CREATE NEW | `Transaction` est un objet domaine avec `Instant` — exposer le type domaine dans le contrat HTTP couplait l'évolution du domaine au contrat API. `TransactionDto(String type, BigDecimal amount, String date)` est le contrat HTTP stable. Mapping dans `AccountController`. |
| D2 | Tri par `timestamp` : côté domaine, use case ou adaptateur ? | **Use case** (`AccountUseCase.getStatement()`) | "Afficher les transactions dans l'ordre chronologique inverse" est une règle de consultation métier, pas de présentation. Le controller a zéro logique métier (confirmé par son Javadoc). Le tri au niveau use case est testable à la couche application sans HTTP. L'ordre stable en cas de timestamps identiques : ordre d'insertion (comportement de `ArrayList` conservé). |
| D3 | Navigation React : `react-router` ou état conditionnel ? | **État conditionnel** (`showStatement: boolean`) | Phase 1 n'a pas de router. Ajouter `react-router` pour une seule vue alternative violerait YAGNI et introduit une dépendance non justifiée. `showStatement` state dans `App.tsx` — bouton "Relevé" → `setShowStatement(true)`, bouton "Retour" dans `StatementView` → `setShowStatement(false)`. |
| D4 | Format date JSON : ISO 8601 ou epoch ms (Long) ? | **ISO 8601** (`Instant.toString()` → `"2026-06-18T14:32:00Z"`) | Standard universel, lisible, interopérable. Formatage `dd/MM/yyyy HH:mm` côté React via `Intl.DateTimeFormat` avec timezone locale du navigateur — aucune perte de précision, aucune lib supplémentaire. |
| D5 | Accessibilité WCAG 2.1 AA : niveau et exigences minimales ? | **WCAG 2.1 AA confirmé** | Exigences minimales (AC#7) : `<th scope="col">` sur chaque en-tête de colonne, `aria-label` sur le bouton "Relevé" si accompagné d'une icône seule, contraste texte/fond >= 4.5:1, navigation clavier (tab order naturel), indicateur de focus visible (`:focus-visible`). |
| D6 | Procédure de réclamation si transaction manquante ? | **Hors scope Phase 2 — déféré Phase 3** | La persistance en mémoire (InMemoryAccountRepository) ne permet pas de piste d'audit fiable entre sessions. La procédure de réclamation requiert la persistance (Phase 3+). Documenté dans les questions ouvertes ci-dessous. |

---

## Wave: DESIGN / [REF] Component Decomposition

| Composant | Couche | Chemin | Changement | Contract Shape |
|-----------|--------|--------|------------|----------------|
| `AccountUseCase` | application/port/in | `application/port/in/AccountUseCase.java` | EXTEND — + `List<Transaction> getStatement()` | pure-function (retourne une vue — aucun effet de bord dans le port) |
| `AccountService` | application | `application/AccountService.java` | EXTEND — implémente `getStatement()` : récupère `account.getTransactions()`, trie par timestamp décroissant, retourne liste immuable | pure-function (lecture seule — aucune mutation) |
| `AccountController` | adapter/in/web | `adapter/in/web/AccountController.java` | EXTEND — + `GET /api/statement` : appel `accountUseCase.getStatement()` + mapping `Transaction → TransactionDto` + `200 OK List<TransactionDto>` | unbounded-preservation (traduit le plan domaine en réponse HTTP, ne mute pas) |
| `TransactionDto` | adapter/in/web | `adapter/in/web/TransactionDto.java` | CREATE NEW — Java record `TransactionDto(String type, BigDecimal amount, String date)` | pure-function (value object immuable — contrat HTTP) |
| `StatementView` | frontend | `frontend/src/StatementView.tsx` | CREATE NEW — composant React : tableau 3 colonnes (Date, Type, Montant) + état vide + solde en pied de tableau + bouton "Retour" | — (frontend) |
| `bankApi.ts` | frontend | `frontend/src/api/bankApi.ts` | EXTEND — + `getStatement(): Promise<TransactionDto[]>` — même pattern fetch que `getBalance()` | — (frontend) |
| `App.tsx` | frontend | `frontend/src/App.tsx` | EXTEND — + state `showStatement: boolean` + bouton "Relevé" → `setShowStatement(true)` + rendu conditionnel `StatementView` | — (frontend) |

Composants non modifiés : `Account`, `Transaction`, `InsufficientFundsException`, `AccountRepository`, `InMemoryAccountRepository`, `BankApplication`, `DepositRequest`, `WithdrawRequest`, `BalanceResponse`.

---

## Wave: DESIGN / [REF] Driving Ports

**Port primaire étendu : `AccountUseCase`** — couche `application/port/in`

Ajout au contrat comportemental existant :
- Consulter l'historique des transactions de la session → retourne la liste de toutes les transactions enregistrées, triée par ordre chronologique inverse (la plus récente en premier), sans modification de l'état du compte.

**Adaptateur driving étendu : `AccountController`** — couche `adapter/in/web`

Endpoint ajouté :
- `GET /api/statement` → `200 OK` avec corps JSON `[{type, amount, date}]` (liste de `TransactionDto`)
  - La liste est vide `[]` si aucune transaction n'a eu lieu — jamais de 404
  - Aucun paramètre de requête — toutes les transactions de la session

Règle d'isolation confirmée : le mapping `Transaction → TransactionDto` appartient exclusivement à `AccountController`. `AccountUseCase.getStatement()` retourne `List<Transaction>` (type domaine). La conversion de `Instant` en String ISO 8601 et le rendu de `Transaction.Type` en String se font dans l'adaptateur.

---

## Wave: DESIGN / [REF] Driven Ports

Aucun nouveau port secondaire pour Phase 2.

**`AccountRepository`** — REUSE AS-IS. `AccountService.getStatement()` appelle `account.getTransactions()` via l'instance `Account` déjà chargée par le repository. Aucune modification du contrat `AccountRepository` ni de `InMemoryAccountRepository`.

---

## Wave: DESIGN / [REF] Technology Choices

Stack Phase 1 **inchangée**. Aucune nouvelle dépendance Maven ni npm.

| Décision | Verdict | Justification |
|----------|---------|---------------|
| `TransactionDto` — sérialisation `BigDecimal` | Spring Boot Jackson par défaut (Number avec 2 décimales) | Cohérence avec `BalanceResponse` Phase 1 — pas de `@JsonFormat` supplémentaire requis si la même configuration Jackson globale est en place |
| `Instant` → String JSON | `Instant.toString()` → ISO 8601 natif Java | Aucune annotation Jackson nécessaire — `Instant` sérialisé en `"2026-06-18T14:32:00Z"` par défaut avec `spring.jackson.serialization.write-dates-as-timestamps=false` (à confirmer en configuration Spring Boot) |
| Formatage date dans l'UI | `Intl.DateTimeFormat` (API navigateur native) | Zéro dépendance npm — gère la timezone locale automatiquement |
| Navigation React | État conditionnel `boolean` dans `App.tsx` | Pas de `react-router` — YAGNI (une seule vue alternative) |
| Accessibilité | HTML sémantique natif (`<th>`, `<table>`, `aria-label`) | Pas de lib d'accessibilité tierce — WCAG 2.1 AA atteignable avec HTML natif |

Note de configuration : vérifier la présence de `spring.jackson.serialization.write-dates-as-timestamps=false` dans `application.properties`. Si absent, ajouter cette propriété pour garantir la sérialisation ISO 8601 de `Instant`. C'est la seule modification de configuration potentielle.

---

## Wave: DESIGN / [REF] Reuse Analysis

| Composant | Fichier | Overlap Phase 2 | Décision | Justification | Contract Shape |
|-----------|---------|-----------------|----------|---------------|----------------|
| `Account` | `domain/Account.java` | `getTransactions()` alimente le relevé | REUSE AS-IS | Méthode déjà implémentée Phase 1 — aucune modification domaine requise | bounded-change (univers = transactions propres à l'agrégat) |
| `Transaction` | `domain/Transaction.java` | `type`, `amount`, `timestamp` lus pour le relevé | REUSE AS-IS | Record immuable — lu via mapping dans `AccountController` | pure-function (value object) |
| `AccountUseCase` | `application/port/in/AccountUseCase.java` | Nouvelle méthode `getStatement()` | EXTEND | Ajout d'un contrat use case — une méthode vs CREATE NEW port séparé non justifié (même bounded context) | pure-function (contrat lecture seule) |
| `AccountService` | `application/AccountService.java` | Implémentation `getStatement()` | EXTEND | Implémente la méthode ajoutée à `AccountUseCase` — même service d'orchestration | pure-function (lecture + tri, aucune mutation) |
| `AccountController` | `adapter/in/web/AccountController.java` | Nouveau endpoint `GET /api/statement` | EXTEND | ~15 LOC ajoutées (endpoint + mapping) — pas de nouvelle classe controller justifiée | unbounded-preservation (traduit sans muter) |
| `InMemoryAccountRepository` | `adapter/out/InMemoryAccountRepository.java` | Aucun | REUSE AS-IS | Pas de modification requise — `getTransactions()` est sur `Account`, pas sur le repository | bounded-change (univers = singleton Account en mémoire) |
| `TransactionDto` | — | Nouveau contrat HTTP | CREATE NEW | Isolation domaine/HTTP — `Transaction` contient `Instant` et `Type` enum non sérialisables directement au format désiré ; `TransactionDto` est le contrat HTTP stable indépendant du domaine | pure-function (value object HTTP) |
| `StatementView` | `frontend/src/` | Nouveau composant React | CREATE NEW | Aucun équivalent existant — vue dédiée au tableau transactions | — |
| `bankApi.ts` | `frontend/src/api/bankApi.ts` | Nouvelle fonction `getStatement()` | EXTEND | Pattern fetch existant réutilisé — cohérence avec `getBalance()` | — |
| `App.tsx` | `frontend/src/App.tsx` | Bouton "Relevé" + état `showStatement` | EXTEND | Ajout conditionnel simple — pas de nouveau composant page | — |

---

## Wave: DESIGN / [REF] Open Questions

| # | Question | Statut | Action |
|---|----------|--------|--------|
| OQ-1 | Procédure de réclamation si transaction manquante | Déféré Phase 3 | Requiert persistance persistante (base de données) + piste d'audit entre sessions. Hors scope Phase 2 (in-memory). À traiter quand Phase 3 introduit la persistence. |
| OQ-2 | `spring.jackson.serialization.write-dates-as-timestamps` — présent dans `application.properties` ? | À vérifier DISTILL | Si absent, ajouter `spring.jackson.serialization.write-dates-as-timestamps=false` — sinon `Instant` sérialisé en tableau `[year, month, ...]` au lieu de String ISO 8601. |
| OQ-3 | Stabilité du tri si deux transactions ont le même `Instant` (résolution nanoseconde) | À préciser DISTILL | Ordre de tri stable = ordre d'insertion (comportement de `List.sort()` stable + `ArrayList` conserve l'ordre d'insertion). Préciser dans les AC du slice. |
| OQ-4 | `BigDecimal` sérialisé en Number avec 2 décimales dans `TransactionDto` | À confirmer DISTILL | Cohérence avec `BalanceResponse` Phase 1. Confirmer la configuration Jackson globale existante. |

---

## Wave: DESIGN / [HANDOFF] DISTILL Wave Package

### Artefacts produits

| Artefact | Chemin | Contenu |
|----------|--------|---------|
| Feature delta (ce fichier, sections DESIGN ajoutées) | `docs/feature/phase2-account-statement/feature-delta.md` | Décisions D1-D6, reuse analysis, composants, ports |
| Brief architecture (section Phase 2 ajoutée) | `docs/product/architecture/brief.md` | C4 mis à jour, composants Phase 2, stack confirmée |
| ADR-004 | `docs/product/architecture/adr-004-transaction-dto.md` | Isolation domaine/HTTP via TransactionDto |
| ADR-005 | `docs/product/architecture/adr-005-react-conditional-navigation.md` | Navigation React conditionnelle vs react-router |

### Contraintes établies pour DISTILL

1. **Rétrocompatibilité totale** : les 3 endpoints Phase 1 (`GET /api/balance`, `POST /api/deposit`, `POST /api/withdraw`) et leurs tests ne sont pas modifiés.
2. **Règle hexagonale** : `domain/` n'importe jamais `adapter/` ni `org.springframework.*` — ArchUnit confirme.
3. **Contrat du port** : `AccountUseCase.getStatement()` retourne `List<Transaction>` (type domaine). Le mapping vers `TransactionDto` se fait uniquement dans `AccountController`.
4. **`TransactionDto`** : Java record dans le package `adapter/in/web/` — `TransactionDto(String type, BigDecimal amount, String date)`.
5. **Format date JSON** : ISO 8601 (`Instant.toString()`) — vérifier `spring.jackson.serialization.write-dates-as-timestamps=false`.
6. **Tri** : chronologique inverse dans `AccountService.getStatement()` — ordre stable par insertion en cas de timestamps identiques.
7. **Navigation React** : état conditionnel `showStatement` dans `App.tsx` — pas de `react-router`.
8. **WCAG 2.1 AA** : `<th scope="col">`, `aria-label` sur bouton "Relevé", contraste >= 4.5:1, focus visible. Critères d'acceptance comportementaux (WHAT, non HOW) — à formaliser en DISTILL : (a) un utilisateur naviguant au clavier uniquement peut atteindre le bouton "Relevé" et déclencher l'affichage du tableau ; (b) un utilisateur naviguant au clavier peut parcourir les lignes du tableau et voir un indicateur de focus visible sur chaque cellule interactive ; (c) le contraste texte/fond est >= 4.5:1 sur tous les éléments de texte du tableau (mesurable via axe DevTools ou WCAG Contrast Checker).
9. **Aucune nouvelle dépendance** Maven ni npm.
10. **`GET /api/statement`** : répond `200 OK` avec liste vide `[]` si aucune transaction — jamais 404.

---

## Wave: DISTILL / [REF] Scenario List

| Scénario | Tags | Fichier feature |
|----------|------|----------------|
| The customer retrieves a non-empty statement after making a deposit | `@walking_skeleton @real-io @driving_port @US-WS2` | `walking-skeleton.feature` |
| The statement shows all transactions in reverse chronological order | `@real-io @driving_port @US-S1 @skip` | `statement-api.feature` |
| Each transaction in the statement has the correct JSON fields | `@real-io @driving_port @US-S1 @skip` | `statement-api.feature` |
| The statement returns an empty array when no transactions have occurred | `@real-io @driving_port @US-S1 @skip @error` | `statement-api.feature` |
| Decimal transaction amounts are preserved without loss of precision | `@real-io @driving_port @US-S1 @skip` | `statement-api.feature` |
| Statement amounts and current balance are consistent after mixed operations | `@real-io @driving_port @US-S1 @skip` | `statement-api.feature` |

---

## Wave: DISTILL / [REF] Walking Skeleton Strategy

Architecture of Reference appliquée par héritage Phase 1 (`--policy=inherit`) :

| Port class | Treatment | Mechanism |
|------------|-----------|-----------|
| Driving — `AccountController` | Real adapter | MockMvc via `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` |
| Driven internal — `AccountRepository` | Real bean | `InMemoryAccountRepository` (`@Component` Spring singleton), reset en `@Before` |
| Driven external / non-déterministe | Fake | Aucun en Phase 2 |

**Limitation Instant.now()** : l'horodatage est produit par `Instant.now()` directement dans
`Account.deposit()` et `Account.withdraw()` — non injecté comme port. Les tests de tri reposent
sur l'ordre naturel des appels MockMvc séquentiels (~1 ms entre appels). Risque de flakiness
négligeable en pratique (résolution nanosecondes de `Instant`).

---

## Wave: DISTILL / [REF] Adapter Coverage

| Adaptateur | Scénario @real-io | Couvert par |
|------------|------------------|-------------|
| `AccountController` — `GET /api/statement` | OUI | Walking skeleton + tous les scénarios statement-api |
| `InMemoryAccountRepository` | OUI (hérité Phase 1) | Tous les scénarios via contexte Spring |

Aucun nouveau port driven. `AccountRepository` REUSE AS-IS (décision DESIGN).

---

## Wave: DISTILL / [REF] Scaffolds

| Artefact | Chemin | Rôle |
|----------|--------|------|
| `walking-skeleton.feature` | `src/test/resources/features/account-statement/` | Walking skeleton — scénario activé, RED par 404 |
| `statement-api.feature` | `src/test/resources/features/account-statement/` | 5 scénarios `@skip` — activés un par un en DELIVER |
| `AccountStatementAcceptanceTest.java` | `src/test/java/com/softcrafts/bankkata/acceptance/` | Runner Cucumber → `features/account-statement` |
| `AccountStatementSteps.java` | `src/test/java/com/softcrafts/bankkata/acceptance/steps/` | Step definitions Phase 2 (partage le contexte Phase 1) |

**Scaffolds production** : aucun fichier stub production nécessaire.
Le test en RED est classifié MISSING_FUNCTIONALITY : `GET /api/statement` n'existe pas dans
`AccountController` → MockMvc retourne 404 → `andExpect(status().isOk())` lève `AssertionError`.
Les step definitions compilent sans importer `TransactionDto` (assertions via `jsonPath()`).

**Configuration ajoutée** : `spring.jackson.serialization.write-dates-as-timestamps=false`
dans `application.properties` → `Instant` sérialisé en ISO 8601 string (OQ-2 résolu).

---

## Wave: DISTILL / [REF] Test Placement

Convention Phase 1 reconduite :

| Artefact | Emplacement |
|----------|-------------|
| Feature files | `src/test/resources/features/account-statement/` |
| Test runners | `src/test/java/com/softcrafts/bankkata/acceptance/` |
| Step definitions | `src/test/java/com/softcrafts/bankkata/acceptance/steps/` |
| Rapports Cucumber | `target/cucumber-reports/account-statement.html` |

---

## Wave: DISTILL / [REF] Driving Adapter Coverage

| Driving Adapter | Entry point | Scénario @walking_skeleton |
|-----------------|-------------|---------------------------|
| `AccountController` | `GET /api/statement` | "The customer retrieves a non-empty statement after making a deposit" |

Tous les scénarios Phase 2 passent par `GET /api/statement` via MockMvc.
Les 3 endpoints Phase 1 (`GET /api/balance`, `POST /api/deposit`, `POST /api/withdraw`)
sont exercés en Given/Then steps — rétrocompatibilité confirmée structurellement.

---

## Wave: DISTILL / [REF] Pre-requisites

| Pré-requis | Statut |
|------------|--------|
| Phase 1 walking skeleton GREEN | Requis — steps Given partagés (`a new bank account`, `customer deposited`) |
| `Account.getTransactions()` accessible | VÉRIFIÉ — méthode présente dans `Account.java` Phase 1 |
| `spring.jackson.serialization.write-dates-as-timestamps=false` | FAIT — ajouté à `application.properties` |
| ATDD Infrastructure Policy — MockMvc + InMemoryAccountRepository | HÉRITÉ Phase 1 — `docs/architecture/atdd-infrastructure-policy.md` mis à jour |

---

## Wave: DISTILL / [REF] Non couvert par les AT automatisés (hors scope MockMvc)

Les critères d'acceptance suivants concernent le rendu React — non vérifiables via MockMvc.
Nécessitent une infrastructure browser (Playwright / Selenium) absente du projet.

| AC | Pourquoi non couvert | Action future |
|----|----------------------|---------------|
| AC #3 UI : message "Aucune transaction enregistrée..." affiché | Rendu React côté client | Playwright E2E en Phase 3+ |
| AC #4 UI : type "Dépôt"/"Retrait", montant signé +/- | Formatage React `StatementView` | Playwright E2E en Phase 3+ |
| AC #5 UI : solde pied de page identique à GET /api/balance | Composant React affiche solde séparé | Playwright E2E en Phase 3+ |
| AC #6 UI : date formatée `dd/MM/yyyy HH:mm` timezone locale | `Intl.DateTimeFormat` côté navigateur | Playwright E2E en Phase 3+ |
| AC #7 WCAG 2.1 AA : navigation clavier, focus visible, contraste | Browser-only | Audit axe-core + revue manuelle en DELIVER |
