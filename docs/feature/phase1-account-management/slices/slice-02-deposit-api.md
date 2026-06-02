# slice-02 : POST /api/deposit → solde mis a jour dans l'UI

**job_id** : `job-001`
**Release** : Release 1
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si un POST /api/deposit avec un montant valide ne met pas a jour
le solde affiche dans le navigateur sans rechargement de page.
**Dependances** : slice-01-ws-balance-api (PASSED)

---

## Problem

Marie sait que son solde est visible mais ne peut pas alimenter son compte depuis
l'interface web. Chaque fois qu'elle veut deposer de l'argent, elle doit passer par
un guichet physique ou appeler sa banque — processus long et contraint par les horaires.

## Who

- Cliente bancaire (grand public, 35 ans)
- Contexte : compte ouvert, solde visible, veut effectuer un depot depuis son domicile
- Motivation : augmenter son solde immediatement, recevoir une confirmation visuelle

## Solution

Un formulaire de depot permet a Marie de saisir un montant et de confirmer l'operation.
Le frontend envoie POST /api/deposit avec le montant. Le domaine valide et met a jour
le solde. L'UI affiche la confirmation et le nouveau solde sans rechargement de page.

### Elevator Pitch

- **Before** : Marie voit son solde dans le navigateur mais n'a aucun moyen d'effectuer
  un depot — le bouton "Deposer" est present mais sans action possible.
- **After** : Marie clique "Deposer", saisit 150,00 EUR, confirme, et voit immediatement
  "Depot de 150,00 EUR effectue — Nouveau solde : 150,00 EUR".
- **Decision enabled** : Marie decide si elle continue avec d'autres operations (solde
  suffisant pour un retrait) ou si elle re-depose un autre montant.

---

## Domain Examples

### 1 : Depot standard (happy path)
Marie a un solde de 0,00 EUR. Elle clique "Deposer", saisit 150,00 EUR et confirme.
La page affiche "Depot de 150,00 EUR effectue. Nouveau solde : 150,00 EUR".

### 2 : Depot successif — accumulation correcte
Thomas a deja depose 200,00 EUR. Il effectue un second depot de 50,00 EUR.
La page affiche "Depot de 50,00 EUR effectue. Nouveau solde : 250,00 EUR".

### 3 : Depot avec montant invalide (montant nul)
Sofia clique "Deposer" et saisit 0 EUR. L'application affiche
"Montant invalide — le depot doit etre superieur a 0 EUR". Le solde reste inchange.

---

## UAT Scenarios (BDD)

### Scenario : Le depot augmente le solde affiche dans l'interface
```gherkin
Given Marie a un solde de 0,00 EUR affiche dans son navigateur
When Marie clique sur "Deposer", saisit 150,00 EUR et clique "Confirmer"
Then la page affiche "Depot de 150,00 EUR effectue"
And le solde affiche est mis a jour a 150,00 EUR sans rechargement de page
```

### Scenario : POST /api/deposit retourne le nouveau solde en JSON
```gherkin
Given le compte a un solde courant de 0,00 EUR
When Thomas envoie POST /api/deposit avec le corps {"amount": "150.00"}
Then la reponse HTTP a le statut 200 OK
And le corps de la reponse est {"balance": "150.00"}
And le Content-Type est application/json
```

### Scenario : Plusieurs depots s'accumulent correctement
```gherkin
Given Thomas a deja depose 200,00 EUR dans la session courante
When Thomas effectue un nouveau depot de 50,00 EUR via le formulaire
Then la page affiche "Depot de 50,00 EUR effectue"
And le solde affiche est 250,00 EUR
```

### Scenario : Un depot avec un montant nul est refuse
```gherkin
Given Sofia a un solde de 100,00 EUR
When Sofia clique "Deposer", saisit 0 EUR et confirme
Then la page affiche un message d'erreur : "Montant invalide"
And le solde reste affiche a 100,00 EUR
And POST /api/deposit repond 400 Bad Request
```

---

## Acceptance Criteria

- [ ] POST /api/deposit {"amount": "150.00"} repond 200 OK avec {"balance": "150.00"}
- [ ] Le solde affiche dans l'UI est mis a jour apres le depot sans rechargement de page complet
- [ ] La confirmation affiche le montant depose et le nouveau solde
- [ ] POST /api/deposit avec {"amount": "0"} ou montant negatif repond 400 Bad Request
- [ ] Le message d'erreur pour montant invalide est visible inline dans l'UI
- [ ] Le solde reste inchange apres un depot refuse
- [ ] Plusieurs depots successifs s'accumulent correctement (coherence domaine)

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Effectue un depot et voit le nouveau solde sans appeler la banque
- **De combien** : 100 % des depots valides mettent a jour le solde affiche
- **Mesure par** : Tests UAT end-to-end + test API POST /api/deposit
- **Baseline** : 0 % (aucune fonctionnalite de depot web existante)

---

## Technical Notes

- **Endpoint** : POST /api/deposit — corps JSON `{"amount": "150.00"}`
- **Validation domaine** : montant doit etre BigDecimal > 0 — sinon IllegalArgumentException → 400
- **Reponse succes** : 200 OK avec `{"balance": "150.00"}`
- **Reponse erreur** : 400 Bad Request avec `{"error": "invalid_amount"}`
- **Frontend** : fetch POST avec JSON body, mise a jour inline du solde affiche (pas de reload)
- **BigDecimal** : serialise avec 2 decimales en JSON (ADR-002)
- **Persistance** : InMemoryAccountRepository — singleton Spring — coherence garantie en session JVM
- **Dependances** : slice-01 (GET /api/balance et page HTML doivent fonctionner)
