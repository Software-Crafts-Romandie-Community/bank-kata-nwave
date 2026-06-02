<!-- markdownlint-disable MD024 -->
# Feature Delta : phase1-account-management

**Projet** : Bank Kata — Software Crafts Romandie  
**Wave** : DISCUSS  
**Date** : 2026-06-02  
**Statut** : Prêt pour DESIGN wave

---

## Wave: DISCUSS / [JTBD-001] Job Analysis

### Job Statement

> When I am learning or practicing software design skills through a kata exercise,
> I want to manage a bank account with deposits, withdrawals, and balance display,
> so I can focus entirely on code quality rather than figuring out what to implement.

**Job ID** : `job-001` (voir `docs/product/jobs.yaml`)

### Dimensions du Job

| Dimension | Contenu |
|-----------|---------|
| Fonctionnel | Déposer, Retirer, Afficher solde, Bloquer retraits invalides |
| Émotionnel | Feedback immédiat, progression visible, confiance construite pas à pas |
| Social | Code kata démontrable en atelier, séparation domaine / interface |

### Four Forces

| Force | Description |
|-------|-------------|
| Push | Sans énoncé, le développeur invente le domaine au lieu de pratiquer les patterns |
| Pull | CLI interactif simple : validation instantanée sans infrastructure externe |
| Anxiety | Solde négatif non géré → démo cassée en atelier |
| Habit | Tests unitaires isolés sans intégration end-to-end → comportement global non vérifié |

### JTBD-to-Story Bridge

| Step job map | Story candidate |
|---|---|
| Define : savoir quoi coder | slice-01 : afficher solde initial |
| Execute : saisir une transaction | slice-02 dépôt / slice-03 retrait |
| Monitor : voir le résultat | Confirmation après chaque opération |
| Confirm : valider règles métier | slice-04 : rejet retrait insuffisant |

---

## Wave: DISCUSS / [SCOPE-001] Scope Assessment

**Résultat : PASS**

| Critère | Valeur | Seuil | Résultat |
|---------|--------|-------|----------|
| User Stories | 4 | ≤ 10 | PASS |
| Bounded Contexts | 1 (Account) | ≤ 3 | PASS |
| Integration points walking skeleton | 1 | ≤ 5 | PASS |
| Effort estimé | ~2 jours | ≤ 2 semaines | PASS |

---

## Wave: DISCUSS / [JOURNEY-001] Mental Model et Happy Path

### Persona

**Développeur kata** — participant aux ateliers Software Crafts Romandie.  
Contexte : exercice d'apprentissage en session autonome ou en mob programming.  
Motivation : pratiquer les patterns de conception (OOP, FP) sur un domaine simple et bien borné.

### Happy Path (6 étapes)

```
[Démarrer]   →   [Déposer]   →   [Retirer]   →   [Afficher solde]   →   [Quitter]
  Voit :           Saisit :        Saisit :         Voit :               Voit :
  Solde 0 €        montant          montant          solde courant        "Au revoir"
  + menu           Voit :           Voit :
                   confirmation     confirmation
                   + nouveau solde  + nouveau solde
```

### Emotional Arc

```
Curiosité  →  Orientation  →  Focus  →  Satisfaction  →  Confiance
(lancement)   (menu clair)   (saisie)  (confirmation)   (kata complet)
```

### Shared Artifacts

| Artefact | Source unique | Consommateurs |
|----------|---------------|---------------|
| `balance` | `Account` domain object | Menu, confirmation dépôt/retrait, affichage solde, message d'erreur |
| `transaction_log` | `Account` domain object | Historique (Phase 2 kata — hors scope Phase 1) |

### Error Paths principaux

| Erreur | Déclencheur | Réponse attendue |
|--------|-------------|-----------------|
| Fonds insuffisants | retrait > solde | Message d'erreur clair, solde inchangé |
| Montant invalide (≤ 0) | saisie négative ou nulle | Rejet avec invitation à ressaisir |
| Saisie non numérique | entrée texte à la place d'un nombre | Rejet avec message explicite |

---

## Wave: DISCUSS / [STORY-MAP-001] User Story Map

### Backbone

| Démarrer | Déposer | Retirer | Consulter | Quitter |
|----------|---------|---------|-----------|---------|
| Afficher solde initial | Saisir montant dépôt | Saisir montant retrait | Voir solde courant | Fermer proprement |
| (Afficher menu) | Voir confirmation + solde | Voir confirmation + solde | — | — |
| — | — | Voir rejet si fonds insuffisants | — | — |

### Walking Skeleton

Slice minimale end-to-end qui connecte toutes les activités :

1. Lancer le programme → voir "Solde : 0,00 €" + menu
2. Déposer 100 € → voir "Solde : 100,00 €"
3. Retirer 30 € → voir "Solde : 70,00 €"
4. Afficher solde → voir "70,00 €"
5. Quitter → fermeture propre

### Releases slicées par outcome

| Release | Slices incluses | Outcome ciblé |
|---------|-----------------|---------------|
| Walking Skeleton | slice-01 | L'objet Account expose un solde cohérent |
| Release 1 | slice-02 | Le solde s'accumule correctement après dépôt |
| Release 2 | slice-03 | Le solde décroît correctement après retrait valide |
| Release 3 | slice-04 | La règle métier "fonds insuffisants" est isolée du CLI |

### Priority Rationale

1. **slice-01** (WS) : valide l'hypothèse fondamentale — Account existe et expose un solde
2. **slice-02** : valide l'accumulation — risque le plus probable d'erreur d'implémentation
3. **slice-03** : valide la décroissance — symétrique au dépôt, dépend de slice-01
4. **slice-04** : valide l'isolation des règles métier — objectif pédagogique clé du kata

---

## Wave: DISCUSS / [REQ-001] System Constraints

- **Langage cible** : non spécifié (kata polyglotte — Java, Python, ou autre au choix du participant)
- **Pas de persistance** : le solde est en mémoire uniquement (Phase 1)
- **Pas de réseau** : pas de serveur, pas de base de données
- **Transaction log** : les opérations doivent être tracées en mémoire pour la Phase 2 (statement), sans contrainte de format en Phase 1
- **Devise** : EUR — format "X,XX €" pour l'affichage (ou équivalent selon localisation du participant)
- **Valeurs limites** : montants en entiers ou décimaux positifs > 0 uniquement

---

## Wave: DISCUSS / [REQ-002] User Stories

Voir fichiers dédiés dans `docs/feature/phase1-account-management/slices/`.

### Index des stories

| Slice | Fichier | Titre | Job ID |
|-------|---------|-------|--------|
| WS | slice-01-display-initial-balance.md | Afficher le solde initial | job-001 |
| S1 | slice-02-deposit.md | Effectuer un dépôt | job-001 |
| S2 | slice-03-withdrawal.md | Effectuer un retrait valide | job-001 |
| S3 | slice-04-insufficient-funds.md | Rejet retrait — fonds insuffisants | job-001 |

---

## Wave: DISCUSS / [KPI-001] Outcome KPIs

### Objectif

> Le développeur kata valide son implémentation de compte bancaire en moins d'une session (< 2 heures),
> sans ambiguïté sur les règles métier, et produit une démo démontrable en atelier.

### Outcome KPIs

| # | Qui | Fait quoi | De combien | Baseline | Mesuré par | Type |
|---|-----|-----------|------------|----------|------------|------|
| 1 | Développeur kata | Valide le walking skeleton end-to-end | 100 % des 4 slices vertes | 0 % (greenfield) | Tests UAT passants | Leading |
| 2 | Développeur kata | Complète le kata Phase 1 sans aide extérieure | ≥ 80 % des participants | Non mesuré | Retour atelier | Leading |
| 3 | Développeur kata | Ne produit pas de solde négatif | 0 occurrence | Non mesuré | Scénarios BDD | Guardrail |

### North Star

**Tous les scénarios BDD des 4 slices passent en une seule session kata.**

### Guardrail

- Solde jamais négatif après une opération
- Aucun crash non géré pendant la session interactive

---

## Wave: DISCUSS / [DOR-001] Definition of Ready — Résultat

| DoR Item | Résultat | Evidence |
|----------|----------|----------|
| Problème clair, langage domaine | PASS | Persona développeur kata, pain point explicite |
| User/persona identifié | PASS | Développeur kata Software Crafts Romandie |
| 3+ exemples domaine avec données réelles | PASS | Alice, Bob, Chloé — données dans chaque slice |
| Scénarios UAT Given/When/Then (3-7) | PASS | 3-4 scénarios par slice |
| AC dérivés des UAT | PASS | AC extraits des scénarios dans chaque slice |
| Right-sized (1-3 j, 3-7 scénarios) | PASS | 4 slices × 0,5 j, 3-4 scénarios chacune |
| Notes techniques | PASS | Contraintes dans "System Constraints" |
| Dépendances tracées | PASS | Ordre de livraison WS → S1 → S2 → S3 |
| Outcome KPIs définis | PASS | Section KPI-001 ci-dessus |

**Statut DoR global : PASSED**

---

## Wave: DISCUSS / [HANDOFF-001] Handoff Package — DESIGN Wave

### Artefacts produits

| Artefact | Chemin | Destinataire |
|----------|--------|-------------|
| Jobs YAML | `docs/product/jobs.yaml` | solution-architect, acceptance-designer |
| Journey YAML | `docs/product/journeys/account-management.yaml` | solution-architect, acceptance-designer |
| Feature delta (ce fichier) | `docs/feature/phase1-account-management/feature-delta.md` | solution-architect |
| Slice-01 | `docs/feature/phase1-account-management/slices/slice-01-display-initial-balance.md` | solution-architect |
| Slice-02 | `docs/feature/phase1-account-management/slices/slice-02-deposit.md` | solution-architect |
| Slice-03 | `docs/feature/phase1-account-management/slices/slice-03-withdrawal.md` | solution-architect |
| Slice-04 | `docs/feature/phase1-account-management/slices/slice-04-insufficient-funds.md` | solution-architect |

### Décisions ouvertes pour DESIGN wave

| # | Question | Impacte |
|---|----------|---------|
| D1 | Format exact des montants (décimaux, arrondi) | Règles de validation saisie |
| D2 | Comportement si le solde est exactement 0 après retrait | Scénario limite slice-03 |
| D3 | Langage de programmation cible | Structure du projet |

### Risques identifiés

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Règle "solde négatif" ambiguë (overdraft autorisé ?) | Faible | Élevé | Tranché en SPEC : pas d'overdraft Phase 1 |
| Kata trop simple → pas de learning pédagogique | Faible | Moyen | Phase 2 (statement) ajoutera complexité |
