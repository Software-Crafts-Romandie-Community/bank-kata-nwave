# slice-01 : Afficher le solde initial

**job_id** : `job-001`  
**Release** : Walking Skeleton  
**Effort estimé** : 0,5 jour  
**Statut DoR** : PASSED

---

## Problem

Alice est développeuse et participe à son premier atelier kata Bank. Elle lance le programme mais
ne sait pas si son objet `Account` est correctement initialisé. Elle n'a aucun retour visuel
avant d'avoir codé des opérations complexes.

## Who

- Développeuse kata (débutante sur ce kata)
- Contexte : première exécution, session d'apprentissage autonome
- Motivation : valider que la structure de base est correcte avant d'aller plus loin

## Solution

Au démarrage, le programme affiche le solde courant du compte (0,00 € à l'initialisation)
et présente le menu des actions disponibles. Le développeur voit immédiatement que `Account`
est opérationnel sans avoir besoin de tests unitaires supplémentaires.

### Elevator Pitch

- **Before** : Alice lance son programme et obtient soit un crash, soit aucun retour — elle ne sait
  pas si son `Account` est bien initialisé.
- **After** : Alice lance le programme et voit instantanément "Solde actuel : 0,00 €" avec le menu
  d'actions — confirmation visuelle que l'objet est prêt.
- **Decision enabled** : Alice décide si elle peut passer à l'implémentation du dépôt
  ou si elle doit corriger l'initialisation de `Account`.

---

## Domain Examples

### 1 : Démarrage standard (happy path)
Alice lance le programme pour la première fois. Le compte n'a reçu aucun dépôt.
Elle voit "Solde actuel : 0,00 €" et le menu avec 4 options numérotées.

### 2 : Relancement après session précédente (état réinitialisé)
Bob relance le programme après l'avoir quitté. Le solde repart à 0,00 € car
il n'y a pas de persistance en Phase 1. Bob le confirme visuellement au démarrage.

### 3 : Démarrage sur machine sans configuration
Chloé lance le kata sur un poste d'atelier vierge. Le programme démarre sans erreur,
affiche 0,00 €, et le menu est lisible sans dépendance externe.

---

## UAT Scenarios (BDD)

### Scenario : Le solde initial est visible au démarrage
```gherkin
Given Alice vient de lancer le programme
When le programme s'initialise
Then Alice voit "Solde actuel : 0,00 €"
And Alice voit un menu avec au moins les options Déposer, Retirer, Afficher le solde, Quitter
```

### Scenario : Le compte démarre toujours à zéro
```gherkin
Given Bob a déjà utilisé le programme dans une session précédente
When Bob relance le programme
Then Bob voit "Solde actuel : 0,00 €"
And le solde n'est pas conservé de la session précédente
```

### Scenario : Le programme démarre sans erreur ni exception
```gherkin
Given Chloé lance le programme sur un poste sans configuration préalable
When le programme démarre
Then aucune exception n'est affichée
And le programme attend une saisie utilisateur
```

---

## Acceptance Criteria

- [ ] Le solde affiché au démarrage est 0,00 €
- [ ] Le menu affiche au minimum 4 options : Déposer, Retirer, Afficher le solde, Quitter
- [ ] Le programme démarre sans exception ni message d'erreur inattendu
- [ ] Le solde n'est pas persisté entre deux sessions (remise à zéro au démarrage)

---

## Outcome KPIs

- **Qui** : Développeur kata (première exécution)
- **Fait quoi** : Valide que Account est initialisé correctement en moins de 10 secondes
- **De combien** : 100 % des lancements affichent 0,00 € sans erreur
- **Mesuré par** : Scénarios BDD passants
- **Baseline** : 0 % (greenfield)

---

## Technical Notes

- Pas de persistance (mémoire uniquement)
- Format d'affichage du montant : à décider par le participant (ex : "0,00 €", "0.00 EUR", "$0.00")
- Pas de dépendance externe requise au démarrage
- Dépendances : aucune (premier slice du walking skeleton)
