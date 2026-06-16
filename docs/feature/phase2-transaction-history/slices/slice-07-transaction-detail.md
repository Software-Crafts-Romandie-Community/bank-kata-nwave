# slice-07 : Detail d'une transaction — vue dediee depuis le releve

**job_id** : `job-002`
**Release** : Release 2
**Effort estime** : 0,5 jour
**Statut DoR** : PASSED
**Learning hypothesis** : infirme si l'affichage du detail necessite une nouvelle source de
donnees au lieu de reutiliser l'element deja present dans la reponse de `GET /api/statement`
(slice-05/06).

---

## Problem

Marie voit une ligne resumee dans le releve ("DEPOT — +150,00 EUR — 15/06/2026") mais pour
certaines verifications (par exemple, confirmer l'heure exacte d'un retrait avant de signaler
un litige), elle a besoin de voir le detail complet de cette transaction precise.

## Who

- Cliente bancaire (grand public, 35 ans) — meme persona Marie
- Contexte : consulte deja le releve (slice-05) et a peut-etre applique un filtre (slice-06)
- Motivation : confirmer les details exacts d'une operation avant de la considerer comme verifiee

## Solution

Chaque ligne du releve est cliquable. Cliquer ouvre une vue detail affichant le type, le
montant et la date/heure complete de la transaction selectionnee, sans nouvelle requete
serveur (les donnees sont deja dans la reponse du releve).

### Elevator Pitch

- **Before** : Marie voit "DEPOT — +150,00 EUR — 15/06/2026" dans le releve mais ne connait
  pas l'heure exacte de l'operation pour la comparer avec son propre justificatif.
- **After** : Marie clique sur cette ligne et voit "DEPOT — 150,00 EUR — 15/06/2026 14:32" —
  l'heure precise confirme que c'est bien l'operation qu'elle recherche.
- **Decision enabled** : Marie decide si cette transaction correspond exactement a l'operation
  qu'elle a en tete, ou si elle doit chercher une autre ligne du releve.

---

## Domain Examples

### 1 : Detail d'un depot (happy path)
Marie clique sur la ligne "DEPOT — +150,00 EUR — 15/06/2026" dans le releve.
La vue detail affiche "Type: DEPOT", "Montant: 150,00 EUR", "Date: 15/06/2026 14:32".

### 2 : Detail d'un retrait
Marie clique sur la ligne "RETRAIT — -50,00 EUR — 10/06/2026" dans le releve filtre.
La vue detail affiche "Type: RETRAIT", "Montant: 50,00 EUR", "Date: 10/06/2026 09:15".

### 3 : Retour au releve depuis le detail
Thomas consulte le detail d'une transaction puis clique sur "Retour au releve".
Il revient a la liste complete (ou filtree) sans perdre l'etat de son filtre precedent.

---

## UAT Scenarios (BDD)

### Scenario : Le detail d'une transaction correspond exactement a la ligne selectionnee
```gherkin
Given Marie voit la transaction du 2026-06-15 de type DEPOT de 150,00 EUR dans le releve
When Marie clique sur cette ligne
Then la vue detail affiche "DEPOT", "150,00 EUR" et la date/heure exacte 2026-06-15
```

### Scenario : Le detail d'un retrait affiche un montant negatif clairement identifie
```gherkin
Given Marie voit la transaction du 2026-06-10 de type RETRAIT de 50,00 EUR dans le releve
When Marie clique sur cette ligne
Then la vue detail affiche "RETRAIT" et "50,00 EUR" (sans ambiguite sur le sens de l'operation)
```

### Scenario : Le retour au releve preserve le filtre actif
```gherkin
Given Thomas a filtre le releve du 2026-06-01 au 2026-06-12 et consulte le detail d'une transaction
When Thomas clique sur "Retour au releve"
Then il revient a la liste filtree du 2026-06-01 au 2026-06-12 (pas le releve complet)
```

### Scenario : Le detail n'effectue pas de nouvel appel serveur
```gherkin
Given Marie a deja recu la liste des transactions via GET /api/statement
When Marie clique sur une ligne du releve
Then la vue detail s'affiche sans nouvelle requete HTTP vers /api/statement
```

### Scenario : Le retour au releve preserve le filtre, la page et le tri actifs
*(Ajoute — amendement pagination/tri backend, 2026-06-16, voir `design/upstream-changes.md`)*
```gherkin
Given Thomas a filtre le releve du 2026-06-01 au 2026-06-12, trie par montant croissant,
  et consulte la page 1
When Thomas clique sur une ligne puis sur "Retour au releve"
Then il revient a la page 1 du releve filtre du 2026-06-01 au 2026-06-12, trie par montant croissant
And aucun nouvel appel a GET /api/statement n'est necessaire pour cet affichage
  (les donnees de la page sont deja en memoire cote client)
```

---

## Acceptance Criteria

- [ ] Chaque ligne du releve (complet ou filtre) est cliquable
- [ ] La vue detail affiche type, montant et date/heure complete (incluant l'heure, pas seulement la date)
- [ ] Le montant affiche est sans ambiguite sur le type d'operation (depot vs retrait)
- [ ] Le retour au releve preserve l'etat complet de consultation actif (filtre de date, page courante, champ et direction de tri), pas seulement le filtre *(amende — voir upstream-changes.md)*

---

## Outcome KPIs

- **Qui** : Cliente bancaire (Marie, grand public)
- **Fait quoi** : Confirme les details complets (incluant l'heure) d'une transaction specifique
- **De combien** : 100 % des clics sur une ligne du releve affichent un detail coherent avec
  la ligne source (0 ecart entre ligne cliquee et detail affiche)
- **Mesure par** : Tests UAT — clic sur ligne puis assertion sur le contenu de la vue detail
- **Baseline** : Non applicable — fonctionnalite absente avant ce slice

---

## Technical Notes

- **Extension additive** : aucune modification serveur necessaire — reutilise les donnees
  deja recues par le frontend depuis `GET /api/statement` (slice-05/06)
- **Implementation** : logique frontend uniquement (etat local, pas de nouvel endpoint)
- **Horodatage** : afficher l'heure complete (`Instant` -> format local lisible, ex. "14:32")
  — fuseau horaire d'affichage a clarifier en DESIGN (meme `Instant` UTC que le domaine)
- **Dependances** : depend de slice-05 (releve) ; compatible avec slice-06 (filtre) sans
  modification additionnelle cote serveur

### Amendement — Pagination et tri backend (2026-06-16, post peer-review DESIGN)

L'etat local frontend a preserver au retour depuis le detail s'elargit : filtre de date **+
page courante + champ de tri + direction de tri**, pas seulement le filtre de date comme
documente initialement. Aucun changement de mecanisme (toujours frontend-only, aucun nouvel
appel serveur) — uniquement un elargissement du perimetre de l'etat preserve. Voir
`docs/feature/phase2-transaction-history/design/upstream-changes.md`.
