# slice-06 : Filtre par plage de dates — GET /api/statement?from=...&to=...

**job_id** : `job-002`
**Release** : Release 1
**Effort estime** : 1 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si le filtrage par date necessite une modification du
domaine `Account`/`Transaction` plutot qu'un filtrage applicatif sur la liste deja exposee
par slice-05.

---

## Problem

Marie a accumule de nombreuses transactions au fil des mois. Pour verifier une operation
specifique (par exemple, un retrait suspect du mois dernier), elle doit faire defiler une
longue liste au lieu de cibler directement la periode concernee.

## Who

- Cliente bancaire (grand public, 35 ans) — meme persona Marie
- Contexte : possede deja plusieurs transactions etalees sur plusieurs semaines/mois
- Motivation : retrouver rapidement une operation precise sans parcourir tout l'historique

## Solution

Le releve expose deux champs de saisie ("du" / "au"). Quand Marie applique un filtre,
`GET /api/statement?from=2026-06-01&to=2026-06-12` ne retourne que les transactions dont
la date est comprise dans l'intervalle (bornes inclusives).

### Elevator Pitch

- **Before** : Marie doit faire defiler 40 transactions pour retrouver le retrait du 10 juin —
  elle perd du temps et risque de rater la bonne ligne.
- **After** : Marie filtre "du 01/06/2026 au 12/06/2026" et `GET /api/statement?from=2026-06-01&to=2026-06-12`
  ne lui montre que les 2 transactions de cette periode.
- **Decision enabled** : Marie decide si l'operation recherchee a bien eu lieu dans la periode
  attendue, ou si elle doit elargir sa recherche.

---

## Domain Examples

### 1 : Filtre avec resultats partiels (happy path)
Marie a des transactions le 2026-06-01, le 2026-06-10 et le 2026-06-15.
Elle filtre du 2026-06-01 au 2026-06-12 → 2 transactions affichees (06-01 et 06-10).

### 2 : Filtre sans aucun resultat
Marie n'a que des transactions en juin 2026. Elle filtre du 2026-01-01 au 2026-01-31.
Le releve affiche "Aucune transaction sur cette periode" — pas d'erreur.

### 3 : Filtre avec bornes inclusives exactes
Thomas a une transaction exactement le 2026-06-01 a 09h00.
Il filtre du 2026-06-01 au 2026-06-01 → cette transaction est incluse dans le resultat
(la borne de debut est inclusive, pas exclusive).

---

## UAT Scenarios (BDD)

### Scenario : Le filtre par date restreint le releve a la periode demandee
```gherkin
Given Marie a des transactions le 2026-06-01, le 2026-06-10 et le 2026-06-15
When Marie filtre du 2026-06-01 au 2026-06-12
Then elle voit 2 transactions (2026-06-01 et 2026-06-10)
And la transaction du 2026-06-15 n'apparait pas
```

### Scenario : Un filtre sans resultat affiche un etat vide explicite
```gherkin
Given Marie a uniquement des transactions en juin 2026
When Marie filtre du 2026-01-01 au 2026-01-31
Then elle voit le message "Aucune transaction sur cette periode"
And aucune erreur n'est affichee
```

### Scenario : Les bornes de la plage sont inclusives
```gherkin
Given Thomas a une transaction le 2026-06-01 a 09h00
When Thomas filtre du 2026-06-01 au 2026-06-01
Then la transaction du 2026-06-01 apparait dans le resultat
```

### Scenario : Une date de fin avant la date de debut est rejetee
```gherkin
Given Marie ouvre le filtre du releve
When Marie saisit "du 2026-06-15 au 2026-06-01" et applique le filtre
Then la page affiche un message d'erreur "La date de fin doit etre posterieure ou egale a la date de debut"
And aucune requete invalide n'est envoyee au serveur
```

### Scenario : GET /api/statement avec parametres invalides repond 400
```gherkin
Given le serveur Spring Boot est demarre
When un client envoie GET /api/statement?from=2026-06-15&to=2026-06-01
Then la reponse HTTP a le statut 400 Bad Request
And le corps suit le format RFC 7807 Problem Details
```

---

## Acceptance Criteria

- [ ] GET /api/statement?from=X&to=Y ne retourne que les transactions dans l'intervalle [X, Y] inclus
- [ ] Une plage sans transaction renvoie un tableau vide (200 OK), pas une erreur
- [ ] from > to renvoie 400 Bad Request au format RFC 7807 Problem Details
- [ ] Les champs de saisie "du"/"au" sont visibles sur la page d'historique
- [ ] Le filtre cote frontend empeche l'envoi d'une requete si la validation locale echoue (UX)
- [ ] Sans filtre applique, le comportement de slice-05 (releve complet) reste inchange

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Retrouve une transaction specifique en filtrant par periode plutot qu'en
  parcourant la liste complete
- **De combien** : Reduction du temps de recherche d'une transaction cible — 100 % des
  filtres valides renvoient exactement les transactions de la periode demandee (0 faux positif/negatif)
- **Mesure par** : Tests UAT GET /api/statement avec query params + scenarios BDD bornes inclusives
- **Baseline** : Non applicable — fonctionnalite absente avant ce slice (slice-05 n'offre que le releve complet)

---

## Technical Notes

- **Extension additive** : ajoute des query params optionnels `from`/`to` (format ISO 8601,
  `yyyy-MM-dd`) sur l'endpoint existant `GET /api/statement` (slice-05) — pas de nouvel endpoint
- **Validation** : from > to → 400 Bad Request, format RFC 7807 Problem Details (coherent Phase 1)
- **Bornes** : inclusives des deux cotes — `from <= transaction.date <= to`
- **Fuseau horaire** : `Transaction.timestamp` est un `Instant` (UTC) ; la comparaison avec
  `from`/`to` (dates sans heure) doit etre documentee explicitement (ex. `from` = debut de
  journee UTC, `to` = fin de journee UTC) — **Assumption** documentee dans wave-decisions
- **Dependances** : depend de slice-05 (meme endpoint, etendu)
