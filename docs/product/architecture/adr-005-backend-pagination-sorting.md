# ADR-005 : Pagination et tri backend pour `GET /api/statement`

**Statut** : Accepté
**Date** : 2026-06-16
**Décideur** : Sylvain Chabert (exigence produit explicite, verrouillée — pas de re-discussion d'alternative sur le besoin lui-même)
**Architecte** : Morgan (solution-architect nWave)
**Supersède** : D8 (`docs/product/architecture/brief.md`, "Pagination — Confirmé hors périmètre", itération DESIGN 1)

---

## Contexte

La première itération DESIGN (2026-06-16) a verrouillé D8 : aucune pagination, justifiée par
un volume de transactions faible (dizaines à centaines, usage mono-utilisateur de démonstration).
Cette décision n'était pas une erreur — c'était la bonne réponse au périmètre connu à ce moment.

Après peer review et approbation de cette première itération, le product owner a exprimé une
nouvelle exigence explicite, non négociable sur le fond : **affichage paginé (avec tri) des
transactions, avec pagination provenant du backend**, même si le stockage reste in-memory. Le
contexte produit a changé — la pagination backend devient une exigence fonctionnelle assumée,
indépendamment du volume réel de données en Phase 2.

Décisions déjà tranchées en amont par le product owner (non re-débattues ici) :
- Champs triables : `timestamp` (date) et `amount` (montant). Tri par défaut : date décroissante (cohérent avec le comportement slice-05/06).
- Style de réponse : DTO custom léger, pas `Pageable`/`Page` de Spring Data.
- Taille de page : défaut 20, choix utilisateur parmi {10, 20, 50}.

La question de design qui reste ouverte : **comment exposer la pagination et le tri sans
introduire de dépendance non justifiée, sans toucher au domaine, et en respectant l'ordre
filtre → tri → pagination déjà implicite dans le pipeline `StatementService` ?**

---

## Décision

**Retenu : pagination et tri backend via un DTO custom léger `PageResponse<T>`, calculés en
mémoire dans `StatementService`, appliqués après le filtrage par date (slice-06).**

Contrat `GET /api/statement` (extension non-breaking des query params existants `from`/`to`) :

| Paramètre | Défaut | Valeurs acceptées | Validation |
|-----------|--------|---------------------|-------------|
| `from`, `to` | absent (pas de filtre) | ISO 8601 `yyyy-MM-dd` | Inchangé depuis slice-06 |
| `page` | `0` | Entier >= 0 | `page < 0` → 400 Bad Request (RFC 7807) |
| `size` | `20` | `10`, `20`, `50` | Valeur hors liste → 400 Bad Request (RFC 7807) — voir rationale ci-dessous |
| `sortBy` | `date` | `date`, `amount` | Valeur hors liste → 400 Bad Request (RFC 7807) |
| `sortDir` | `desc` | `asc`, `desc` | Valeur hors liste → 400 Bad Request (RFC 7807) |

**Rationale validation stricte de `size`** : le product owner a explicitement fixé la liste fermée
`{10, 20, 50}` (pas "une valeur raisonnable avec plafond"). Une validation stricte (whitelist)
plutôt qu'un plafond ouvert évite la dérive silencieuse (un client qui passe `size=37` aurait un
comportement ambigu avec un plafond ouvert — silencieusement tronqué à quelle valeur ? alors qu'un
400 explicite est sans ambiguïté et cohérent avec le principe déjà établi pour `from > to`
(D5/ADR-004) : un input invalide produit une erreur explicite, jamais un comportement dégradé
silencieux. Le frontend ne propose que 3 valeurs (D du brief) ; un client externe qui envoie une
valeur hors liste obtient un contrat clair plutôt qu'un comportement implicite.

Réponse : nouveau DTO `PageResponse<TransactionResponse>` :

```
PageResponse<T> {
  content: T[]            // page courante des transactions (TransactionResponse[])
  page: int                // index de page courant (0-based)
  size: int                // taille de page effective (10, 20 ou 50)
  totalElements: long      // nombre total de transactions APRÈS filtrage par date, AVANT pagination
  totalPages: int          // ceil(totalElements / size), minimum 0
}
```

Pipeline `StatementService` (ordre des opérations, non négociable pour la cohérence du résultat) :

1. **Filtre** — restreindre par `[from, to]` (slice-06, inchangé)
2. **Tri** — trier la liste filtrée par `sortBy` (`timestamp` ou `amount`) selon `sortDir`
3. **Pagination** — découper la liste triée en page de taille `size`, extraire la page `page`

Le tri remplace l'ordre fixe "date décroissante" de slice-05 par un ordre paramétrable dont la
valeur par défaut (`date`, `desc`) reproduit exactement le comportement slice-05 — aucune
régression de comportement par défaut.

**Page hors limites** (ex. `page=99` alors qu'il n'existe que 2 pages) : **200 OK avec
`content: []`** et métadonnées cohérentes (`totalElements`, `totalPages` reflètent l'état réel,
`page` reflète la valeur demandée). Pas de 400. Rationale : cohérent avec le principe déjà
établi en slice-05/06 — "jamais d'erreur pour une absence de résultat" (filtre sans transaction
→ 200 OK liste vide). Une page hors limites est une absence de résultat, pas une requête
malformée ; elle ne nécessite aucune information que le client ne pourrait déjà déduire de
`totalPages`. Un 400 ici introduirait une asymétrie injustifiée avec le traitement de `from`/`to`
sans résultat.

`StatementUseCase` reste un port strictement read-only (Mandate 12, ADR-004 inchangé) : la
pagination/tri n'ajoute aucune méthode de mutation. Contract shape : pure read / bounded-change,
identique à l'analyse ADR-004 — `StatementService` ne mute jamais `Account`, n'invoque jamais
`save()`. Le tri et la pagination opèrent sur une copie en mémoire de la liste filtrée, jamais sur
`Account.getTransactions()` directement (qui reste non modifiable).

---

## Alternatives considérées

### Option A retenue : DTO custom léger `PageResponse<T>`

Voir Décision ci-dessus. Avantages : zéro nouvelle dépendance, contrôle total du contrat JSON,
cohérent avec la philosophie "stack minimale" déjà appliquée (D7/D8 itération 1).

### Option B : Pagination backend via Spring Data `Pageable`/`Page`

**Évaluation** :
- Avantages : `Pageable` standard Spring, binding automatique des query params (`page`, `size`,
  `sort`) via `@PageableDefault`, `Page<T>` inclut déjà les métadonnées équivalentes
- Inconvénients :
  - Nécessite la dépendance `spring-data-commons` (via `spring-boot-starter-data-jpa` ou
    `spring-data-commons` seul), absente du projet — `pom.xml`/`build.gradle` Phase 1 ne déclare
    aucun module Spring Data (pas de base de données, stockage 100% in-memory custom)
  - `Pageable`/`Page` sont conçus pour s'intégrer avec des `Repository` Spring Data (JPA, MongoDB,
    etc.) — les utiliser ici reviendrait à importer l'écosystème Spring Data pour sa seule classe
    de pagination, sans bénéficier d'aucune intégration réelle avec `InMemoryAccountRepository`
    (qui n'est pas un `Repository` Spring Data)
  - Format JSON par défaut de `Page<T>` (Jackson) est verbeux (`pageable`, `sort`, `first`, `last`,
    `empty`, etc.) — surface de contrat plus large que nécessaire, davantage de champs à maintenir
    stables pour le frontend

**Rejeté** : dépendance non justifiée pour ce périmètre (principe Open Source First n'est pas en
cause — Spring Data est OSS — mais "Simplest solution first" et l'absence de besoin réel
d'intégration Spring Data Repository le sont). Le product owner a explicitement exclu cette
option dans le brief de la demande.

### Option C : Pagination côté frontend uniquement, sur la liste complète déjà chargée

**Évaluation** :
- Avantages : zéro changement de contrat API, implémentation entièrement frontend, réutilise
  `TransactionResponse[]` déjà reçu
- Inconvénients :
  - **Contredit directement l'exigence produit** : "pagination provenant du backend" est explicite
    et non négociable — ce n'est pas un raffinement technique mais une exigence fonctionnelle
  - Ne réduit pas la charge réseau si le volume de transactions grandit (Phase 3 introduira
    potentiellement la persistance — Q6 brief itération 1) : toute la liste continuerait de
    transiter sur chaque requête, rendant la pagination cosmétique plutôt que structurelle
  - Le tri resterait possible côté frontend, mais la pagination frontend seule ne répond pas à
    l'intention du product owner (préparer le terrain pour un futur volume de données plus grand,
    même si le stockage actuel ne l'exige pas encore)

**Rejeté** : viole une exigence produit explicite et n'apporte aucun bénéfice architectural futur.

---

## Conséquences

### Positives

- **Aucune nouvelle dépendance OSS** : DTO custom `PageResponse<T>` est un record Java, zéro
  bibliothèque ajoutée — cohérent avec D7/D8 itération 1 et le principe Open Source First (rien à
  documenter en termes de licence, le code appartient au projet)
- **Contrat API explicite et stable** : 5 champs documentés (`content`, `page`, `size`,
  `totalElements`, `totalPages`), pas de champs Spring Data superflus à maintenir
- **Cohérence avec le principe "jamais d'erreur pour absence de résultat"** déjà établi en
  slice-05/06 — la page hors limites suit la même philosophie que le filtre sans résultat
- **Aucun impact domaine** : `Account`, `Transaction`, `AccountUseCase`, `AccountService`,
  `AccountRepository`, `AccountController` restent strictement intouchés — la contrainte
  additive DISCUSS/ADR-004 est préservée à l'identique
- **Testabilité inchangée** : tri et pagination sont des fonctions pures sur `List<Transaction>`
  en mémoire dans `StatementService` — testables unitairement sans Spring, sans mock
  supplémentaire au-delà de ceux déjà nécessaires pour le filtrage

### Négatives / Compromis

- **`StatementController` et `StatementService` passent d'une extension mineure (slice-06) à un
  changement de contrat plus substantiel** (4 nouveaux query params + nouvelle forme de réponse).
  Compromis accepté : le DTO `StatementResponse` (liste plate) est remplacé par
  `PageResponse<TransactionResponse>` — **breaking change de contrat API** pour les consommateurs
  de `GET /api/statement` antérieurs à cet amendement (aucun consommateur en production réelle à
  ce stade du projet — kata pédagogique, acceptable)
- **D8 explicitement supersédée** : ce n'est pas une correction d'erreur, c'est un changement de
  périmètre piloté par le produit. Documenté dans `brief.md` (table des décisions, D8 marqué
  SUPERSEDED → renvoi vers D10/ADR-005) pour qu'aucun lecteur futur n'interprète D8 comme un oubli
  invalidé techniquement
- **Tri en mémoire sur la liste complète avant pagination** : pour ce volume (dizaines à centaines
  de transactions, mono-utilisateur), aucun problème de performance. Si Phase 3 introduit la
  persistance et un volume significativement plus grand, ce point devra être revisité (tri/filtre
  délégués à la couche de stockage plutôt qu'en mémoire applicative) — noté en Question ouverte

### Enforcement

Aucune nouvelle règle ArchUnit structurelle requise — `PageResponse` et les nouveaux paramètres
de requête restent dans les packages déjà couverts (`adapter.in.web`, `application`).
Recommandation au crafter : test unitaire `StatementServiceTest` couvrant explicitement l'ordre
filtre → tri → pagination (ex. un cas où le filtre réduit le total avant que la pagination ne soit
appliquée, pour détecter une éventuelle inversion accidentelle de l'ordre des opérations).

---

## Lien vers la décision supersédée

**D8** (`docs/product/architecture/brief.md`, table "Décisions de conception", itération DESIGN 1,
2026-06-16) : *"Aucune pagination — Mono-utilisateur, in-memory, volumes faibles... contrainte
documentée explicitement, pas un oubli."*

D8 reste documentée intacte dans le brief (immutabilité des ADR/décisions — on ne réécrit pas
l'historique). Elle est marquée **SUPERSEDED by D10 / ADR-005** dans la table des décisions du
brief, avec la précision explicite que D8 était la bonne décision pour le périmètre connu au
moment où elle a été prise ; le changement de contexte (exigence produit explicite post-review)
justifie la supersession, pas une invalidation rétroactive.

---

## Références

- `docs/product/architecture/brief.md` — section `### Pagination et tri — Amendement Phase 2`, D8 marqué SUPERSEDED, D10+ ajoutées
- `docs/product/architecture/adr-004-statement-read-side-extension.md` — port `StatementUseCase` inchangé, contract shape read-only confirmé
- `docs/feature/phase2-transaction-history/feature-delta.md` — `## Wave: DESIGN / [REF] Amendement — Pagination et tri backend`
- `docs/feature/phase2-transaction-history/design/upstream-changes.md` — impact sur slice-05/slice-06 (AC amendés)
