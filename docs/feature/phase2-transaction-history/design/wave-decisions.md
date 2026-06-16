# Wave Decisions Summary — DESIGN — phase2-transaction-history

**Wave** : DESIGN
**Architecte** : Morgan (solution-architect nWave)
**Mode** : Propose
**Date** : 2026-06-16 (iteration 1) — **amende le 2026-06-16 (pagination/tri backend)**
**Statut** : Amendement APPROUVE (peer review 0 critical/high/medium, 2 low remediees) — pret pour handoff DISTILL

---

## Decisions cles — iteration 1

| # | Decision | Verrouille par | Resultat |
|---|----------|------------------|----------|
| D1 | Localisation endpoint | DISCUSS (confirme) | Nouveau `StatementController` dedie, `AccountController` non touche |
| D2 | Nouveau port driving `StatementUseCase` | Propose (ADR-004) | Port separe, read-only, pas d'extension d'`AccountUseCase` |
| D3 | Pas de nouveau port driven | Propose | Reutilisation `AccountRepository.load()` en lecture seule |
| D4 | Filtrage par date en couche application | DISCUSS (confirme) | `StatementService`, jamais le domaine |
| D5 | Exception dediee `InvalidDateRangeException` | Propose | Handler local au `StatementController`, distinct de `IllegalArgumentException` |
| D6 | Bornes UTC journee complete (00:00:00.000Z / 23:59:59.999Z) | DISCUSS assumption A2 — confirmee DESIGN | Pas de notion de fuseau utilisateur (pas d'auth) |
| D7 | Pas de router frontend | Propose (simplicite) | Etat local React, pas de nouvelle dependance npm |
| D8 | Pagination | DISCUSS (confirme iteration 1) | **SUPERSEDEE par D10/ADR-005 (amendement)** — voir ci-dessous |
| D9 | Format Number pour les montants | Correction factuelle (A1 DISCUSS erronee) | Code Phase 1 confirme Number deja en place |

## Decisions cles — amendement pagination/tri backend (post peer-review iteration 1)

**Declencheur** : exigence produit explicite (Sylvain Chabert) — "affichage pagine (avec tri),
pagination provenant du backend, meme en in-memory". Supersede D8.

| # | Decision | Mode | Resultat |
|---|----------|------|----------|
| D10 | Style API pagination | Verrouille produit (style evalue) | DTO custom `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`) — pas Spring Data `Pageable`/`Page` (dependance non justifiee) |
| D11 | Ordre filtre/tri/pagination | Propose -> Retenu | Filtre (date) -> Tri -> Pagination, ordre strict |
| D12 | Page hors limites | Propose -> Retenu | 200 OK, `content: []`, metadonnees coherentes — pas 400 |
| D13 | Validation `size` | Propose -> Retenu | Whitelist stricte `{10,20,50}` -> 400 si hors liste |

Contrat final `GET /api/statement` : `from`, `to` (slice-06, inchanges) + `page` (defaut 0),
`size` (defaut 20, `{10,20,50}`), `sortBy` (`date`|`amount`, defaut `date`), `sortDir`
(`asc`|`desc`, defaut `desc`). Reponse : `PageResponse<TransactionResponse>`.

## Artefacts produits/modifies

| Artefact | Chemin | Action |
|----------|--------|--------|
| Architecture brief | `docs/product/architecture/brief.md` | Iteration 1 : section `## Application Architecture — Phase 2` ajoutee. **Amendement** : sous-section `### Pagination et tri — Amendement Phase 2` ajoutee, table de decisions D8 marquee SUPERSEDED, D10-D13 ajoutees, Reuse Analysis amendee, ADR-005 reference |
| ADR-004 | `docs/product/architecture/adr-004-statement-read-side-extension.md` | Cree (iteration 1), inchange par l'amendement |
| ADR-005 (nouveau) | `docs/product/architecture/adr-005-backend-pagination-sorting.md` | **Cree (amendement)** — pagination/tri backend, supersession D8 |
| Feature delta | `docs/feature/phase2-transaction-history/feature-delta.md` | Iteration 1 : sections `## Wave: DESIGN / [REF] *` + `## Changed Assumptions` ajoutees. **Amendement** : section `## Wave: DESIGN / [REF] Amendement — Pagination et tri backend` ajoutee en fin de fichier (contenu anterieur preserve) |
| Wave decisions | `docs/feature/phase2-transaction-history/design/wave-decisions.md` | Ce fichier, amende |
| Upstream changes (nouveau) | `docs/feature/phase2-transaction-history/design/upstream-changes.md` | **Cree (amendement)** — impact precis sur AC slice-05/06/07, nouveaux scenarios Gherkin |
| Slices amendees | `slices/slice-05-ws-statement-api.md`, `slices/slice-06-date-range-filter.md`, `slices/slice-07-transaction-detail.md` | **Amendees** — AC + scenarios Gherkin ajoutes pour pagination/tri, AC existants reformules ou la forme de reponse change |

## C4 produits

- System Context (delta) — Mermaid, dans brief.md
- Container (delta) — Mermaid, dans brief.md
- Component (L3) — non produit : le sous-systeme (1 port, 1 service, 1 controller, 2 DTOs) ne
  justifie pas un niveau L3 dedie (< 5 composants internes au sens du seuil nWave)

## Reuse Analysis

**Iteration 1** : tableau complet dans brief.md section "Reuse Analysis (Phase 2) — HARD GATE" :
12 composants existants analyses (REUSE AS-IS x9, EXTEND x3), 8 nouveaux composants (CREATE NEW,
justifies).

**Amendement** : tableau additionnel dans brief.md section "Reuse Analysis — composants impactes
par l'amendement" : `StatementService`/`StatementController`/`StatementUseCase` passent de EXTEND
mineur a EXTEND avec contrat elargi ; `StatementResponse` remplace par `PageResponse<T>` (CREATE
NEW) ; `TransactionResponse` reste REUSE AS-IS (element interne inchange) ; `StatementPage`/
`TransactionList` (frontend) passent de CREATE NEW (iteration 1) a EXTEND ; `PageSizeSelector`/
`PaginationControls` (frontend) CREATE NEW.

## Quality Gates — auto-validation avant peer review

- [x] Requirements traces aux composants (job-002, slices 05/06/07 -> composants nommes, y compris amendement)
- [x] Component boundaries avec responsabilite unique (table Component Decomposition + impact amendement)
- [x] Choix technologiques en ADR avec alternatives (ADR-004 : 3 alternatives ; ADR-005 : 3 alternatives)
- [x] Quality attributes adressees (testabilite via read-only port, maintenabilite via isolation Phase 1, performance — tri/pagination en memoire sans risque a ce volume)
- [x] Dependency-inversion compliance (StatementService depend de ports, pas d'adapter — inchange par l'amendement)
- [x] C4 diagrams (L1+L2 Mermaid produits iteration 1 ; amendement : note explicite "inchange, voir diagramme iteration 1")
- [x] Integration patterns specifies (REST, query params, RFC 7807 — 4 nouveaux params documentes)
- [x] OSS preference validee (aucun nouvel ajout — DTO custom plutot que Spring Data, justifie ADR-005)
- [x] AC behavioral (stories amendees avec nouveaux AC/scenarios behavioral, pas d'implementation-coupling)
- [x] External integrations annotees — N/A, aucune integration externe
- [x] Enforcement tooling recommande (ArchUnit existant suffit, test ordre filtre->tri->pagination recommande)
- [x] Document Update / Back-Propagation effectue (`upstream-changes.md` + 3 slices amendees)
- [x] D8 marquee SUPERSEDED (pas effacee) avec justification explicite de la non-erreur
- [x] Peer review solution-architect-reviewer sur l'amendement — APPROUVE (iteration 1, 0 critical/high/medium, 2 low remediees, voir section Peer Review Proof ci-dessous)

## External Integrations

Aucune. Pas d'annotation contract testing necessaire pour ce perimetre (inchange par l'amendement).

---

## Peer Review Proof (solution-architect-reviewer)

### Iteration 1 (architecture initiale, avant amendement) — APPROVED

```yaml
review_id: "arch_rev_2026-06-16_phase2_transaction_history"
reviewer: "solution-architect-reviewer"
iteration: 1
approval_status: "approved"
critical_issues_count: 0
high_issues_count: 0
medium_issues_count: 0
low_issues_count: 0

strengths:
  - "Additive extension constraint honored absolutely — zero Phase 1 code modifications"
  - "ADR-004 exemplary — 3 alternatives fully evaluated with explicit rejection rationale; Mandate 12 contract shapes declared"
  - "C4 diagrams complete (L1 + L2 Mermaid, delta views)"
  - "Dependency inversion honored — StatementService depends on AccountRepository (read-only), testability via port mocking"
  - "Team capability aligned — no new technologies, ~2d estimate proportional to scope"
  - "Proportionality validated — no microservices/pagination/router over-engineering"

priority_validation:
  q1_largest_bottleneck: "UNCLEAR — derived from SPEC.md, not production KPI data (acceptable for kata)"
  q2_simple_alternatives: "ADEQUATE — 3 alternatives in ADR-004"
  q3_constraint_prioritization: "CORRECT — additive constraint strictly honored"
  q4_data_justified: "NO_DATA — kata greenfield, acceptable; flag for Phase 3+ data-driven prioritization"

compliance_checks:
  mandate_12_effect_isolation: "PASS"
  eo_architecture_test_passing: "VERIFIED — existing ArchUnit rules cover new packages without modification"
  additive_extension_verified: "PASS — 9 REUSE AS-IS + 3 EXTEND + 8 CREATE NEW, Phase 1 pristine"
```

**Gate status iteration 1 : CLEAR.**

### Amendement (pagination/tri backend) — APPROVED (iteration 1 de l'amendement, modele haiku)

```yaml
review_id: "arch_rev_2026-06-16_phase2_pagination_amendment"
reviewer: "solution-architect-reviewer"
iteration: 1
amendment_focus: "Pagination et tri backend — changement de contrat API pour GET /api/statement, supersession D8"
approval_status: "approved"
critical_issues_count: 0
high_issues_count: 0
medium_issues_count: 0
low_issues_count: 2

strengths:
  - "ADR-005 exemplary — 3 alternatives evaluees (DTO custom retenu, Spring Data rejete sur dependance absente, frontend-only rejete sur violation exigence produit)"
  - "D8 supersession transparente — marquee SUPERSEDED dans brief.md, justifiee 'decision correcte pour perimetre connu, contexte change post-review, pas invalidation retroactive'"
  - "Contrat API coherent bout-en-bout — memes champs/defauts/comportement page hors limites dans ADR-005 + brief D10-D13 + feature-delta + upstream-changes + 3 slices amendees"
  - "Ordre filtre->tri->pagination explicite et justifie partout"
  - "Document Update / Back-Propagation complet — upstream-changes.md + 3 slices amendees avec traces explicites"
  - "Contrainte additive Phase 1 respectee a 100% — Account/Transaction/AccountUseCase/AccountService/AccountRepository/AccountController REUSE AS-IS"
  - "Mandate 12 declare et verifiable — StatementUseCase reste read-only, aucun appel save()"
  - "AC behavioral, pas d'implementation-coupling — aucune signature de methode dans les documents"
  - "Aucun biais architectural — rejet Spring Data justifie par absence reelle de la dependance, pas une preference"

issues_identified:
  low:
    - issue: "Note C4 Container 'inchange' occultait le changement de DTO (StatementResponse -> PageResponse)"
      severity: "low"
      recommendation: "Preciser que 'inchange' s'applique au niveau C4 L2 uniquement, le contrat applicatif change"
      remediation: "APPLIQUEE — note de precision ajoutee dans brief.md section C4 Container — impact"
    - issue: "Ambiguite mineure sur le statut de StatementResponse (remplace vs supprime)"
      severity: "low"
      recommendation: "Clarifier que StatementResponse n'est plus utilise comme DTO de sortie, breaking change accepte au stade kata"
      remediation: "APPLIQUEE — precision ajoutee dans la table Reuse Analysis de brief.md"

priority_validation:
  q1_largest_bottleneck: "VERIFIED_PRODUCT_DRIVEN — exigence produit explicite post-review"
  q2_simple_alternatives: "ADEQUATE — 3 alternatives ADR-005"
  q3_constraint_prioritization: "CORRECT — extension additive, pas d'inversion"
  q4_data_justified: "JUSTIFIED — tracee a exigence produit + verification absence Spring Data"

mandate_12_compliance:
  effect_isolation_by_design: "PASS"
  contract_shape_declared: "PASS — pure read / bounded-change"
  driving_ports_read_write_split: "PASS — StatementUseCase reste strictement read-only"
```

**Gate status amendement : CLEAR (apres remediation des 2 issues low) — pret pour handoff DISTILL.**

### Remediation appliquee (issue-by-issue)

| Issue | Severite | Action |
|-------|----------|--------|
| Note C4 "inchange" occulte changement DTO | low | Precision ajoutee : "inchange" = niveau C4 L2 uniquement ; contrat applicatif (`StatementResponse` -> `PageResponse`) change separement |
| Ambiguite statut `StatementResponse` | low | Precision ajoutee dans Reuse Analysis : remplace integralement comme DTO de sortie, breaking change accepte (kata, aucun consommateur production) |

Aucune iteration 2 necessaire — 0 critical/high/medium, 2 low remediees immediatement.
