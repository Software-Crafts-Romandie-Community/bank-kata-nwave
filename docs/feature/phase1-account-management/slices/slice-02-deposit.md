# slice-02 : Effectuer un dépôt

**job_id** : `job-001`  
**Release** : Release 1  
**Effort estimé** : 0,5 jour  
**Statut DoR** : PASSED  
**Dépend de** : slice-01

---

## Problem

Alice a vérifié que son compte démarre à 0,00 €. Maintenant elle doit implémenter
le dépôt, mais elle ne sait pas si son objet `Account` accumule correctement les montants
successifs ou si chaque dépôt écrase le précédent.

## Who

- Développeuse kata (a complété slice-01)
- Contexte : deuxième étape de la session kata, implémentation de la logique de dépôt
- Motivation : valider que le solde augmente du bon montant et s'accumule sur plusieurs dépôts

## Solution

L'utilisateur choisit l'option "Déposer", saisit un montant positif, et voit une confirmation
avec le nouveau solde. Deux dépôts successifs donnent un solde cumulé. Un montant invalide
(négatif, nul ou non numérique) est rejeté avec un message clair.

### Elevator Pitch

- **Before** : Alice dépose 100 € puis 50 €, mais elle ne sait pas si son solde est 150 € ou
  si le deuxième dépôt a écrasé le premier (50 €).
- **After** : Alice voit "Solde actuel : 150,00 €" après les deux dépôts — confirmation visuelle
  que l'accumulation fonctionne.
- **Decision enabled** : Alice décide si elle peut passer à l'implémentation du retrait
  ou si elle doit corriger la méthode `deposit()` de `Account`.

---

## Domain Examples

### 1 : Dépôt unique (happy path)
Alice part d'un solde de 0,00 €. Elle dépose 100 €.
Elle voit "Dépôt de 100,00 € effectué. Solde actuel : 100,00 €".

### 2 : Dépôts successifs (accumulation)
Bob a un solde de 100,00 €. Il dépose 50 €.
Il voit "Dépôt de 50,00 € effectué. Solde actuel : 150,00 €".

### 3 : Dépôt d'un montant invalide
Chloé tente de déposer -20 €. Elle voit "Montant invalide : le dépôt doit être supérieur à 0".
Le solde reste inchangé.

---

## UAT Scenarios (BDD)

### Scenario : Le solde augmente après un dépôt valide
```gherkin
Given Alice a un solde de 0,00 €
When Alice choisit "Déposer" et saisit 100
Then Alice voit "Dépôt de 100,00 € effectué"
And Alice voit "Solde actuel : 100,00 €"
```

### Scenario : Deux dépôts s'accumulent correctement
```gherkin
Given Bob a un solde de 100,00 €
When Bob choisit "Déposer" et saisit 50
Then Bob voit "Dépôt de 50,00 € effectué"
And Bob voit "Solde actuel : 150,00 €"
```

### Scenario : Un montant négatif est rejeté
```gherkin
Given Chloé a un solde de 80,00 €
When Chloé choisit "Déposer" et saisit -20
Then Chloé voit un message d'erreur indiquant que le montant est invalide
And le solde de Chloé reste 80,00 €
```

### Scenario : Un montant nul est rejeté
```gherkin
Given Alice a un solde de 50,00 €
When Alice choisit "Déposer" et saisit 0
Then Alice voit un message d'erreur indiquant que le montant doit être supérieur à 0
And le solde d'Alice reste 50,00 €
```

---

## Acceptance Criteria

- [ ] Après un dépôt valide, le solde augmente exactement du montant saisi
- [ ] Deux dépôts successifs s'accumulent (pas d'écrasement)
- [ ] Un montant nul ou négatif est rejeté avec message d'erreur explicite
- [ ] Le solde reste inchangé après un dépôt invalide
- [ ] Le programme retourne au menu après chaque opération (valide ou invalide)

---

## Outcome KPIs

- **Qui** : Développeur kata (implémentant la méthode `deposit()`)
- **Fait quoi** : Valide que l'accumulation de dépôts est correcte
- **De combien** : 100 % des scénarios dépôt passants
- **Mesuré par** : Scénarios BDD passants
- **Baseline** : 0 % (greenfield)

---

## Technical Notes

- La méthode de dépôt doit ajouter (pas remplacer) le montant au solde existant
- Validation du montant : > 0 (exact) ou > 0 (tolérant aux décimaux selon langage cible)
- Les saisies non numériques doivent être capturées sans faire planter le programme
- Le log de transaction doit enregistrer l'opération (prérequis silencieux pour Phase 2)
- Dépendances : slice-01 (Account initialisé)
