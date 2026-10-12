# Lurata

Application Android personnelle d'apprentissage de l'arabe, inspirée du fonctionnement de Busuu,
100 % hors ligne. Le cursus suit les Tomes de Médine.

## Organisation

- `CLAUDE.md` : règles du projet et mémoire pour Claude (à lire en premier).
- `docs/pedagogie.md` : guide de conception des leçons (cadre fixe, palette d'outils, nouveaux types).
- `docs/sources.md` : ce que chaque leçon enseigne et les choix de contenu ; `docs/roadmap.md` : la suite.
- `.claude/skills/nouvelle-lecon/` : procédure pour ajouter une leçon ; `.claude/agents/relecteur-arabe.md` : relecteur.

- `docs/content-format.md` : format des cours (l'app ne contient aucune leçon dans son code).
- `app/src/main/assets/content/` : les cours eux-mêmes.
- `core/` : logique de l'app en Kotlin pur (lecture et validation du contenu, arabe, moteur
  d'exercices). Build Gradle autonome, testable sans le SDK Android : `./gradlew -p core test`.
- `app/` : l'application Android (Jetpack Compose).
- `tools/preview_content.py` : aperçu HTML lisible d'un cours, pour relire le contenu sans construire l'app :
  `python3 tools/preview_content.py app/src/main/assets/content/ar apercu.html`

## Vérifier le contenu

```
./gradlew -p core validateContent
```

## Compilation et signature

GitHub Actions (`.github/workflows/android.yml`) lance les tests, valide le contenu, compile l'APK
et le publie dans les *Releases* du dépôt.

L'APK est toujours signé avec la même clé, pour que chaque mise à jour s'installe par-dessus la
précédente sans perdre la progression. La clé est versionnée **chiffrée**
(`signing/release.keystore.enc`, AES-256) ; la CI la déchiffre avec le secret GitHub
`SIGNING_PASSPHRASE`. Sans ce secret, l'APK est signé avec une clé temporaire et publié comme
pré-version.

## Licences

- Police Noto Naskh Arabic : SIL Open Font License 1.1 (`docs/licenses/OFL-NotoNaskhArabic.txt`).
