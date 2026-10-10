# language-learning-app

Application Android personnelle d'apprentissage de l'arabe, inspirée du fonctionnement de Busuu,
100 % hors ligne. Le cursus suit les Tomes de Médine.

- `docs/content-format.md` : format des cours (l'app ne contient aucune leçon dans son code).
- `app/src/main/assets/content/` : les cours eux-mêmes.
- `tools/preview_content.py` : aperçu HTML lisible d'un cours, pour relire le contenu sans construire l'app :
  `python3 tools/preview_content.py app/src/main/assets/content/ar apercu.html`
