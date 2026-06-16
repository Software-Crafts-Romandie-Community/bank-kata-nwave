# Upstream Changes — Amendement Pagination et tri backend

**Feature** : phase2-transaction-history
**Wave d'origine** : DISCUSS (slice-05, slice-06, slice-07)
**Wave déclenchant le changement** : DESIGN (amendement post-peer-review, exigence produit explicite)
**Date** : 2026-06-16
**Statut** : Soumis pour trace (le product owner du projet a explicitement autorisé l'extension
de périmètre dans cette session — pas de validation séparée `nw-product-owner` requise, mais
documenté ici comme si soumis à review, conformément à la pratique nWave de traçabilité)

---

## Pourquoi ce document existe

L'exigence produit "affichage paginé (avec tri), pagination backend" change le contrat de
`GET /api/statement` que les stories DISCUSS (slice-05, slice-06) décrivaient sans pagination.
Les AC et scénarios Gherkin de ces stories supposaient implicitement que `GET /api/statement`
retourne **toujours la liste complète** (filtrée ou non) en un seul appel. Ce n'est plus le cas :
la réponse est désormais une page parmi N, avec des métadonnées de pagination.

Ce document liste précisément les AC impactés et les nouveaux scénarios requis, **avant**
d'amender les fichiers slice-05/slice-06 eux-mêmes (fait dans la foulée de ce dispatch).

---

## Impact sur slice-05 (`slice-05-ws-statement-api.md`)

### AC existants à amender

| AC original | Impact | Amendement requis |
|---|---|---|
| "GET /api/statement repond 200 OK avec un tableau JSON des transactions" | **Changement de forme de réponse** — le corps n'est plus un tableau JSON nu, c'est un objet `PageResponse` dont `content` est le tableau | Reformuler : "GET /api/statement répond 200 OK avec un objet paginé dont `content` contient les transactions de la page courante" |
| "Les transactions sont triees du plus recent au plus ancien" | Toujours vrai par défaut, mais maintenant un comportement par défaut parmi plusieurs tris possibles, pas le seul comportement | Reformuler : "Par défaut (sans paramètre `sortBy`/`sortDir`), les transactions de la page sont triées du plus récent au plus ancien" |
| "Un compte sans transaction renvoie un tableau vide et la page affiche un etat vide explicite" | Le tableau vide devient `content: []` dans `PageResponse`, avec `totalElements: 0`, `totalPages: 0` | Reformuler avec mention explicite des métadonnées cohérentes pour l'état vide |

### Nouveaux AC requis (pagination de base)

- Le relevé complet est désormais retourné **par page de 20 transactions par défaut** (pas la
  totalité en un seul appel) si `size` n'est pas spécifié
- La réponse inclut les métadonnées `page`, `size`, `totalElements`, `totalPages` cohérentes avec
  le nombre réel de transactions du compte
- Un client peut demander une taille de page parmi `{10, 20, 50}` via le paramètre `size`
- Une taille de page hors de cette liste est rejetée avec 400 Bad Request (RFC 7807)

### Nouveaux scénarios Gherkin requis pour slice-05

```gherkin
Scenario: Le releve complet est paginé avec une taille de page par defaut de 20
  Given Marie a effectue 25 transactions au total
  When Marie ouvre la page d'historique sans preciser de taille de page
  Then la reponse contient 20 transactions dans "content"
  And "totalElements" vaut 25
  And "totalPages" vaut 2
  And "page" vaut 0

Scenario: Marie navigue vers la page suivante du releve
  Given Marie a effectue 25 transactions au total et consulte la page 0 (taille 20)
  When Marie clique sur "Page suivante"
  Then la reponse contient les 5 transactions restantes dans "content"
  And "page" vaut 1

Scenario: Marie choisit une taille de page parmi les valeurs autorisees
  Given Marie a effectue 12 transactions au total
  When Marie selectionne une taille de page de 10
  Then la reponse contient 10 transactions dans "content"
  And "totalPages" vaut 2

Scenario: Une taille de page non autorisee est rejetee
  Given le serveur Spring Boot est demarre
  When un client envoie GET /api/statement?size=37
  Then la reponse HTTP a le statut 400 Bad Request
  And le corps suit le format RFC 7807 Problem Details

Scenario: Une page au-dela du nombre total de pages renvoie un resultat vide coherent
  Given Marie a effectue 5 transactions au total (taille de page 20 -> 1 page)
  When Marie demande la page 99
  Then la reponse HTTP a le statut 200 OK
  And "content" est un tableau vide
  And "totalElements" vaut 5
  And "totalPages" vaut 1
  And "page" vaut 99
```

---

## Impact sur slice-06 (`slice-06-date-range-filter.md`)

### AC existants à amender

| AC original | Impact | Amendement requis |
|---|---|---|
| "GET /api/statement?from=X&to=Y ne retourne que les transactions dans l'intervalle [X, Y] inclus" | Toujours vrai, mais le résultat filtré est désormais lui-même paginé | Reformuler : "...ne retourne, dans la page demandée, que les transactions dans l'intervalle [X, Y] inclus ; `totalElements` reflète le total filtré, pas le total du compte" |
| "Une plage sans transaction renvoie un tableau vide (200 OK), pas une erreur" | Le tableau vide devient `content: []`, `totalElements: 0`, `totalPages: 0` | Reformuler avec mention des métadonnées |
| "Sans filtre applique, le comportement de slice-05 (releve complet) reste inchange" | Toujours vrai — le comportement par défaut (page 0, taille 20, tri date desc) est partagé entre slice-05 et slice-06 | Inchangé tel quel, référence croisée vers les nouveaux AC slice-05 |

### Nouveaux AC requis (filtre + pagination + tri combinés)

- Le filtre par date retourne aussi un résultat paginé et triable : `totalElements`/`totalPages`
  sont calculés **après filtrage, avant pagination** (l'ordre filtre → tri → pagination est
  observable dans le résultat)
- Le tri par montant (croissant ou décroissant) est applicable en combinaison avec un filtre de
  date actif
- `from > to` continue de produire un 400 indépendamment des paramètres de pagination/tri
  présents dans la même requête

### Nouveaux scénarios Gherkin requis pour slice-06

```gherkin
Scenario: Le filtre par date retourne un total coherent avec le sous-ensemble filtre
  Given Marie a 10 transactions au total, dont 3 entre le 2026-06-01 et le 2026-06-12
  When Marie filtre du 2026-06-01 au 2026-06-12
  Then "totalElements" vaut 3 (pas 10)
  And "totalPages" vaut 1 (taille de page par defaut 20)

Scenario: Le tri par montant croissant s'applique a un releve filtre par date
  Given Marie a des transactions de 50 EUR, 200 EUR et 10 EUR entre le 2026-06-01 et le 2026-06-12
  When Marie filtre cette periode et trie par montant croissant
  Then "content" affiche les transactions dans l'ordre 10 EUR, 50 EUR, 200 EUR

Scenario: Le tri par montant decroissant s'applique a un releve filtre par date
  Given Marie a des transactions de 50 EUR, 200 EUR et 10 EUR entre le 2026-06-01 et le 2026-06-12
  When Marie filtre cette periode et trie par montant decroissant
  Then "content" affiche les transactions dans l'ordre 200 EUR, 50 EUR, 10 EUR

Scenario: from > to est rejete meme avec des parametres de pagination/tri presents
  Given le serveur Spring Boot est demarre
  When un client envoie GET /api/statement?from=2026-06-15&to=2026-06-01&page=0&size=10&sortBy=amount
  Then la reponse HTTP a le statut 400 Bad Request
  And le corps suit le format RFC 7807 Problem Details
```

---

## Impact sur slice-07 (`slice-07-transaction-detail.md`)

### Analyse d'impact

Slice-07 (détail, frontend-only, aucun nouvel appel serveur) ne change pas de mécanisme : le
détail reste affiché à partir des données déjà reçues dans `content`, sans fetch supplémentaire.

**Impact identifié** : l'AC "Le retour au releve preserve l'etat du filtre actif" ne mentionnait
que le filtre de date. Avec l'introduction de la pagination et du tri, **l'état à préserver au
retour s'élargit** : filtre **+ page + tri**, pas seulement le filtre. Si Marie consulte la page 2
d'un relevé trié par montant croissant et clique sur une ligne, le retour doit la ramener à la
page 2 triée par montant croissant — pas à la page 0 triée par date décroissante (régression UX
sinon, contraire à l'objectif "Confiance totale" du DISCUSS).

### AC existant à amender

| AC original | Impact | Amendement requis |
|---|---|---|
| "Le retour au releve preserve l'etat du filtre actif (si un filtre etait applique)" | Insuffisant — ne couvre pas page/tri | Reformuler : "Le retour au releve preserve l'etat complet de consultation actif (filtre de date, page courante, champ et direction de tri), pas seulement le filtre" |

### Nouveau scénario Gherkin requis pour slice-07

```gherkin
Scenario: Le retour au releve preserve le filtre, la page et le tri actifs
  Given Thomas a filtre le releve du 2026-06-01 au 2026-06-12, trie par montant croissant,
    et consulte la page 1
  When Thomas clique sur une ligne puis sur "Retour au releve"
  Then il revient a la page 1 du releve filtre du 2026-06-01 au 2026-06-12, trie par montant croissant
  And aucun nouvel appel a GET /api/statement n'est necessaire pour cet affichage
    (les donnees de la page sont deja en memoire cote client)
```

---

## Synthèse des amendements appliqués

| Slice | AC amendés | AC ajoutés | Scénarios Gherkin ajoutés |
|-------|-----------|------------|----------------------------|
| slice-05 | 3 | 4 | 5 |
| slice-06 | 3 | 3 | 4 |
| slice-07 | 1 | 0 | 1 |

Les fichiers `slice-05-ws-statement-api.md`, `slice-06-date-range-filter.md` et
`slice-07-transaction-detail.md` ont été amendés directement à la suite de ce document, dans la
même session, avec l'autorisation explicite du product owner du projet (Sylvain Chabert) pour
cette extension de périmètre post-review.
