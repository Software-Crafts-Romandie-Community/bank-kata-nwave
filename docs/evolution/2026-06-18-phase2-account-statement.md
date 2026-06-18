# Evolution — phase2-account-statement

**Date** : 2026-06-18
**Feature ID** : `phase2-account-statement`
**Job** : `job-002` — bank-account-statement-consultation
**Auteur** : Apex (nw-platform-architect)

---

## Résumé

Phase 2 étend l'application Bank Application (Phase 1 — Account Management) d'une fonctionnalité complète : consulter l'historique des transactions de la session via `GET /api/statement` et un composant React `StatementView`. Le domaine n'a pas été modifié. L'architecture hexagonale Phase 1 est respectée et renforcée par un nouveau DTO d'isolation.

**North Star** : Marie peut vérifier chaque opération de sa session et confirmer que son solde est juste — sans appeler la banque.

---

## Contexte métier

| Élément | Détail |
|---------|--------|
| Persona | Marie — cliente bancaire, 35 ans, utilise le web pour ses finances |
| Pain point | Après plusieurs opérations, Marie voit seulement le solde final — elle ne peut pas vérifier ce qui s'est passé |
| Job JTBD | `job-002` — consulter l'historique est un job distinct de gérer le compte (`job-001`) |
| Scope Phase 2 | Toutes les transactions sans filtre — filtre déféré Phase 3+ |
| Rétrocompatibilité | Phase 1 inchangée (GET /api/balance, POST /api/deposit, POST /api/withdraw) |

---

## Vagues exécutées

| Vague | Statut | Date | Artefacts clés |
|-------|--------|------|----------------|
| DISCUSS | Terminée | 2026-06-18 | `feature-delta.md` §DISCUSS, `discuss/outcome-kpis.md`, `discuss/journey-account-statement.yaml` |
| DESIGN | Terminée | 2026-06-18 | `feature-delta.md` §DESIGN, ADR-004, ADR-005, `docs/product/architecture/brief.md` §Phase 2 |
| DISTILL | Terminée | 2026-06-18 | `feature-delta.md` §DISTILL, 6 scénarios Cucumber, `distill/red-classification.md` |
| DELIVER | Terminée | 2026-06-18 | 7 steps TDD + 1 refactor — tous PASS |

---

## Décisions clés

### D1 — TransactionDto : isolation domaine/HTTP (ADR-004)

`Transaction` est un objet domaine contenant `Instant` et `Type` enum. Exposer le type domaine dans le contrat HTTP couplait l'évolution du domaine au contrat API. Décision : CREATE NEW `TransactionDto(String type, BigDecimal amount, String date)` comme contrat HTTP stable. Mapping dans `AccountController` uniquement. ADR-004 documenté dans `docs/product/architecture/`.

### D2 — Tri chronologique inverse : dans le use case

"Afficher les transactions dans l'ordre chronologique inverse" est une règle de consultation métier, pas de présentation. Le tri est placé dans `AccountService.getStatement()` — testable à la couche application sans HTTP. Ordre stable en cas de timestamps identiques : ordre d'insertion.

### D3 — Navigation React : état conditionnel (ADR-005)

Phase 1 sans router. Ajouter `react-router` pour une seule vue alternative violerait YAGNI. Solution : `showStatement: boolean` dans `App.tsx`. ADR-005 documenté dans `docs/product/architecture/`.

### D4 — Format date JSON : ISO 8601

`Instant.toString()` → `"2026-06-18T14:32:00Z"`. Standard universel, lisible. Formatage `dd/MM/yyyy HH:mm` côté React via `Intl.DateTimeFormat` — timezone locale, zéro dépendance npm. Configuration ajoutée : `spring.jackson.serialization.write-dates-as-timestamps=false`.

### D5 — Accessibilité WCAG 2.1 AA

Exigences minimales : `<th scope="col">`, `aria-label` sur bouton "Relevé", contraste texte/fond >= 4.5:1, focus visible (`:focus-visible`). Atteint avec HTML sémantique natif.

### D6 — Procédure réclamation : déféré Phase 3

La persistance en mémoire (InMemoryAccountRepository) ne permet pas de piste d'audit fiable entre sessions. Déféré Phase 3 avec persistance.

---

## Travail réalisé (exécution DELIVER)

### Roadmap : 7 steps TDD + 1 refactor

| Step | Nom | Phases | Résultat |
|------|-----|--------|---------|
| 01-01 | Walking skeleton — GET /api/statement end-to-end | RED / GREEN / COMMIT | PASS |
| 02-01 | Reverse chronological order — @skip activé | RED / GREEN / COMMIT | PASS |
| 02-02 | JSON field shape — @skip activé | RED / GREEN / COMMIT | PASS |
| 02-03 | Empty statement — @skip activé | RED / GREEN / COMMIT | PASS |
| 02-04 | Decimal precision — @skip activé | RED / GREEN / COMMIT | PASS |
| 02-05 | Balance consistency — @skip activé | RED / GREEN / COMMIT | PASS |
| 03-01 | Frontend React — StatementView, bankApi.ts, App.tsx | RED / GREEN / COMMIT | PASS |
| refactor-L1-L6 | Refactoring L1-L6 | GREEN / COMMIT (RED N/A) | PASS |

### Commits DELIVER (8 commits)

```
eb4dad3 feat(phase2-account-statement): add StatementView React component
7e185f3 feat(phase2-account-statement): activate balance consistency scenario
a5fdb7a feat(phase2-account-statement): activate decimal precision scenario
bd43fad feat(phase2-account-statement): activate empty statement scenario
ad0467f feat(phase2-account-statement): activate JSON field shape scenario
41f22b0 feat(phase2-account-statement): activate reverse chronological order scenario
e979e9d feat(phase2-account-statement): wire GET /api/statement end-to-end
```

### Composants implémentés

| Composant | Couche | Changement |
|-----------|--------|------------|
| `AccountUseCase` | application/port/in | EXTEND — + `List<Transaction> getStatement()` |
| `AccountService` | application | EXTEND — + `getStatement()` (tri timestamp décroissant) |
| `TransactionDto` | adapter/in/web | CREATE NEW — record `(String type, BigDecimal amount, String date)` |
| `AccountController` | adapter/in/web | EXTEND — + `GET /api/statement` + mapping `Transaction → TransactionDto` |
| `StatementView.tsx` | frontend | CREATE NEW — tableau Date/Type/Montant + état vide + solde + bouton Retour |
| `bankApi.ts` | frontend/api | EXTEND — + `getStatement(): Promise<TransactionDto[]>` |
| `App.tsx` | frontend | EXTEND — + `showStatement` state + bouton "Relevé" + rendu conditionnel |

---

## Validation finale

**Suite d'acceptance** : `mvn test` — **25 tests, 0 failures, BUILD SUCCESS**

| Suite | Tests | Résultat |
|-------|-------|---------|
| AccountStatementAcceptanceTest | 6/6 | GREEN |
| AccountManagementAcceptanceTest | 16/16 | GREEN (Phase 1 inchangée) |
| ArchitectureTest | 3/3 | GREEN |

**Scénarios slice-01 validés** :
- Walking skeleton : dépôt 200€ → GET /api/statement → 200 OK, 1 transaction
- Tri décroissant : [WITHDRAWAL 50, DEPOSIT 100, DEPOSIT 200]
- Champs JSON : type, amount, date (ISO 8601 non vide)
- Liste vide : GET /api/statement sans transaction → 200 OK, []
- Précision décimale : 149.99 et 0.01 exactement préservés
- Cohérence solde : DEPOSIT 300 + WITHDRAWAL 50 → balance 250.00

---

## Outcome KPIs — atteints

| KPI | Cible | Résultat |
|-----|-------|---------|
| GET /api/statement retourne la liste exacte | 100 % | 100 % — 6 scénarios GREEN |
| Cohérence solde : sum(transactions) == balance | 0 écart | 0 écart — Scenario 5 GREEN |
| État vide géré sans confusion | 100 % | 100 % — Scenario 3 GREEN |
| Aucune régression Phase 1 | 0 régression | 0 — 16 scénarios Phase 1 GREEN |

---

## Problèmes rencontrés et résolutions

| Problème | Résolution |
|----------|------------|
| `Instant` sérialisé en tableau JSON au lieu d'ISO 8601 | Ajout de `spring.jackson.serialization.write-dates-as-timestamps=false` dans `application.properties` (OQ-2 DISTILL) |
| Steps 02-01 à 02-05 : tous GREEN sans production code | Logique 01-01 déjà correcte — steps de vérification et activation @skip uniquement |

---

## Portée des tests automatisés — limites documentées

Les critères d'acceptance React (formatage UI, accessibilité WCAG, état vide visuel) ne sont pas couverts par MockMvc. Infrastructure browser (Playwright) absente.

| AC | Statut | Action future |
|----|--------|---------------|
| Message "Aucune transaction" côté React | Non couvert | Playwright E2E — Phase 3+ |
| Type "Dépôt"/"Retrait", montant signé | Non couvert | Playwright E2E — Phase 3+ |
| Solde en pied de page StatementView | Non couvert | Playwright E2E — Phase 3+ |
| Date formatée `dd/MM/yyyy HH:mm` | Non couvert | Playwright E2E — Phase 3+ |
| WCAG 2.1 AA accessibilité | Non couvert | axe-core audit + revue manuelle — Phase 3+ |

---

## Lessons learned

1. **Phase 2 sans modification domaine** : l'architecture hexagonale Phase 1 a permis d'étendre la fonctionnalité en touchant uniquement les couches `application` et `adapter/in/web`. Le domaine `Account` et `Transaction` sont restés intouchables.
2. **Walking skeleton first** : câbler l'end-to-end complet (01-01) avant d'activer les scénarios détaillés permet de valider l'infrastructure de test et d'éviter les surprises en cascade.
3. **DTO d'isolation dès le premier step** : créer `TransactionDto` dans 01-01 (plutôt que de réutiliser `Transaction`) a simplifié les steps suivants — le contrat HTTP est stable dès le début.
4. **Configuration Jackson** : `write-dates-as-timestamps=false` est critique pour la sérialisation `Instant` → ISO 8601. À inclure dans le checklist DISTILL pour toute feature utilisant des types temporels Java.

---

## Artefacts permanents

| Artefact | Destination permanente |
|----------|----------------------|
| `discuss/journey-account-statement.yaml` | `docs/ux/phase2-account-statement/journey-account-statement.yaml` |
| `discuss/journey-account-statement-visual.md` | `docs/ux/phase2-account-statement/journey-account-statement-visual.md` |
| ADR-004 (transaction-dto) | `docs/product/architecture/adr-004-transaction-dto.md` (déjà en place) |
| ADR-005 (react-conditional-navigation) | `docs/product/architecture/adr-005-react-conditional-navigation.md` (déjà en place) |
| brief.md §Phase 2 | `docs/product/architecture/brief.md` (déjà en place) |

---

## Prochain incrément candidat

**Phase 3 — Interest & Advanced Features** (voir `SPEC.md` §Phase 3). Sujets à traiter :
- Persistance (remplacement InMemoryAccountRepository) — prérequis pour la piste d'audit et la procédure de réclamation (OQ-1)
- Calcul des intérêts
- Authentification (ADR-003 — déféré Phase 1)
- Tests browser E2E (Playwright) pour les AC React
