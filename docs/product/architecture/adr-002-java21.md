# ADR-002 : Java 21 LTS comme langage cible

**Statut** : Accepté  
**Date** : 2026-06-02  
**Décideur** : Participant kata (choix confirmé en DESIGN wave)  
**Architecte** : Morgan (solution-architect nWave)

---

## Contexte

La contrainte [REQ-001] du DISCUSS wave indique que le kata est polyglotte — le participant choisit
son langage. En DESIGN wave, le participant a sélectionné **Java** comme langage cible.

La question devient : **quelle version de Java retenir pour un kata en 2026 ?**

Contraintes :
- Kata pédagogique — la version doit être accessible sur les postes d'atelier standards
- Pas de framework applicatif — Java pur
- Objectif : utiliser les fonctionnalités du langage qui illustrent les patterns de design OOP
  (value objects, exceptions métier, interfaces comme ports)
- La version doit être stable, bien supportée par les IDE (IntelliJ, Eclipse, VS Code + plugins Java)

---

## Décision

**Retenu : Java 21 LTS (OpenJDK).**

Licence : GPL v2 + Classpath Exception (OpenJDK) — conforme à la politique OSS first.  
Support LTS : jusqu'à septembre 2031 (Oracle) / sources communautaires.  
Distribution recommandée : Temurin 21 (Eclipse Adoptium, Apache 2.0) — installable via `sdk install java 21-tem`.

Fonctionnalités Java 21 exploitées dans ce kata :
- **Records** (stable depuis Java 16) : `Transaction` est un Record — value object immutable sans boilerplate
- **Sealed classes** (stable depuis Java 17, optionnel) : peut servir à typer les résultats d'opération si le crafter le souhaite
- **Text blocks** (stable depuis Java 15) : messages CLI lisibles
- **Pattern matching `instanceof`** (stable depuis Java 16) : optionnel, utile en CLIAdapter

---

## Alternatives considérées

### Java 17 LTS

**Évaluation** :
- Avantages : LTS, très répandu, Records disponibles
- Inconvénients : version supérieure (21) disponible avec le même coût d'adoption ; certains
  participants seront en Java 21 par défaut via Temurin/SDKMAN en 2026

**Rejeté** : Java 21 est préféré car c'est le LTS courant en 2026 — même stabilité, meilleure longévité.

---

### Java 11 LTS

**Évaluation** :
- Avantages : présent sur d'anciens postes d'atelier
- Inconvénients : Records absents (Java 16+) — `Transaction` doit être une classe avec equals/hashCode
  manuels, ce qui alourdit le code pédagogique sans bénéfice ; fin de support étendu proche

**Rejeté** : l'absence de Records dégrade la lisibilité pédagogique du kata.

---

### Kotlin (JVM)

**Évaluation** :
- Avantages : data classes natives, null-safety, syntaxe concise, interop Java
- Inconvénients : le participant a explicitement choisi Java ; Kotlin introduit une courbe
  d'apprentissage supplémentaire et des outils de build additionnels — hors scope de ce kata

**Rejeté** : hors du périmètre décisionnel (Java sélectionné par le participant).

---

## Conséquences

### Positives

- **Records natifs** : `Transaction` est un Record — immutabilité, `equals`/`hashCode`/`toString`
  gratuits, lisibilité maximale du value object
- **LTS stable** : aucun risque de breaking change avant 2031
- **Écosystème** : JUnit 5, Mockito 5, Cucumber JVM, ArchUnit — tous compatibles Java 21
- **Disponibilité IDE** : IntelliJ IDEA 2024+, Eclipse 2024+, VS Code Java Extension Pack — support Java 21 natif
- **Installation simplifiée** : `sdk install java 21-tem` via SDKMAN, disponible sur Linux/macOS/WSL2

### Négatives / Compromis

- **Version minimale** : les participants avec Java 11 doivent mettre à jour. Mitigation :
  SDKMAN ou Temurin permettent l'installation parallèle sans droits admin sur la plupart des postes d'atelier.
- **`--enable-preview`** non requis : les fonctionnalités utilisées sont toutes stables en Java 21
  (pas de features preview nécessaires pour ce kata).

---

## Références

- Temurin 21 : https://adoptium.net/temurin/releases/?version=21 (Apache 2.0)
- OpenJDK 21 release notes : https://openjdk.org/projects/jdk/21/
- Java Records JEP 395 (stable Java 16) : https://openjdk.org/jeps/395
