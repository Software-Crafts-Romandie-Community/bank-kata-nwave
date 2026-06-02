# slice-01 : Walking Skeleton — GET /api/balance → page HTML affiche le solde

**job_id** : `job-001`
**Release** : Walking Skeleton
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si la pile navigateur → fetch → Spring Boot → domain → JSON → HTML
ne fonctionne pas de bout en bout sans modification du domaine.

---

## Problem

Marie est cliente bancaire. Elle ouvre son application bancaire dans le navigateur mais
voit une page vide ou un message d'erreur. Elle ne sait pas si l'application fonctionne
et ne peut pas verifier son solde sans appeler son conseiller.

## Who

- Cliente bancaire (grand public, 35 ans)
- Contexte : premiere utilisation de l'application web, navigateur standard
- Motivation : verifier son solde immediatement sans action supplementaire

## Solution

Au chargement de la page, le navigateur interroge automatiquement `GET /api/balance`.
Le solde courant est affiche de facon lisible. Deux boutons "Deposer" et "Retirer"
sont visibles et prets a l'emploi. Aucune action manuelle n'est requise de la part
de Marie pour voir son solde.

### Elevator Pitch

- **Before** : Marie ouvre le navigateur et voit une page blanche ou une erreur reseau —
  elle ne peut pas savoir si son compte est accessible.
- **After** : Marie ouvre http://localhost:8080 et voit immediatement "Solde : 0,00 EUR"
  avec deux boutons d'action — confirmation que l'application fonctionne.
- **Decision enabled** : Marie decide si elle peut effectuer une operation (solde lisible)
  ou si elle doit contacter le support (page en erreur).

---

## Domain Examples

### 1 : Premier chargement — compte a zero (happy path)
Marie ouvre http://localhost:8080 pour la premiere fois.
Le compte n'a recu aucun depot. La page affiche "0,00 EUR" et deux boutons.

### 2 : Rechargement apres operation precedente
Marie a depose 200 EUR plus tot dans la session. Elle rechargera la page.
Le fetch GET /api/balance renvoie {"balance": "200.00"} et la page affiche "200,00 EUR".

### 3 : Serveur demarre mais balance non encore touche
Thomas ouvre l'application pour la premiere fois sur sa machine de developpement.
Le compte en memoire est a 0. La page charge en moins de 2 secondes et affiche "0,00 EUR".

---

## UAT Scenarios (BDD)

### Scenario : Le solde est affiche au chargement de la page
```gherkin
Given Marie ouvre son navigateur a http://localhost:8080
When la page se charge completement
Then Marie voit son solde affiche : "0,00 EUR"
And deux boutons "Deposer" et "Retirer" sont visibles et cliquables
```

### Scenario : GET /api/balance retourne le solde courant en JSON
```gherkin
Given le serveur Spring Boot est demarre
When Thomas envoie GET /api/balance
Then la reponse HTTP a le statut 200 OK
And le corps de la reponse est {"balance": "0.00"}
And le Content-Type est application/json
```

### Scenario : Le solde affiche refletchit le solde reel apres depot en session
```gherkin
Given Marie a depose 200,00 EUR dans la session courante
When Marie recharge la page http://localhost:8080
Then la page affiche "200,00 EUR" comme solde courant
```

### Scenario : La page charge sans erreur visible pour l'utilisateur
```gherkin
Given le serveur Spring Boot est demarre sur le port 8080
When Marie ouvre http://localhost:8080 dans son navigateur
Then aucun message d'erreur n'est visible sur la page
And la page repond en moins de 2 secondes
```

---

## Acceptance Criteria

- [ ] GET /api/balance repond 200 OK avec {"balance": "0.00"} quand le compte est vide
- [ ] La page HTML affiche le montant recu depuis l'API (format "X,XX EUR")
- [ ] Deux boutons "Deposer" et "Retirer" sont presents et cliquables au chargement
- [ ] La page charge sans erreur JavaScript visible dans la console navigateur
- [ ] Le temps de reponse de GET /api/balance est inferieur a 200 ms
- [ ] Le solde affiche correspond au solde reel en memoire (coherence fetch → Account)

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Consulte son solde sans action manuelle apres ouverture de la page
- **De combien** : 100 % des chargements affichent un solde valide (pas de NaN ni vide)
- **Mesure par** : Tests UAT end-to-end (navigateur → API → domaine)
- **Baseline** : 0 % (greenfield — aucune UI web existante)

---

## Technical Notes

- **Framework** : Spring Boot 3.x — `@RestController` + `@GetMapping("/api/balance")`
- **Format JSON** : `{"balance": "0.00"}` — BigDecimal serialise en String avec 2 decimales
- **Frontend** : HTML vanilla + `fetch("/api/balance")` au `DOMContentLoaded`
- **Persistance** : InMemoryAccountRepository — singleton Spring (scope JVM, perte au redemarrage)
- **Port** : 8080 par defaut
- **CORS** : non requis si HTML servi par le meme serveur Spring Boot (static resources)
- **Devise** : EUR — "X,XX EUR" dans l'UI, "X.XX" en JSON (separateur decimal anglais)
- **BigDecimal** : type monetaire conserve (ADR-002)
- **Dependances** : aucune (premier slice du walking skeleton — aucun autre slice requis)
