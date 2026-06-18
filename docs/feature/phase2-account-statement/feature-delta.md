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
