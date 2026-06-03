# ADR-003 : Spring Boot 3.x comme framework web

**Statut** : Accepté
**Date** : 2026-06-02
**Décideur** : Sylvain Chabert (décision utilisateur — pivot web app)
**Architecte** : Morgan (solution-architect nWave)

---

## Contexte

Le projet pivote d'un kata CLI (Java pur, sans framework) vers une application web bancaire
standard. Le nouveau JTBD est "Marie gère son compte bancaire depuis le navigateur" — ce qui
requiert un serveur HTTP de production, une REST API, et la capacité à servir des ressources
statiques (HTML + JS).

Java 21 pur ne fournit pas de serveur HTTP adapté à un usage de production. La question est :
**quel framework web Java choisir pour exposer la REST API et servir le frontend ?**

Contraintes :
- Langage : Java 21 (décision ADR-002, inchangée)
- Paradigme : OOP (décision ADR-001, inchangée)
- Architecture : Hexagonale Ports & Adapters (ADR-001) — le framework ne doit pas envahir le domaine
- Équipe : petite (1-2 développeurs), objectif time-to-market élevé
- Tests : isolation HTTP critique — tester l'API REST sans démarrer un serveur complet

---

## Décision

**Retenu : Spring Boot 3.x (Apache 2.0).**

Spring Boot 3.x est la version courante basée sur Spring Framework 6.x, compatible Java 21 LTS,
et produit des binaires Jakarta EE 10. Il est le standard de facto de l'industrie Java pour les
applications web en 2026.

---

## Alternatives considérées

### Option B : Javalin

**Description** : Framework web léger, proche du "no framework", API fluide, serveur Jetty ou
Netty embarqué.

**Évaluation** :
- Avantages : très léger, courbe d'apprentissage minimale, proche de Java pur, excellente
  lisibilité du code de routage
- Inconvénients pour ce contexte :
  - Absence de MockMvc — les tests d'intégration REST nécessitent un serveur démarré (plus lent,
    plus fragile en CI)
  - Injection de dépendances manuelle ou via un conteneur IoC tiers — composition root plus
    verbeux que Spring
  - Écosystème de documentation et d'exemples nettement plus réduit que Spring Boot
  - Le crafter devra reconstruire des utilitaires (sérialisation JSON, validation, gestion
    d'erreurs) que Spring Boot fournit out-of-the-box

**Rejeté** : l'absence de MockMvc est un obstacle critique à l'attribut de qualité "testabilité"
(Critique selon brief.md). L'outillage de test de Spring Boot est un avantage décisif.

---

### Option C : Quarkus

**Description** : Framework cloud-native JVM, compilable en binaire natif (GraalVM), CDI pour
l'injection de dépendances, REST via JAX-RS (RESTEasy).

**Évaluation** :
- Avantages : démarrage ultra-rapide, empreinte mémoire réduite, compilation native possible
- Inconvénients pour ce contexte :
  - CDI (Contexts and Dependency Injection) != Spring DI — courbe d'apprentissage additionnelle
    pour une équipe Spring-familière
  - JAX-RS (annotations `@GET`, `@Path`) != Spring MVC (`@GetMapping`) — documentation et
    exemples moins abondants pour un public Java standard
  - La compilation native GraalVM est hors scope Phase 1 — le bénéfice principal de Quarkus ne
    s'applique pas ici
  - Outillage de test d'intégration REST (`@QuarkusTest` + RestAssured) équivalent mais moins
    familier que MockMvc

**Rejeté** : la courbe d'apprentissage CDI et l'absence de bénéfice cloud-native en Phase 1 ne
justifient pas l'écart par rapport au standard Spring Boot.

---

### Option D : com.sun.net.httpserver (Java pur)

**Description** : Serveur HTTP inclus dans le JDK standard depuis Java 6. Aucune dépendance
externe.

**Évaluation** :
- Avantages : zéro dépendance, parfaitement conforme à l'objectif initial "Java pur"
- Inconvénients pour ce contexte :
  - Non conçu pour un usage de production — pas de gestion des threads avancée, pas de support
    HTTPS natif, pas de sérialisation JSON intégrée
  - Routage HTTP entièrement manuel (conditions `if/else` sur `request.getPath()`)
  - Aucun outil de test d'intégration — tester l'API requiert de démarrer un vrai serveur sur un
    port réseau en CI
  - Le pivot vers une "application web bancaire standard" rend cet outillage inadapté à l'objectif

**Rejeté** : niveau de production insuffisant pour une application web bancaire ; le projet a
explicitement évolué au-delà de ce périmètre.

---

## Conséquences

### Positives

- **MockMvc** : tests d'intégration REST rapides sans démarrer Tomcat — `@WebMvcTest` isole
  uniquement la couche web, le domaine est mocké via Mockito
- **Injection de dépendances** : `@Component` sur `InMemoryAccountRepository` et `@RestController`
  sur `AccountController` — la composition root devient le contexte Spring (BankApplication)
- **Tomcat embarqué** : `java -jar` suffit pour démarrer — pas de déploiement WAR nécessaire
- **Static resources** : `src/main/resources/static/` servi automatiquement sur `/` — même
  serveur que l'API REST, CORS non requis en Phase 1
- **Écosystème** : documentation abondante, exemples disponibles, support IDE natif

### Négatives / Compromis

- **Risque de contamination du domaine** : Spring Boot facilite (syntaxiquement) l'ajout
  d'annotations Spring dans les classes domaine. Guardrail obligatoire via ArchUnit (voir
  ADR-001 Update 2026-06-02) : le domaine ne doit **jamais** importer `org.springframework.*`
- **Dépendance framework** : le projet prend une dépendance sur le cycle de vie Spring Boot.
  Mitigation : l'architecture hexagonale garantit que cette dépendance est confinée aux adaptateurs
  et à la composition root — le domaine reste portable
- **Startup time** : démarrage Spring plus lent que Javalin ou Java pur. Non critique pour
  une application web bancaire Phase 1

### Enforcement obligatoire

Règles ArchUnit à activer en CI pour protéger l'invariant hexagonal face à Spring :
- Le package `domain` n'importe aucune classe `org.springframework.*`
- Le package `application` (hors `BankApplication`) n'importe aucune classe `org.springframework.*`
- Les classes annotées `@RestController` appartiennent uniquement au package `adapter.in.web`
- Seul `BankApplication` est annoté `@SpringBootApplication`

### Note sécurité (Phase 1)

Pas d'authentification ni de gestion de session en Phase 1. Cette décision est explicite et
documentée : le compte unique en mémoire n'est pas exposé sur le réseau public. Phase 3
adressera l'authentification (Spring Security ou équivalent).

---

## Références

- Spring Boot 3.x : https://spring.io/projects/spring-boot (Apache 2.0)
- Spring Boot Starter Test / MockMvc : https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html
- Jakarta EE 10 compatibility : https://spring.io/blog/2022/11/16/spring-framework-6-0-goes-ga
- Javalin : https://javalin.io (Apache 2.0) — alternative rejetée
- Quarkus : https://quarkus.io (Apache 2.0) — alternative rejetée
