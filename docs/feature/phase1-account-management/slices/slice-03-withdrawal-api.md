# slice-03 : POST /api/withdraw → solde mis a jour OU erreur affichee

**job_id** : `job-001`
**Release** : Release 2
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si un POST /api/withdraw valide ne met pas a jour le solde
dans l'UI, ou si un retrait invalide (montant nul/negatif) ne retourne pas 400 Bad Request.
**Dependances** : slice-01-ws-balance-api (PASSED), slice-02-deposit-api (PASSED)

---

## Problem

Marie peut voir son solde et effectuer des depots, mais elle ne peut pas retirer d'argent
depuis l'interface web. Pour obtenir du liquide ou payer une facture, elle doit se rendre
a un guichet ou utiliser un autre canal — ce qui annule le benefice de l'application web.

## Who

- Cliente bancaire (grand public, 35 ans)
- Contexte : compte approvisionne, veut effectuer un retrait depuis son domicile
- Motivation : reduire son solde d'un montant precis, recevoir une confirmation visuelle

## Solution

Un formulaire de retrait permet a Marie de saisir un montant et de confirmer l'operation.
Le frontend envoie POST /api/withdraw avec le montant. Le domaine valide le montant
(strictement positif) et verifie la disponibilite des fonds. En cas de succes, l'UI
affiche la confirmation et le nouveau solde. En cas de montant invalide (zero ou negatif),
l'UI affiche un message d'erreur specifique.
La regle "fonds insuffisants" est traitee dans slice-04.

### Elevator Pitch

- **Before** : Marie a 300,00 EUR dans son compte web mais ne peut effectuer aucun retrait —
  le bouton "Retirer" est present mais ne declenche rien d'utile.
- **After** : Marie clique "Retirer", saisit 80,00 EUR, confirme et voit immediatement
  "Retrait de 80,00 EUR effectue — Nouveau solde : 220,00 EUR".
- **Decision enabled** : Marie decide si le solde restant (220,00 EUR) est suffisant
  pour ses prochaines depenses ou si elle doit effectuer un depot supplementaire.

---

## Domain Examples

### 1 : Retrait standard avec solde suffisant (happy path)
Marie a un solde de 300,00 EUR. Elle clique "Retirer", saisit 80,00 EUR et confirme.
La page affiche "Retrait de 80,00 EUR effectue. Nouveau solde : 220,00 EUR".

### 2 : Retrait exact du solde total — autorise
Thomas a un solde de 150,00 EUR. Il retire exactement 150,00 EUR.
La page affiche "Retrait de 150,00 EUR effectue. Nouveau solde : 0,00 EUR".
Le solde a zero est autorise (SPEC.md + ADR decision).

### 3 : Retrait avec montant nul — refuse
Sofia clique "Retirer" et saisit 0 EUR. L'application affiche
"Montant invalide — le retrait doit etre superieur a 0 EUR". Le solde reste inchange.

---

## UAT Scenarios (BDD)

### Scenario : Le retrait valide diminue le solde affiche dans l'interface
```gherkin
Given Marie a un solde de 300,00 EUR affiche dans son navigateur
When Marie clique sur "Retirer", saisit 80,00 EUR et clique "Confirmer"
Then la page affiche "Retrait de 80,00 EUR effectue"
And le solde affiche est mis a jour a 220,00 EUR sans rechargement de page
```

### Scenario : POST /api/withdraw retourne le nouveau solde en JSON
```gherkin
Given le compte a un solde courant de 300,00 EUR
When Thomas envoie POST /api/withdraw avec le corps {"amount": "80.00"}
Then la reponse HTTP a le statut 200 OK
And le corps de la reponse est {"balance": "220.00"}
And le Content-Type est application/json
```

### Scenario : Le retrait du solde exact ramene le solde a zero
```gherkin
Given Thomas a un solde de 150,00 EUR affiche dans son navigateur
When Thomas effectue un retrait de 150,00 EUR via le formulaire
Then la page affiche "Retrait de 150,00 EUR effectue"
And le solde affiche est 0,00 EUR
```

### Scenario : Un retrait avec un montant nul est refuse
```gherkin
Given Sofia a un solde de 100,00 EUR
When Sofia clique "Retirer", saisit 0 EUR et confirme
Then la page affiche un message d'erreur : "Montant invalide"
And le solde reste affiche a 100,00 EUR
And POST /api/withdraw repond 400 Bad Request
```

---

## Acceptance Criteria

- [ ] POST /api/withdraw {"amount": "80.00"} repond 200 OK avec {"balance": "220.00"} quand le solde est 300,00
- [ ] Le solde affiche dans l'UI est mis a jour apres le retrait sans rechargement de page complet
- [ ] La confirmation affiche le montant retire et le nouveau solde
- [ ] Le retrait exact du solde total (solde → 0,00 EUR) est accepte
- [ ] POST /api/withdraw avec {"amount": "0"} ou montant negatif repond 400 Bad Request
- [ ] Le message d'erreur pour montant invalide est visible inline dans l'UI
- [ ] Le solde reste inchange apres un retrait refuse pour montant invalide

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Effectue un retrait et voit le nouveau solde sans se deplacer
- **De combien** : 100 % des retraits valides (montant > 0, fonds suffisants) mettent a jour le solde
- **Mesure par** : Tests UAT end-to-end + test API POST /api/withdraw
- **Baseline** : 0 % (aucune fonctionnalite de retrait web existante)

---

## Technical Notes

- **Endpoint** : POST /api/withdraw — corps JSON `{"amount": "80.00"}`
- **Validation domaine** : montant doit etre BigDecimal > 0 — sinon IllegalArgumentException → 400
- **Reponse succes** : 200 OK avec `{"balance": "220.00"}`
- **Reponse erreur montant invalide** : 400 Bad Request avec `{"error": "invalid_amount"}`
- **Regle fonds insuffisants** : traitee dans slice-04 (409 Conflict) — hors scope de cette slice
- **Frontend** : fetch POST avec JSON body, mise a jour inline du solde (pas de reload)
- **BigDecimal** : serialise avec 2 decimales en JSON (ADR-002)
- **Persistance** : InMemoryAccountRepository — singleton Spring — coherence garantie en session JVM
- **Dependances** : slice-01 (page HTML), slice-02 (pattern deposit deja etabli)
