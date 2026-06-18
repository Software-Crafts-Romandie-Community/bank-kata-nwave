# ADR-005 — Navigation React conditionnelle vs react-router

**Date** : 2026-06-18
**Statut** : Accepté
**Décideur** : Morgan (nw-solution-architect)
**Contexte feature** : phase2-account-statement

---

## Contexte

Phase 2 ajoute une vue "Relevé de compte" (`StatementView`) à l'application React Phase 1.
L'utilisateur doit pouvoir naviguer depuis la page principale vers le relevé et revenir.

Phase 1 n'a pas de système de navigation — `App.tsx` est la seule vue. Deux approches sont
envisageables pour gérer l'affichage conditionnel :

1. **État conditionnel** (`showStatement: boolean` dans `App.tsx`) — rendu conditionnel inline.
2. **react-router** — bibliothèque de routing avec URLs et `<Routes>/<Route>`.

---

## Décision

Utiliser un **état conditionnel `showStatement: boolean`** dans `App.tsx`.

- Bouton "Relevé" → `setShowStatement(true)` → `StatementView` remplace la vue principale.
- Bouton "Retour" dans `StatementView` → prop callback → `setShowStatement(false)`.
- Aucune nouvelle dépendance npm.

---

## Conséquences

**Positives :**
- Zéro nouvelle dépendance — pas de `react-router-dom` dans `package.json`.
- Cohérence avec Phase 1 : même approche de rendu conditionnel (Phase 1 gère déjà `isError`,
  états de chargement en state local).
- Implémentation immédiate (~5 LOC dans `App.tsx`) — time-to-market préservé.
- Pas de gestion des URLs, pas de `window.history`, pas de `BrowserRouter` à wrapper.

**Négatives :**
- Pas d'URL dédiée pour le relevé — l'utilisateur ne peut pas mettre en favoris `/statement`
  ni partager un lien direct.
- Navigation non reflétée dans l'historique navigateur — le bouton "Précédent" du navigateur
  ne revient pas à la vue principale (mitigé : Phase 2 est une application monosession).
- Si Phase 3 ajoute 2+ vues supplémentaires, migrer vers react-router sera nécessaire —
  mais ce coût sera connu et justifié à ce moment.

---

## Alternatives rejetées

### Alternative 1 : react-router-dom

Ajouter `react-router-dom` (`npm install react-router-dom`), wrapper `App` dans `<BrowserRouter>`,
définir `<Route path="/statement" element={<StatementView />}>`.

**Rejeté** : YAGNI — une seule vue alternative ne justifie pas une bibliothèque de routing.
`react-router-dom` introduit une surface d'API supplémentaire (hooks `useNavigate`, `useParams`,
`Outlet`), une dépendance versionnée à maintenir, et une configuration Spring Boot supplémentaire
(redirection `/**` vers `index.html` pour le routing côté client). Phase 1 n'a pas ce besoin.
À reconsidérer si Phase 3+ introduit 2 vues supplémentaires ou plus.

### Alternative 2 : Tabs / onglets dans la même page

Afficher le relevé dans la même page via un système d'onglets ("Compte" | "Relevé") sans
changement de composant racine.

**Rejeté** : l'expérience utilisateur décrite dans le DISCUSS ("cliquer Relevé → tableau
remplace la vue principale") implique une transition de vue entière, pas des onglets. Un système
d'onglets nécessiterait une bibliothèque UI ou du CSS custom non justifié. L'état conditionnel
produit le même résultat avec moins de complexité.
