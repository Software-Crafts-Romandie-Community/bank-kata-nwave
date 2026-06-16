# Évolution : phase1-account-management

**Finalisée le** : 2026-06-16
**Statut** : Complète — 8/8 steps DONE
**Workspace source** : `docs/feature/phase1-account-management/` (conservé, non supprimé — voir note en fin de document)

---

## Résumé

Application bancaire web complète : une cliente (Marie) consulte son solde, dépose et retire de
l'argent depuis un navigateur. Backend Spring Boot 3.x (architecture hexagonale, domaine Java pur)
exposant une REST API, frontend React 18 + TypeScript + Vite intégré au build Maven.

## Contexte métier

- **Job statement** : *"When I need to manage my bank account, I want to perform deposits and
  withdrawals and view my balance from a browser, so I can stay in control of my finances without
  visiting a branch."* (`job-001`, voir `docs/product/jobs.yaml`)
- **Persona** : Marie, cliente bancaire grand public, 35 ans.
- **North Star** : Marie effectue un dépôt et un retrait complets dans le navigateur, avec
  confirmation visuelle à chaque étape, en moins de 3 minutes.
- **Pivot d'origine** : le projet a démarré comme kata CLI pédagogique (Software Crafts Romandie)
  puis a été requalifié en application web bancaire standard le 2026-06-02 (fusion de
  `phase1-account-management` CLI + `phase2-web-ui` démo en une seule Phase 1).

## Décisions clés

| # | Décision | ADR / Source |
|---|----------|---------------|
| D1 | Architecture hexagonale Ports & Adapters | ADR-001 |
| D2 | Java 21 LTS | ADR-002 |
| D3 | Spring Boot 3.x comme framework web | ADR-003 |
| D4 | `CLIAdapter` supprimé — remplacé par `AccountController` (@RestController) | feature-delta.md §DESIGN |
| D5 | Persistance en mémoire Phase 1 (`InMemoryAccountRepository` @Component singleton) | ADR-001 |
| D6 | `BigDecimal` pour tous les montants, sérialisé en Number JSON | ADR-002 |
| D7 | Pas d'authentification en Phase 1 — hors scope explicite | ADR-003 |
| D8 | Erreurs HTTP au format RFC 7807 Problem Details (400 montant invalide, 409 fonds insuffisants) | feature-delta.md §DELIVER |

**Issue notable (UI-01, 2026-06-03)** : la décision initiale "HTML vanilla + JS, pas de framework"
a été supersédée en cours de DELIVER par "React 18 + TypeScript + Vite" — l'app CDN/Babel
standalone a été jugée insuffisante (pas de tests, pas de build réel). Back-propagée dans
`feature-delta.md`, `docs/product/architecture/brief.md` et `roadmap.json` (step 05-01).

## Travail réalisé

8 steps répartis en 5 phases, tous `COMMIT: EXECUTED / PASS` (voir `deliver/execution-log.json`) :

| Phase | Steps | Contenu | Période |
|---|---|---|---|
| 01 — Walking Skeleton | 01-01 | `GET /api/balance` end-to-end (MockMvc → AccountController → AccountService → InMemoryAccountRepository) | 2026-06-02 |
| 02 — Deposit | 02-01, 02-02 | Dépôt valide + rejet montant invalide (400 RFC 7807) | 2026-06-03 |
| 03 — Withdrawal | 03-01, 03-02 | Retrait valide + rejet montant invalide (400 RFC 7807) | 2026-06-03 |
| 04 — Insufficient Funds | 04-01, 04-02 | Rejet 409 RFC 7807 + propriété "solde jamais négatif" | 2026-06-03 |
| 05 — React Frontend | 05-01 | Frontend Vite + React 18 + TypeScript, intégré via `frontend-maven-plugin` | 2026-06-03 |

16 scénarios BDD actifs (Cucumber-JVM), répartis sur 4 fichiers `.feature`
(`walking-skeleton`, `deposit`, `withdrawal`, `insufficient-funds`) — 50 % de scénarios
d'erreur/limite (ratio cible 40 %+, PASS).

## Leçons apprises / Issues rencontrées

- **ADR-025 (canon nWave)** appliqué systématiquement : l'authoring de tests unitaires a été
  sauté quand l'AT atteignait GREEN directement (cas fréquent ici — domaine très simple).
- **Pivot mi-parcours (UI-01)** : un changement de décision en plein DELIVER (vanilla JS → React)
  a nécessité une back-propagation manuelle vers 3 documents — un rappel que le feature-delta.md
  doit rester la source de vérité même après le démarrage du DELIVER.
- **Pas de scaffolding nécessaire** pour la plupart des steps 02 à 04 — les fichiers `.feature` et
  step definitions avaient été pré-rédigés par la DISTILL wave, accélérant le cycle RED→GREEN.

## Artefacts permanents (déjà en place — aucune migration nécessaire)

Contrairement au modèle générique `/nw-finalize` (qui suppose des sous-dossiers
`discuss/`, `design/`, `distill/` séparés dans le workspace), ce projet centralise toutes les
décisions de vague dans un unique `feature-delta.md`. Les artefacts à valeur durable étaient déjà
écrits directement dans des emplacements permanents au fil des vagues :

- ADRs : `docs/product/architecture/adr-001-hexagonal-oop.md`, `adr-002-java21.md`, `adr-003-spring-boot.md`
- Architecture brief : `docs/product/architecture/brief.md`
- Journeys : `docs/product/journeys/account-management.yaml`, `web-ui.yaml`
- Politique d'infrastructure ATDD : `docs/architecture/atdd-infrastructure-policy.md`
- Spécifications BDD vivantes : `src/test/resources/features/account-management/*.feature`

→ Phase B (migration) du processus `/nw-finalize` n'a rien eu à déplacer.

## Workspace conservé

`docs/feature/phase1-account-management/` n'est **pas supprimé** : la matrice de vagues nWave
dérive le statut des features de ce répertoire. Il reste consultable comme historique détaillé
(feature-delta.md, slices/, roadmap.json, execution-log.json). Seul le marqueur de session
`deliver/.develop-progress.json` (état de reprise temporaire, sans valeur historique) a été
supprimé lors du nettoyage.

## Hors scope Phase 1 (candidats Phase 2 / Phase 3 — voir `SPEC.md`)

- Phase 2 — Transaction History : relevé de compte, filtre par date, détail des transactions
- Phase 3 — Interest & Advanced Features : intérêts, types de comptes multiples, frais
- Authentification / sessions, HTTPS/TLS, persistance base de données, multi-compte, déploiement cloud
