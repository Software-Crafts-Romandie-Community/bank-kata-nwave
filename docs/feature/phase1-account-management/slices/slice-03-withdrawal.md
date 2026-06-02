# slice-03 : Effectuer un retrait valide

**job_id** : `job-001`  
**Release** : Release 2  
**Effort estimé** : 0,5 jour  
**Statut DoR** : PASSED  
**Dépend de** : slice-01, slice-02

---

## Problem

Alice a validé le dépôt. Elle doit maintenant implémenter le retrait, mais elle craint
que sa méthode `withdraw()` ne soit pas symétrique à `deposit()` — le solde pourrait
décroître trop vite, pas assez, ou ne pas décroître du tout.

## Who

- Développeuse kata (a complété slice-01 et slice-02)
- Contexte : troisième étape, implémentation de la logique de retrait pour solde suffisant
- Motivation : valider que le solde décroît exactement du montant saisi quand le solde est suffisant

## Solution

L'utilisateur choisit "Retirer", saisit un montant inférieur ou égal au solde, et voit
une confirmation avec le nouveau solde. Un montant invalide (négatif, nul) est rejeté.
Le cas "montant > solde" est traité séparément dans slice-04.

### Elevator Pitch

- **Before** : Alice dépose 100 €, retire 30 €, mais ne sait pas si son solde est 70 €
  ou si `withdraw()` a un bug qui soustrait deux fois ou pas du tout.
- **After** : Alice voit "Retrait de 30,00 € effectué. Solde actuel : 70,00 €" — la symétrie
  avec `deposit()` est confirmée visuellement.
- **Decision enabled** : Alice décide si elle peut passer à la règle des fonds insuffisants
  (slice-04) ou si elle doit corriger `withdraw()`.

---

## Domain Examples

### 1 : Retrait simple avec solde suffisant (happy path)
Alice a un solde de 100,00 €. Elle retire 30 €.
Elle voit "Retrait de 30,00 € effectué. Solde actuel : 70,00 €".

### 2 : Retrait consommant tout le solde (valeur limite)
Bob a un solde de 50,00 €. Il retire exactement 50 €.
Il voit "Retrait de 50,00 € effectué. Solde actuel : 0,00 €".

### 3 : Retrait d'un montant invalide (nul)
Chloé a un solde de 80,00 €. Elle tente de retirer 0 €.
Elle voit "Montant invalide : le retrait doit être supérieur à 0".
Son solde reste 80,00 €.

---

## UAT Scenarios (BDD)

### Scenario : Le solde diminue après un retrait valide
```gherkin
Given Alice a un solde de 100,00 €
When Alice choisit "Retirer" et saisit 30
Then Alice voit "Retrait de 30,00 € effectué"
And Alice voit "Solde actuel : 70,00 €"
```

### Scenario : Le solde peut descendre à zéro exactement
```gherkin
Given Bob a un solde de 50,00 €
When Bob choisit "Retirer" et saisit 50
Then Bob voit "Retrait de 50,00 € effectué"
And Bob voit "Solde actuel : 0,00 €"
```

### Scenario : Un montant nul est rejeté pour un retrait
```gherkin
Given Chloé a un solde de 80,00 €
When Chloé choisit "Retirer" et saisit 0
Then Chloé voit un message d'erreur indiquant que le montant doit être supérieur à 0
And le solde de Chloé reste 80,00 €
```

### Scenario : Un montant négatif est rejeté pour un retrait
```gherkin
Given Alice a un solde de 60,00 €
When Alice choisit "Retirer" et saisit -10
Then Alice voit un message d'erreur indiquant que le montant est invalide
And le solde d'Alice reste 60,00 €
```

---

## Acceptance Criteria

- [ ] Après un retrait valide (montant ≤ solde), le solde diminue exactement du montant saisi
- [ ] Le solde peut atteindre exactement 0,00 € après retrait total
- [ ] Un montant nul ou négatif est rejeté avec message d'erreur explicite
- [ ] Le solde reste inchangé après un retrait invalide (montant ≤ 0)
- [ ] Le programme retourne au menu après chaque opération (valide ou invalide)

---

## Outcome KPIs

- **Qui** : Développeur kata (implémentant la méthode `withdraw()`)
- **Fait quoi** : Valide la symétrie dépôt/retrait sur solde suffisant
- **De combien** : 100 % des scénarios retrait valide passants
- **Mesuré par** : Scénarios BDD passants
- **Baseline** : 0 % (greenfield)

---

## Technical Notes

- Scope de cette slice : retrait valide uniquement (montant ≤ solde)
- Le rejet "fonds insuffisants" (montant > solde) est dans slice-04
- Validation du montant : > 0, identique à la règle de dépôt
- Le log de transaction doit enregistrer l'opération (prérequis silencieux pour Phase 2)
- Dépendances : slice-01 (Account initialisé), slice-02 (dépôt pour alimenter le solde avant test)
