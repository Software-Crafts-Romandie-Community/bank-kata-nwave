# slice-04 : Rejet retrait — fonds insuffisants

**job_id** : `job-001`  
**Release** : Release 3  
**Effort estimé** : 0,5 jour  
**Statut DoR** : PASSED  
**Dépend de** : slice-01, slice-02, slice-03

---

## Problem

Alice a implémenté dépôt et retrait. Mais si un utilisateur tente de retirer plus
que son solde, son implémentation actuelle pourrait créer un solde négatif — ce qui
viole la règle métier fondamentale. Alice doit isoler cette règle du CLI et la tester
explicitement.

## Who

- Développeuse kata (a complété les 3 slices précédentes)
- Contexte : dernière étape Phase 1, focus sur l'isolation des règles métier
- Motivation : valider que la règle "pas de solde négatif" est dans le domaine
  (objet `Account`), pas dans le CLI

## Solution

Quand l'utilisateur saisit un montant de retrait supérieur au solde, le programme
affiche un message d'erreur clair indiquant le solde disponible et le montant demandé.
Le solde reste inchangé. L'isolation de la règle dans le domaine est l'objectif
pédagogique clé de cette slice.

### Elevator Pitch

- **Before** : Alice tente de retirer 500 € sur un solde de 70 €. Son programme
  affiche "Solde actuel : -430,00 €" — la règle métier n'est pas implémentée.
- **After** : Alice voit "Retrait refusé : fonds insuffisants. Disponible : 70,00 € —
  Demandé : 500,00 €" et son solde reste 70,00 €.
- **Decision enabled** : Alice décide si la règle métier est bien dans `Account.withdraw()`
  (qui doit lever une exception ou retourner un résultat explicite), et non dans le CLI.

---

## Domain Examples

### 1 : Retrait supérieur au solde (cas principal)
Alice a un solde de 70,00 €. Elle tente de retirer 500 €.
Elle voit "Retrait refusé : fonds insuffisants. Disponible : 70,00 € — Demandé : 500,00 €".
Son solde reste 70,00 €.

### 2 : Retrait d'un euro de plus que le solde (valeur limite)
Bob a un solde de 50,00 €. Il tente de retirer 51 €.
Il voit le message de refus avec "Disponible : 50,00 € — Demandé : 51,00 €".
Son solde reste 50,00 €.

### 3 : Tentative de retrait sur solde vide
Chloé a un solde de 0,00 €. Elle tente de retirer 10 €.
Elle voit "Retrait refusé : fonds insuffisants. Disponible : 0,00 € — Demandé : 10,00 €".
Son solde reste 0,00 €.

---

## UAT Scenarios (BDD)

### Scenario : Un retrait supérieur au solde est refusé
```gherkin
Given Alice a un solde de 70,00 €
When Alice choisit "Retirer" et saisit 500
Then Alice voit un message de refus indiquant que les fonds sont insuffisants
And Alice voit le solde disponible (70,00 €) et le montant demandé (500,00 €)
And le solde d'Alice reste 70,00 €
```

### Scenario : Un retrait d'un euro de plus que le solde est refusé (limite)
```gherkin
Given Bob a un solde de 50,00 €
When Bob choisit "Retirer" et saisit 51
Then Bob voit un message de refus indiquant que les fonds sont insuffisants
And le solde de Bob reste 50,00 €
```

### Scenario : Un retrait sur compte vide est refusé
```gherkin
Given Chloé a un solde de 0,00 €
When Chloé choisit "Retirer" et saisit 10
Then Chloé voit un message de refus indiquant que les fonds sont insuffisants
And le solde de Chloé reste 0,00 €
```

### Scenario : Plusieurs tentatives refusées ne modifient pas le solde
```gherkin
Given Alice a un solde de 70,00 €
When Alice tente deux retraits de 500 € successifs, tous les deux refusés
Then le solde d'Alice est toujours 70,00 € après les deux tentatives
```

---

## Acceptance Criteria

- [ ] Un retrait supérieur au solde est refusé, le solde reste inchangé
- [ ] Le message d'erreur affiche le solde disponible ET le montant demandé
- [ ] La règle de rejet est vérifiable indépendamment du CLI (testable en isolation)
- [ ] Plusieurs retraits refusés successifs ne modifient pas le solde (idempotence du rejet)
- [ ] Un retrait de solde_actuel + 0,01 est refusé (cas limite)

---

## Outcome KPIs

- **Qui** : Développeur kata (implémentant la règle métier dans Account)
- **Fait quoi** : Valide que la règle "pas de solde négatif" est isolée dans le domaine
- **De combien** : 0 occurrence de solde négatif dans tous les scénarios
- **Mesuré par** : Scénarios BDD passants + inspection manuelle de la localisation de la règle
- **Baseline** : Non mesuré (greenfield)

---

## Technical Notes

- Objectif pédagogique clé : la règle doit être dans le domaine (`Account`), pas dans le CLI
- `Account.withdraw()` doit soit lever une exception métier, soit retourner un résultat explicite
  indiquant le refus — décision de design laissée au participant
- Pas d'overdraft : la Phase 1 du kata ne gère pas les découverts autorisés
- Le log de transaction : une tentative refusée n'est PAS enregistrée dans le log (clarification
  utile pour Phase 2)
- Dépendances : slice-01, slice-02 (pour charger un solde), slice-03 (symétrie confirmée)
