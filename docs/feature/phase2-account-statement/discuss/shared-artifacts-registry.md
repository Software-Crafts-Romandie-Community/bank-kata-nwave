# Shared Artifacts Registry — phase2-account-statement

**Feature** : `phase2-account-statement`
**Date** : 2026-06-18

---

## Registre

```yaml
shared_artifacts:

  balance:
    source_of_truth: "Account domain object — Account.getBalance() via GET /api/balance"
    consumers:
      - "Page principale Phase 1 — solde affiché au chargement"
      - "Pied de page du relevé Phase 2 — solde actuel confirmé"
    owner: "job-001 + job-002 (partagé)"
    integration_risk: "HIGH — divergence entre solde Phase 1 et pied de page Phase 2 = incohérence visible"
    validation: "GET /api/balance et pied de page du relevé doivent afficher le même montant à tout instant"

  transactions:
    source_of_truth: "Account.getTransactions() — List<Transaction> immuable retournée par le domaine"
    consumers:
      - "GET /api/statement → sérialise la liste complète en JSON"
      - "Tableau HTML du relevé — toutes les lignes"
    owner: "job-002"
    integration_risk: "HIGH — la liste affichée doit être la liste exacte du domaine, sans filtrage ni enrichissement non documenté"
    validation: "Nombre de lignes dans le tableau = taille de Account.getTransactions()"

  transaction_type:
    source_of_truth: "Transaction.type (enum : DEPOSIT / WITHDRAWAL)"
    consumers:
      - "Colonne 'Type' du tableau (DEPOSIT → 'Dépôt', WITHDRAWAL → 'Retrait')"
      - "Signe du montant affiché (+ ou -)"
    owner: "job-002"
    integration_risk: "MEDIUM — mauvaise traduction de l'enum en libellé = confusion utilisateur"
    validation: "DEPOSIT → '+' et 'Dépôt' | WITHDRAWAL → '-' et 'Retrait' — vérifiable par test UAT"

  transaction_amount:
    source_of_truth: "Transaction.amount (BigDecimal)"
    consumers:
      - "Colonne 'Montant' du tableau — format X,XX EUR avec signe"
    owner: "job-002"
    integration_risk: "MEDIUM — format BigDecimal JSON → Number (décision Phase 1) doit être cohérent avec l'affichage"
    validation: "Même règle de formatage que Phase 1 : 2 décimales, séparateur décimal selon locale"

  transaction_timestamp:
    source_of_truth: "Transaction.timestamp (Instant)"
    consumers:
      - "Colonne 'Date' du tableau — format dd/MM/yyyy HH:mm"
      - "Tri décroissant du tableau (plus récent en premier)"
    owner: "job-002"
    integration_risk: "MEDIUM — Instant (UTC) doit être affiché dans la timezone locale du navigateur"
    validation: "Les timestamps en tête de liste doivent être plus récents que ceux en bas"
```

---

## Checkpoints d'intégration

| Checkpoint | Validation | Risque |
|------------|------------|--------|
| `balance` cohérent entre Phase 1 et pied de page Phase 2 | GET /api/balance == solde affiché dans relevé | HIGH |
| `transactions` liste complète sans perte | len(tableau) == len(Account.getTransactions()) | HIGH |
| `transaction_type` traduit correctement | DEPOSIT→Dépôt, WITHDRAWAL→Retrait | MEDIUM |
| `transaction_timestamp` formaté lisiblement | "dd/MM/yyyy HH:mm" dans timezone locale | MEDIUM |
| Ordre décroissant confirmé | Première ligne = transaction la plus récente | MEDIUM |

---

## Vocabulaire CLI/API

| Terme domaine | Libellé utilisateur (FR) | Endpoint/Champ JSON |
|---------------|--------------------------|---------------------|
| DEPOSIT | Dépôt | `type: "DEPOSIT"` |
| WITHDRAWAL | Retrait | `type: "WITHDRAWAL"` |
| timestamp | Date | `date` (ISO 8601 serialisé) |
| amount | Montant | `amount` (Number, 2 décimales) |
