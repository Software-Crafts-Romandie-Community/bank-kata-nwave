# ADR-001 : Architecture Hexagonale (Ports & Adapters) + OOP

**Statut** : Accepté  
**Date** : 2026-06-02  
**Décideur** : Participant kata (choix confirmé en DESIGN wave)  
**Architecte** : Morgan (solution-architect nWave)

---

## Contexte

Le Bank Kata Phase 1 est un exercice pédagogique dont l'objectif explicite (slice-04) est de
démontrer l'**isolation des règles métier du CLI**. Le critère d'acceptation clé est :
"la règle de rejet est vérifiable indépendamment du CLI (testable en isolation)".

Contraintes identifiées :
- Equipe = 1 développeur en atelier (taille d'équipe minimal)
- Greenfield — aucune dette technique préexistante
- Périmètre = 4 slices, ~2 jours d'effort
- Pas de persistance externe (Phase 1), pas de réseau, pas d'API tierce
- Objectif secondaire : le code produit doit être démontrable et lisible en mob programming

La question de design centrale est : **quelle structure architecturale illustre le mieux
l'isolation des règles métier tout en restant accessible à un participant atelier ?**

---

## Décision

**Retenu : Architecture Hexagonale (Ports & Adapters) avec paradigme OOP.**

Structure :
- **Ports primaires (driving)** : `AccountUseCase` — interface définissant les cas d'usage entrants
- **Ports secondaires (driven)** : `AccountRepository` — interface pour l'accès au compte
- **Domaine pur** : `Account` + `Transaction` (Record) + `InsufficientFundsException` — aucune dépendance vers les couches externes
- **Adaptateurs** : `CLIAdapter` (driving) et `InMemoryAccountRepository` (driven) — couche externe substituable

Règle de dépendance : toutes les flèches d'import pointent vers l'intérieur (domaine).
Le domaine n'importe aucune classe d'infrastructure ou d'adaptateur.

---

## Alternatives considérées

### Option B : Architecture fonctionnelle (FP)

**Description** : Fonctions pures, immutabilité stricte, types algébriques (`Either<Error, Success>`),
composition de fonctions comme mécanisme principal d'organisation.

**Évaluation** :
- Avantages : testabilité maximale (fonctions déterministes), cohérence avec certains paradigmes kata
- Inconvénients pour ce contexte :
  - Java 21 n'est pas un langage FP natif — émulation FP en Java produit une cérémonie syntaxique
    élevée (streams, Optional chaînés, lambdas imbriqués) qui nuit à la lisibilité pédagogique
  - Le kata est explicitement conçu pour pratiquer les patterns OOP (selon le contexte DISCUSS)
  - `Either` en Java nécessite une bibliothèque tierce (Vavr) ou un implémentation manuelle — complexité injustifiée

**Rejeté** : le bénéfice FP ne compense pas le bruit syntaxique Java pour un public kata OOP.

---

### Option C : Architecture en couches (Layered / N-Tier)

**Description** : Couches horizontales classiques — Présentation → Application → Domaine → Infrastructure.
Dépendances strictement descendantes.

**Évaluation** :
- Avantages : familière, faible courbe d'apprentissage, documentation abondante
- Inconvénients pour ce contexte :
  - L'isolation du domaine n'est pas structurellement garantie — un développeur peut importer
    directement une couche infrastructure depuis le domaine sans violation syntaxique
  - L'objectif pédagogique de slice-04 ("règle testable indépendamment du CLI") est moins évident
    à démontrer : les couches ne définissent pas de **contrat d'interface** explicite (port)
  - Le remplacement du CLI par un autre adaptateur (Phase 2 potentielle : API REST) nécessite une
    réorganisation plus invasive qu'avec Hexagonal

**Rejeté** : insuffisant pour démontrer l'isolation domaine de façon structurellement explicite.

---

## Conséquences

### Positives

- **Testabilité maximale** : le domaine (`Account`) est testable sans aucun adaptateur — les tests
  unitaires n'ont pas besoin de mock pour le cœur métier
- **Isolation pédagogique visible** : la règle `InsufficientFundsException` est physiquement dans
  le package `domain`, sa localisation est auto-documentante
- **Extensibilité Phase 2** : `StatementService` s'ajoute sans modifier `Account` ni `CLIAdapter`
- **Substituabilité** : `InMemoryAccountRepository` peut être remplacé par un adapter JDBC ou
  fichier sans modifier `AccountService`

### Négatives / Compromis

- **Cérémonie supplémentaire** : Hexagonal introduit 2 interfaces (`AccountUseCase`,
  `AccountRepository`) qui ne seraient pas nécessaires dans une architecture monolithique simple.
  Pour un kata de 4 slices, cette cérémonie est acceptable car elle est l'objectif pédagogique.
- **Indirection supplémentaire** : un participant débutant doit naviguer entre 3 couches pour
  comprendre le flux complet. Mitigation : la structure de packages et les C4 dans le brief.md
  documentent l'architecture explicitement.

### Enforcement

Règles ArchUnit (Apache 2.0) à activer en CI :
- Les classes du package `domain` n'ont aucune dépendance vers `application` ou `adapter`
- Les classes du package `application` n'ont aucune dépendance vers `adapter`
- Seul `Main` instancie les classes des packages `adapter`

---

## Références

- Evans, E. — *Domain-Driven Design* (2003) — concept d'isolation du domaine
- Cockburn, A. — *Hexagonal Architecture* (2005) — pattern Ports & Adapters
- slice-04 AC : "La règle de rejet est vérifiable indépendamment du CLI"

---

## Update 2026-06-02 — Pivot web app

### Décision confirmée et renforcée

La décision ADR-001 (Architecture Hexagonale + OOP) est **confirmée** par le pivot vers Spring Boot.
Elle n'est pas remise en question — elle est renforcée : le risque de pollution Spring dans le
domaine est structurellement plus élevé avec un framework IoC qu'avec Java pur.

### Changement de driving port

Le driving port primaire passe de `CLIAdapter` à `AccountController` (@RestController Spring Boot).
Le contrat `AccountUseCase` (interface Java) reste **inchangé** — c'est précisément l'objectif
de l'architecture hexagonale : remplacer un adaptateur sans modifier le domaine ni l'application.

### Risque de contamination Spring identifié

Avec Spring Boot, le risque principal est l'introduction d'annotations Spring (`@Autowired`,
`@Component`, `@Value`) dans les packages `domain/` ou `application/`. Ce pattern violerait la
règle de dépendance hexagonale et couplerait irréversiblement le domaine au framework.

**Guardrail ArchUnit étendu** :

En plus des règles d'origine, ajouter obligatoirement en CI :
- `domain` ne doit pas importer `org.springframework.*`
- `application` (hors `BankApplication`) ne doit pas importer `org.springframework.*`
- Seul `BankApplication` (composition root) est annoté `@SpringBootApplication`
- Les classes annotées `@RestController` appartiennent uniquement à `adapter.in.web`

### Impact sur les conséquences originales

| Conséquence (ADR-001 initial) | Statut après pivot |
|---|---|
| Testabilité maximale — domaine sans adaptateur | Maintenue : domaine sans Spring = testable avec JUnit 5 seul |
| Isolation pédagogique visible | Renforcée : la règle ArchUnit rend la violation immédiatement détectable en CI |
| Extensibilité Phase 2 | Inchangée : `StatementService` s'ajoute sans modifier `Account` ni `AccountController` |
| Substituabilité adaptateurs | Confirmée : le pivot CLI → HTTP a remplacé `CLIAdapter` sans modifier `AccountService` |
| Cérémonie supplémentaire (interfaces) | Acceptable : Spring Boot valide structurellement l'intérêt des interfaces (injection IoC) |
