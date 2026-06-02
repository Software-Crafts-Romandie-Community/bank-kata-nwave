# slice-04 : Regle fonds insuffisants — domaine rejette, API retourne 409, UI affiche message

**job_id** : `job-001`
**Release** : Release 3
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si un retrait superieur au solde modifie le solde ou
ne retourne pas 409 Conflict avec un corps JSON structurant le message d'erreur.
**Dependances** : slice-01-ws-balance-api (PASSED), slice-02-deposit-api (PASSED), slice-03-withdrawal-api (PASSED)

---

## Problem

Marie tente de retirer plus que son solde disponible. Sans protection explicite,
elle pourrait se retrouver avec un solde negatif — ce qui est inacceptable dans
une application bancaire. De plus, sans message clair, elle ne sait pas pourquoi
l'operation a echoue ni quel est son solde reel.

## Who

- Cliente bancaire (grand public, 35 ans)
- Contexte : veut retirer un montant mais doute de son solde disponible
- Motivation : comprendre immediatement pourquoi l'operation est refusee et quel solde reste disponible

## Solution

Lorsque Marie saisit un montant superieur a son solde, le domaine (Account) lance
une exception metier. L'adaptateur HTTP traduit cette exception en reponse 409 Conflict
avec un corps JSON contenant le code d'erreur et le solde disponible.
L'UI affiche un message explicite : "Fonds insuffisants — solde disponible : X,XX EUR".
Le solde reste strictement inchange.

### Elevator Pitch

- **Before** : Marie tente de retirer 500,00 EUR alors qu'elle n'a que 100,00 EUR.
  L'application ne repond pas clairement — elle ne sait pas si l'operation a eu lieu
  ni quel est son solde reel.
- **After** : Marie voit immediatement "Fonds insuffisants — solde disponible : 100,00 EUR.
  Demande : 500,00 EUR." Le solde reste affiche a 100,00 EUR — aucune donnee perdue.
- **Decision enabled** : Marie decide si elle reduit le montant de son retrait (max 100,00 EUR)
  ou si elle effectue un depot d'abord pour atteindre le montant souhaite.

---

## Domain Examples

### 1 : Tentative de retrait superieur au solde (happy path de l'erreur)
Marie a un solde de 100,00 EUR. Elle tente de retirer 500,00 EUR.
La page affiche "Fonds insuffisants — solde disponible : 100,00 EUR. Demande : 500,00 EUR."
Le solde reste a 100,00 EUR.

### 2 : Tentative de retrait d'un centime de plus que le solde (cas limite)
Thomas a un solde de 50,00 EUR. Il tente de retirer 50,01 EUR.
La page affiche "Fonds insuffisants — solde disponible : 50,00 EUR. Demande : 50,01 EUR."
Le solde reste a 50,00 EUR.

### 3 : Retrait depuis un compte vide
Sofia vient de creer son compte (solde 0,00 EUR). Elle tente de retirer 10,00 EUR.
La page affiche "Fonds insuffisants — solde disponible : 0,00 EUR. Demande : 10,00 EUR."
Le solde reste a 0,00 EUR.

---

## UAT Scenarios (BDD)

### Scenario : Le retrait est refuse si le montant depasse le solde
```gherkin
Given Marie a un solde de 100,00 EUR affiche dans son navigateur
When Marie clique sur "Retirer", saisit 500,00 EUR et clique "Confirmer"
Then la page affiche "Fonds insuffisants — solde disponible : 100,00 EUR"
And le solde reste affiche a 100,00 EUR
And POST /api/withdraw repond 409 Conflict
```

### Scenario : POST /api/withdraw retourne 409 avec detail d'erreur en JSON
```gherkin
Given le compte a un solde de 100,00 EUR
When Thomas envoie POST /api/withdraw avec le corps {"amount": "500.00"}
Then la reponse HTTP a le statut 409 Conflict
And le corps de la reponse contient {"error": "insufficient_funds", "balance": "100.00"}
And le Content-Type est application/json
```

### Scenario : Le solde reste inchange apres un retrait refuse
```gherkin
Given Marie a un solde de 100,00 EUR
When Marie effectue un retrait de 500,00 EUR (refuse — fonds insuffisants)
And Marie envoie ensuite GET /api/balance
Then la reponse de GET /api/balance retourne {"balance": "100.00"}
And le solde affiche dans l'UI est toujours 100,00 EUR
```

### Scenario : Le retrait d'un centime de plus que le solde est refuse
```gherkin
Given Thomas a un solde de 50,00 EUR
When Thomas tente de retirer 50,01 EUR
Then la page affiche "Fonds insuffisants — solde disponible : 50,00 EUR"
And le solde reste a 50,00 EUR
```

### Scenario : Retrait refuse depuis un compte vide
```gherkin
Given Sofia a un solde de 0,00 EUR (compte sans depot)
When Sofia tente de retirer 10,00 EUR
Then la page affiche "Fonds insuffisants — solde disponible : 0,00 EUR"
And le solde reste a 0,00 EUR
```

---

## Acceptance Criteria

- [ ] POST /api/withdraw avec un montant superieur au solde repond 409 Conflict
- [ ] La reponse 409 contient `{"error": "insufficient_funds", "balance": "<solde_reel>"}` en JSON
- [ ] L'UI affiche "Fonds insuffisants — solde disponible : X,XX EUR" apres un retrait refuse
- [ ] Le solde affiche dans l'UI reste inchange apres un retrait refuse pour fonds insuffisants
- [ ] GET /api/balance apres un retrait refuse retourne le meme solde qu'avant la tentative
- [ ] La regle "solde jamais negatif" est enforced par le domaine (Account), pas par l'adaptateur HTTP
- [ ] Le retrait de 0,01 EUR de plus que le solde est refuse (cas limite exact)
- [ ] Le retrait depuis un compte vide (solde 0,00 EUR) est refuse avec 409

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Comprend immediatement pourquoi son retrait est refuse et connait son solde reel
- **De combien** : 0 occurrence de solde negatif — guardrail absolu
- **Mesure par** : Tests UAT (scenarios BDD 409) + verification GET /api/balance apres rejet
- **Baseline** : Non mesure (greenfield — regle a implanter from scratch)

---

## Technical Notes

- **Endpoint** : POST /api/withdraw — corps JSON `{"amount": "500.00"}`
- **Exception domaine** : `InsufficientFundsException` levee par `Account.withdraw()` quand montant > solde
- **Mapping HTTP** : `@ExceptionHandler(InsufficientFundsException.class)` → 409 Conflict
- **Reponse erreur** : `{"error": "insufficient_funds", "balance": "<solde_courant>"}` en JSON
- **Regle d'isolation** : la logique de rejet est EXCLUSIVEMENT dans le domaine — zero condition dans le controller
- **BigDecimal** : comparaison exacte `amount.compareTo(balance) > 0` — pas de soustraction puis test negatif
- **Frontend** : detecter le statut 409 dans le fetch handler, afficher le message inline
- **Dependances** : slice-01, slice-02, slice-03 (tous PASSED)
