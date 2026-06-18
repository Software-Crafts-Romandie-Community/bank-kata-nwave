# ADR-004 — Isolation domaine/HTTP via TransactionDto

**Date** : 2026-06-18
**Statut** : Accepté
**Décideur** : Morgan (nw-solution-architect)
**Contexte feature** : phase2-account-statement

---

## Contexte

Phase 2 expose un nouveau endpoint `GET /api/statement` qui retourne la liste des transactions
enregistrées dans la session. Le domaine possède déjà un objet `Transaction` (record Java) avec
trois champs : `type` (enum `Transaction.Type`), `amount` (BigDecimal), `timestamp` (Instant).

La question est : peut-on sérialiser `Transaction` directement en réponse HTTP, ou faut-il
créer un DTO dédié ?

Contraintes à respecter :
- La règle hexagonale interdit aux objets domaine d'importer des annotations d'infrastructure
  (`@JsonProperty`, `@JsonSerialize`, etc.).
- Le contrat HTTP doit rester stable indépendamment de l'évolution future du domaine.
- Le type `Instant` de `Transaction.timestamp` n'est pas naturellement sérialisé au format
  désiré (`"2026-06-18T14:32:00Z"` en String) sans configuration Jackson explicite.
- L'enum `Transaction.Type` (valeurs `DEPOSIT`, `WITHDRAWAL`) est une valeur technique domaine —
  le contrat HTTP expose des Strings (`"DEPOSIT"`, `"WITHDRAWAL"`) dont la forme peut évoluer
  (ex. traduction, ajout de valeurs) sans impacter le domaine.

---

## Décision

Créer un nouveau Java record `TransactionDto(String type, BigDecimal amount, String date)`
dans le package `adapter/in/web/`.

Le mapping `Transaction → TransactionDto` appartient exclusivement à `AccountController`
(adaptateur driving HTTP). `AccountUseCase.getStatement()` retourne `List<Transaction>`
(type domaine pur). La conversion de `Transaction.Type` en String et de `Instant` en
String ISO 8601 se fait dans l'adaptateur, pas dans le service ni dans le domaine.

### Contract shape — Effect Isolation (Mandate-12)

**`AccountUseCase.getStatement()` est un contrat pure-function** :
- Retourne une valeur immuable (`List<Transaction>` non modifiable — `Collections.unmodifiableList`).
- Aucun effet de bord : pas d'I/O, pas de mutation d'état du compte, pas d'écriture en mémoire.
- Déterministe : appelé deux fois dans le même état de session, produit la même liste.
- Zéro side effect permis dans l'implémentation `AccountService.getStatement()`.

**`AccountController.getStatement()`** est un contrat unbounded-preservation :
- Traduit le plan domaine (`List<Transaction>`) en réponse HTTP (`List<TransactionDto>`).
- Ne mute aucun objet domaine. La conversion `Transaction → TransactionDto` est une transformation pure.

Le crafter DOIT implémenter `AccountService.getStatement()` comme une fonction pure : lecture
de `account.getTransactions()`, tri, retour de liste immuable. Toute mutation dans cette méthode
constitue une violation de ce contrat et doit être rejetée en revue de code et mutation testing.

---

## Conséquences

**Positives :**
- Le package `domain` reste exempt de toute dépendance d'infrastructure (ArchUnit guardrail).
- Le contrat HTTP (`TransactionDto`) peut évoluer (ex. renommer `"DEPOSIT"` en `"deposit"`,
  ajouter un champ `currency`) sans modifier `Transaction` ni `AccountService`.
- Le format de date dans le JSON (`date: String`) est contrôlé dans l'adaptateur — évolution
  future (ex. passer à epoch ms) ne touche pas le domaine.
- `TransactionDto` est testable isolément comme value object HTTP.

**Négatives :**
- Une étape de mapping supplémentaire dans `AccountController` (~5-8 LOC).
- Deux représentations d'une même donnée — risque de désynchronisation si le domaine évolue
  (mitigé : le compilateur force la mise à jour du mapping si les champs de `Transaction` changent).

---

## Alternatives rejetées

### Alternative 1 : Sérialiser `Transaction` directement

Exposer `Transaction` comme réponse JSON en ajoutant des annotations Jackson ou une
configuration de sérialisation.

**Rejeté** : violerait la règle hexagonale (annotations `@JsonProperty` ou `@JsonSerialize`
dans le domaine importeraient `com.fasterxml.jackson.*` — ArchUnit bloque). Alternative sans
annotations : la sérialisation par défaut de `Transaction.Type` (enum) et `Instant` par Jackson
ne produit pas le format désiré sans configuration globale. Couplage fort domaine/HTTP.

### Alternative 2 : Créer un port secondaire `StatementQuery` dédié

Créer un nouveau port driving `StatementQuery` (interface séparée de `AccountUseCase`)
retournant déjà `List<TransactionDto>`.

**Rejeté** : `TransactionDto` est un type HTTP — le faire traverser le port driving ferait
dépendre `application/port/in` du package `adapter/in/web`, violant la dépendance interne.
Phase 2 n'a pas de complexité suffisante pour justifier un port séparé (un seul bounded context,
une seule source de données).

### Alternative 3 : Créer un `TransactionDto` dans le package `application`

Placer le DTO dans `application/` pour le retourner directement depuis `AccountUseCase`.

**Rejeté** : `TransactionDto` est un contrat de transport HTTP (champ `date: String` au format
ISO 8601, `type: String` au lieu de l'enum). Sa forme est dictée par le consommateur HTTP,
pas par les règles métier. Le placer dans `application/` mélangerait les responsabilités.
