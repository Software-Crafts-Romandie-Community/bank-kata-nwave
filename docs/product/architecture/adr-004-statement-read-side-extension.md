# ADR-004 : Extension additive read-side — `StatementUseCase` séparé

**Statut** : Accepté
**Date** : 2026-06-16
**Décideur** : Sylvain Chabert (décision proposée en mode "propose" — DESIGN wave)
**Architecte** : Morgan (solution-architect nWave)

---

## Contexte

Phase 2 (`phase2-transaction-history`) ajoute la consultation du relevé de transactions, le
filtre par plage de dates et le détail d'une transaction. Le DISCUSS a verrouillé une contrainte
forte : **extension additive stricte** — zéro modification de `Account`, `Transaction`,
`AccountUseCase`, `AccountService`, `AccountRepository`, `AccountController` existants
(confirmée Q5 du brief architecture Phase 1, reprise en `## Wave: DISCUSS / [REF] System
Constraints`).

La question de design centrale : **comment exposer un nouveau cas d'usage de lecture
(consultation + filtre du relevé) sans toucher aux ports/adaptateurs Phase 1, et sans dupliquer
l'accès à l'agrégat `Account` ?**

---

## Décision

**Retenu : nouveau port driving `StatementUseCase` (read-only) + nouveau service applicatif
`StatementService`, réutilisant le port driven `AccountRepository` existant en lecture seule.
Nouveau `StatementController` dédié.**

Structure :
- `StatementUseCase` (application/port/in) — port primaire **sans aucune méthode de mutation**.
  Contrat : consulter le relevé complet ou filtré par plage de dates.
- `StatementService` (application) — implémente `StatementUseCase`. Charge `Account` via
  `AccountRepository.load()` (jamais `save()`), filtre les transactions en mémoire, trie par
  `timestamp` décroissant. Aucune dépendance Spring, aucune dépendance vers `AccountService`.
- `StatementController` (adapter/in/web) — nouveau `@RestController` dédié, `GET /api/statement`,
  parse `from`/`to` (ISO 8601 `yyyy-MM-dd`), traduit `InvalidDateRangeException` en 400 RFC 7807.
- Aucun nouveau port driven : `AccountRepository` est réutilisé tel quel.
- Le filtrage par date vit exclusivement dans `StatementService` (application), jamais dans
  `Account`/`Transaction` (domaine) : le domaine ne doit pas connaître la notion de query params
  HTTP `from`/`to`.

---

## Alternatives considérées

### Option B : Étendre `AccountUseCase` avec une méthode `getStatement(from, to)`

**Évaluation** :
- Avantages : un seul port driving à maintenir, pas de nouveau fichier d'interface
- Inconvénients :
  - Viole directement la contrainte DISCUSS "extension additive stricte" (modification du port
    Phase 1)
  - Mélange deux responsabilités dans un seul port : écriture (`deposit`/`withdraw`) et lecture
    pure historique (`getStatement`) — viole la séparation lecture/écriture des ports driving
    (principe Effect Isolation / Contract Shape — un port qui "ne fait que lire" ne doit pas être
    fusionné avec un port qui écrit)
  - Casserait potentiellement `AccountControllerTest` existant si le port mocké change de forme

**Rejeté** : viole une contrainte verrouillée du DISCUSS et le principe de séparation read/write
des ports.

### Option C : Filtrage par date dans le domaine (`Account.getTransactions(from, to)`)

**Évaluation** :
- Avantages : logique de filtrage co-localisée avec la donnée filtrée
- Inconvénients :
  - Modifie `Account` (interdit par le DISCUSS)
  - Couple le domaine à un concept de présentation/transport (`from`/`to` sont des paramètres de
    requête HTTP, pas une règle métier intrinsèque à un compte bancaire)
  - Rendrait `Account` plus difficile à tester en isolation (paramètres de filtrage à mocker même
    pour des tests qui ne portent pas sur le filtre)

**Rejeté** : viole la contrainte additive ET le principe de pureté du domaine (le domaine ne
connaît aucune notion HTTP).

### Option D : Endpoint de filtre séparé `GET /api/statement/filtered`

Déjà rejetée par le DISCUSS (`## Wave: DISCUSS / [REF] Alternatives Considered`, Alternative 2) —
confirmée en DESIGN : un seul endpoint avec query params optionnels évite la duplication de
logique de tri/validation entre deux endpoints.

---

## Conséquences

### Positives

- **Isolation Phase 1 garantie** : aucun fichier Phase 1 modifié — `ArchitectureTest.java`
  existant continue de passer sans changement
- **Séparation read/write explicite** : `StatementUseCase` ne peut structurellement pas muter
  `Account` — un crafter ne peut pas accidentellement ajouter une méthode d'écriture sans que ce
  soit visible comme une violation de la responsabilité du port
- **Réutilisation du port driven sans duplication** : pas de second port pour charger le même
  agrégat — `AccountRepository.load()` suffit, évite deux sources de vérité sur l'accès à
  `Account`
- **Testabilité** : `StatementService` testable unitairement avec un `AccountRepository` mocké,
  sans Spring, comme `AccountService` Phase 1

### Négatives / Compromis

- **Deux ports primaires dans l'application** (`AccountUseCase` + `StatementUseCase`) au lieu
  d'un seul — légère augmentation de la surface du package `application.port.in`. Compromis
  accepté : la cohésion (un port = une responsabilité) prime sur la minimisation du nombre de
  fichiers.
- **`AccountRepository` n'est pas formellement split en port lecture/écriture** : `StatementService`
  a techniquement accès à `save()` même s'il ne l'utilise jamais. Le respect du contrat read-only
  n'est pas garanti par le type system à ce niveau, seulement par discipline + test
  (`verify(never()).save(any())`). Mitigation explicite : si une Phase 3 introduit plusieurs
  agrégats ou un véritable risque d'écriture accidentelle, revisiter avec un port
  `AccountReader` dédié (lecture uniquement) — non justifié aujourd'hui (un seul agrégat,
  mono-utilisateur, risque faible).

### Enforcement

Aucune nouvelle règle ArchUnit requise — les règles existantes (couches `domain` / `application`
/ `adapter`) couvrent structurellement les nouveaux composants sans modification. Recommandation
au crafter : test unitaire `StatementServiceTest` vérifiant explicitement
`verify(accountRepository, never()).save(any())` pour matérialiser le contrat read-only en test
exécutable (pas seulement en convention documentée).

---

## Références

- `docs/feature/phase2-transaction-history/feature-delta.md` — `## Wave: DISCUSS / [REF] System Constraints` (extension additive stricte)
- `docs/product/architecture/brief.md` — `## Application Architecture — Phase 2`, décisions D2/D3/D4
- Principe nWave 12 (Effect Isolation by Design) — séparation des ports driving lecture/écriture
