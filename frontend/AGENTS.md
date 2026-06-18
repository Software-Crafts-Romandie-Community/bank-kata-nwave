# Frontend — Design System Rules

**Source de vérité** : `src/index.css`. Lire ce fichier avant tout travail frontend.
Ne jamais coder une couleur, une fonte ou un espacement en dur.

## Tokens CSS

| Catégorie | Variables | Usage |
|-----------|-----------|-------|
| Couleurs | `--c-bg`, `--c-surface`, `--c-border` | Fond, surface blanche, bordure |
| Vert | `--c-green`, `--c-green-dark`, `--c-green-pale` | Accent primaire, hover, focus |
| Texte | `--c-text`, `--c-text-2`, `--c-text-3` | Principal, secondaire, labels |
| Erreur | `--c-error`, `--c-error-bg` | Messages d'erreur |
| Fontes | `--f-serif`, `--f-mono`, `--f-sans` | Voir Typographie |

## Typographie

- `--f-serif` (DM Serif Display) — wordmark "Banque" uniquement
- `--f-mono` (IBM Plex Mono) — **tous les montants monétaires** sans exception
- `--f-sans` (IBM Plex Sans) — tout le reste (labels, boutons, texte courant)

Labels de section : `font-size: 9.5–10px; font-weight: 500; letter-spacing: 0.18–0.22em; text-transform: uppercase; color: var(--c-text-3)`

## Composants récurrents

**Cards** : `background: var(--c-surface); border: 1px solid var(--c-border); border-top: 3px solid var(--c-green)`

**Boutons** : toujours `.btn` + modificateur. Ne jamais écrire `<button>` sans classe dans l'UI.
- `.btn--deposit` — vert plein (action principale)
- `.btn--withdraw` — ghost (action secondaire / navigation)

**Montants** : toujours `toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })`.
Ne jamais afficher un `number` brut à l'écran.

**Erreurs** : classe `.error-message` (fond rouge pâle + bordure gauche `--c-error`).

**Animations** : `animation: fadeUp 500ms ease both` sur les nouveaux blocs de contenu.

## Conventions structurelles

- `border-radius: 0` partout — coins francs, jamais arrondis
- Tout nouveau composant de vue full-page utilise le shell `.app`
  (`header.app-header` + `main.app-body` + `footer.app-footer`)
  — même structure que `App.tsx` et `StatementView.tsx`
- Nouvelles classes CSS ajoutées dans `src/index.css`, nommées en BEM
  (`block__element--modifier`)
- Pas de fichier CSS par composant — tout dans `index.css`
