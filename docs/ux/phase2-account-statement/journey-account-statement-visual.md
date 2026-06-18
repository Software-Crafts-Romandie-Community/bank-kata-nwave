# Parcours utilisateur — Relevé de compte (Phase 2)

**Feature** : `phase2-account-statement`
**Job** : `job-002` — Consulter l'historique des transactions
**Persona** : Marie — cliente bancaire, 35 ans
**Date** : 2026-06-18

---

## Vue d'ensemble du parcours

```
[Solde affiché]   →   [Clic "Relevé"]   →   [Liste transactions]   →   [Lecture + vérification]
  Phase 1 acquis         Déclencheur          Résultat principal          Outcome job-002
  Feels: orientée        Feels: curieuse      Feels: focalisée            Feels: rassurée
```

**Arc émotionnel** :
```
Curiosité → Attention → Lecture → Rassurance
(je veux voir   (la page     (je parcours    (tout correspond,
ce qui s'est    charge ?)    les lignes)     mon compte est juste)
passé)
```

---

## Étape 1 — Déclencher la consultation du relevé

**Déclencheur** : Marie a effectué plusieurs opérations (dépôts, retraits) dans la session.
Elle veut vérifier ce qui s'est passé avant de fermer le navigateur.

```
+----------------------------------------------------------+
| Bank — Mon Compte                         [localhost]     |
+----------------------------------------------------------+
|                                                          |
|   Solde actuel                                           |
|   +--------------------------------------------+        |
|   |   250,00 EUR                               |        |
|   +--------------------------------------------+        |
|                                                          |
|   [ Déposer ]     [ Retirer ]     [ Relevé ]            |
|                                        ^                 |
|                                   bouton visible,       |
|                                   accessible clavier    |
+----------------------------------------------------------+
```

**État émotionnel** :
- Entrée : Marie est orientée (Phase 1 accomplie), elle a un solde à vérifier
- Sortie : curieuse — elle clique sur "Relevé" pour voir ses transactions

**Artefacts partagés** :
- `${balance}` : source `Account` → affiché ici ET dans le relevé (cohérence)

**Mode d'erreur** :
- Aucune transaction en session : afficher état vide "Aucune transaction" (ne pas afficher liste vide sans message)

---

## Étape 2 — Afficher la liste des transactions (GET /api/statement)

**Action** : Le navigateur envoie `GET /api/statement`. La liste s'affiche.

```
+----------------------------------------------------------+
| Bank — Relevé de compte                   [localhost]     |
+----------------------------------------------------------+
|                                                          |
|   Relevé de compte                                       |
|   +-------------------------------------------------+    |
|   | Date              | Type      | Montant         |    |
|   |-------------------|-----------|-----------------|    |
|   | 18/06/2026 14:32  | Dépôt     | + 300,00 EUR    |    |
|   | 18/06/2026 14:15  | Retrait   |  - 50,00 EUR    |    |
|   | 18/06/2026 14:01  | Dépôt     | + 100,00 EUR    |    |
|   | 18/06/2026 13:45  | Dépôt     | + 200,00 EUR    |    |
|   +-------------------------------------------------+    |
|                                                          |
|   Solde actuel : 550,00 EUR                              |
|                                                          |
|   [ ← Retour au compte ]                                 |
+----------------------------------------------------------+
```

**Variables documentées** :
- `${transaction.date}` : `Transaction.timestamp` (Instant) formaté en `dd/MM/yyyy HH:mm` (timezone locale navigateur)
- `${transaction.type}` : `Transaction.type` (DEPOSIT → "Dépôt", WITHDRAWAL → "Retrait")
- `${transaction.amount}` : `Transaction.amount` (BigDecimal) formaté `X,XX EUR` avec signe
- `${balance}` : `Account` → même source que Phase 1 (cohérence inter-pages)
- Ordre d'affichage : chronologique inverse (plus récent en premier, par `timestamp` décroissant)

**État émotionnel** :
- Entrée : curieuse, un peu anxieuse (les chiffres vont-ils correspondre ?)
- Sortie : rassurée — la liste est claire, chaque opération est identifiable

**Checkpoint d'intégration** :
```
GET /api/statement
  → 200 OK
  → JSON : liste de transactions [{type, amount, date}]
  → Page : tableau lisible, ordre décroissant, état vide si 0 transactions
```

**Mode d'erreur** :
- Liste vide (aucune transaction en session) : message "Aucune transaction enregistrée dans cette session."
- Serveur non démarré : erreur réseau navigateur (hors scope — même traitement que Phase 1)

---

## Étape 3 — Lecture et vérification (outcome)

**Action** : Marie parcourt les lignes et vérifie que le total correspond à son solde.

```
Calcul mental de Marie :
  +300,00  →  +300,00 EUR
  -50,00   →  +250,00 EUR
  +100,00  →  +350,00 EUR
  +200,00  →  +550,00 EUR ✓ = solde affiché

→ Confiance totale. Elle peut fermer le navigateur.
```

**État émotionnel** :
- Entrée : focalisée sur la liste, cherche à reconcilier
- Sortie : rassurée et confiante — le compte est juste

---

## Chemins d'erreur principaux

| Erreur | Déclencheur | Comportement attendu |
|--------|-------------|----------------------|
| Aucune transaction | Session démarrée sans opération | Message "Aucune transaction" — liste vide avec guidance |
| Serveur non démarré | GET /api/statement avant boot | Erreur réseau navigateur (hors scope Phase 2) |
| Transaction corrompue | Données malformées | Ne pas afficher de ligne cassée — journal serveur |

---

## Artefacts partagés — Résumé

| Artefact | Source unique | Consommateurs dans ce parcours |
|----------|---------------|-------------------------------|
| `balance` | `Account` domain object | Page solde (Phase 1), pied de page relevé |
| `transactions` | `Account.getTransactions()` | Tableau du relevé (type + amount + timestamp) |
| `transaction.type` | `Transaction.type` (enum DEPOSIT/WITHDRAWAL) | Colonne "Type" + signe du montant |
| `transaction.amount` | `Transaction.amount` (BigDecimal) | Colonne "Montant" formatée |
| `transaction.timestamp` | `Transaction.timestamp` (Instant) | Colonne "Date" formatée |
