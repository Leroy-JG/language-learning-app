# Loura — règles du projet

App Android personnelle pour apprendre l'arabe (interface en français), inspirée de Busuu et de
Duolingo, **100 % hors ligne**. Cursus : Tomes de Médine (tome 1 : leçons 1 à 23, « Révision 1 » et « Révision 2 » publiées,
le tome 1 est terminé ; tome 2 commencé : leçons 1 à 12 publiées, niveau `medine-t2`, voir `docs/sources-t2-lecon-1.md` à `docs/sources-t2-lecon-12.md`), à partir
des supports PDF du professeur de l'utilisateur. Le but est aussi de **donner
envie** d'apprendre l'arabe : la qualité du design, de l'UX et des animations compte.

L'utilisateur est francophone et non développeur : lui répondre **en français**, simplement, et lui
donner **le lien direct de l'APK** à chaque version publiée.

## Règle n° 1 : le contenu doit être juste à 100 %

C'est une application d'apprentissage : une seule erreur dans un cours (harakat, terminaison,
traduction, explication, bonne réponse d'un exercice, distracteur qui serait aussi juste) apprend
une faute à l'utilisateur. Aucune approximation n'est acceptable.

Pour tout ajout ou modification de contenu (`app/src/main/assets/content/`) :
- Vérifier chaque mot arabe lettre par lettre : harakat, shadda, tanwin, sukun, hamza, alif waṣla,
  tāʾ marbūṭa, cas grammatical selon la fonction dans la phrase, accord, pluriels.
- Vérifier chaque phrase française : sens exact, défini/indéfini identique à l'arabe, orthographe,
  typographie française (« guillemets », espace avant ? ! : ;).
- Chaque explication doit être vraie, sans simplification qui la rendrait fausse (« la plupart »
  plutôt que « tous » s'il existe des exceptions).
- Chaque exercice : la réponse désignée est juste, aucune autre option n'est acceptable (y compris
  harakat masquées, et هٰذَا / ذٰلِكَ qui se traduisent tous deux « c'est »), et rien n'exige une
  notion pas encore enseignée **à ce point du parcours** (ordre des sections de `chapter.json`).
  Si une autre réponse est juste, l'ajouter à `answers` plutôt que la refuser.
- Rester fidèle aux supports du professeur (PDF des Tomes de Médine fournis par l'utilisateur).
- Chaque nom présenté porte son genre (`"gender": "m"` ou `"f"`) : l'app l'affiche sous la traduction.
  Pas de genre pour les adjectifs (il dépend du nom décrit), particules et phrases.
- Compléter les supports du professeur par ce qu'un bon enseignant dirait à l'oral, **uniquement
  quand c'est pertinent à ce point du cours et certain** (règle de grammaire établie, dictionnaire de
  référence) : ex. « la plupart des parties du corps qui vont par paire sont féminines ». À chaque
  nouvelle leçon, chercher activement ces explications manquantes ; ne jamais en inventer.
- En cas de doute sur un point de langue : ne pas l'écrire, ou le signaler à l'utilisateur.
- Faire relire le contenu par des relecteurs indépendants (sous-agent `relecteur-arabe`, voir
  `.claude/agents/`) et **vérifier soi-même chaque remarque** avant de corriger (les relecteurs se
  trompent aussi). `./gradlew -p core test validateContent` doit passer.

**Ajouter une leçon : suivre la procédure `.claude/skills/nouvelle-lecon/SKILL.md`** (étape par
étape, avec la liste de contrôle). **Concevoir une leçon : `docs/pedagogie.md`** (cadre fixe pour la
cohérence de l'app, palette d'outils à choisir librement pour chaque notion, et méthode pour créer
un nouveau type d'écran si aucun ne convient). Historique des choix de contenu : `docs/sources.md` (tome 1), `docs/sources-t2-lecon-1.md`, `docs/sources-t2-lecon-2.md`, `docs/sources-t2-lecon-3.md`, `docs/sources-t2-lecon-4.md`, `docs/sources-t2-lecon-5.md`, `docs/sources-t2-lecon-6.md`, `docs/sources-t2-lecon-7.md`, `docs/sources-t2-lecon-8.md`, `docs/sources-t2-lecon-9.md`, `docs/sources-t2-lecon-10.md`, `docs/sources-t2-lecon-11.md` et `docs/sources-t2-lecon-12.md` (tome 2, leçons 1 à 12). **Tome 2** : vue d'ensemble et plan des 31 cours dans `docs/tome2-vue-densemble.md` (à lire avant de créer un chapitre du tome 2). Les défis notés, repris des quiz du professeur (types `test` et `exam`, écran `multi`) : `docs/content-format.md` §9.15 et `docs/sources-quiz.md`.

## Autres règles

- Format du contenu : `docs/content-format.md` (ids stables, **jamais renommés ni réutilisés** : la
  progression et les révisions sont enregistrées par id). Pour insérer un écran entre deux autres,
  utiliser un suffixe (`…#3b`) au lieu de renuméroter.
- Interface et messages en français, dans `res/values/strings.xml` (pas en dur). Pas de permission
  Internet.
- La progression de l'utilisateur ne doit jamais être perdue : pas de migration destructive
  (`fallbackToDestructiveMigration` interdit), même clé de signature pour chaque version.
- Ne jamais committer la clé de signature en clair (seule `signing/release.keystore.enc` est versionnée).
- Le dépôt est **public** : ne pas y mettre les PDF du professeur (droits d'auteur) ni de données
  personnelles.

## Environnement et commandes

- Le SDK Android n'est pas accessible dans l'environnement cloud (dl.google.com bloqué) : l'app se
  compile **uniquement sur GitHub Actions**. `core/` (Kotlin pur) se compile et se teste localement :
  `./gradlew -p core test validateContent` (ajouter `--offline` si les dépendances sont en cache).
  Si Maven Central refuse les téléchargements (erreur 404 ou 429 avec un en-tête `retry-after` très long), passer par le miroir Google, qui répond :
  `./gradlew -p core test validateContent -q -I miroir.init.gradle` avec, dans `miroir.init.gradle` (hors du dépôt),
  `allprojects { buildscript { repositories { maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") } } }; repositories { maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") } } }`.
- Le code `app/` ne peut donc pas être compilé ici : relire très soigneusement chaque modification
  Compose (imports, portée des `RowScope`/`ColumnScope`, types) avant de pousser, puis vérifier que
  la CI passe (outil GitHub `actions_list` / `get_job_logs`).
- `core/` est compilé avec `allWarningsAsErrors` : pas d'avertissement toléré.
- Noms de tests Kotlin entre backticks : pas de `:` ni de `%`.
- Piège de l'outil Write : il transforme les échappements ` ` en caractères réels ; dans
  `strings.xml`, garder les espaces insécables sous forme ` `.
- Aperçu lisible d'un cours sans compiler : `python3 tools/preview_content.py app/src/main/assets/content/ar apercu.html`.
- Les PDF du professeur : les lire **visuellement** (outil Read avec `pages`) ; `pdftotext` mélange
  les harakat de l'arabe.

## Publier une version

Chaque push sur n'importe quelle branche lance `.github/workflows/android.yml` : tests de `core`,
validation du contenu, compilation, signature avec la clé définitive (secret `SIGNING_PASSPHRASE`)
puis publication de la release `v0.1.<numéro du run>`. Après le push :
1. Trouver le run du dernier commit (`actions_list`, filtre par branche) et attendre sa fin.
2. En cas d'échec, lire les logs (`get_job_logs`), corriger, repousser.
3. Donner le lien : `https://github.com/Leroy-JG/language-learning-app/releases/download/v0.1.<n>/loura.apk`
   (il s'installe par-dessus l'ancienne version, la progression est conservée).

Branches : chaque conversation travaille sur sa branche `claude/…`, puis la fusionne dans `main`
(pull request) pour que la conversation suivante parte du travail à jour.

## Architecture (où est quoi)

Carte détaillée fichier par fichier avec numéros de ligne : `docs/carte-du-code.md` (à lire avant de
chercher du code ; la régénérer après un ajout de fichier/fonction : `python3 tools/carte_du_code.py`).
Lire une plage (`Read` avec `offset`/`limit`) plutôt que des fichiers entiers.

- `app/src/main/assets/content/ar/` : les cours en JSON (`course.json` → `medine-t1/level.json` (ou `medine-t2`, outils : `LURATA_NIVEAU=medine-t2`) →
  `lNN/chapter.json`, `items.json`, un fichier par activité). Aucune leçon dans le code.
- `core/src/main/kotlin/io/github/leroyjg/arabe/` (Kotlin pur, testé) :
  - `content/` : modèle (`Model.kt`), chargeur, `Validator.kt` (le build échoue si le contenu est
    incohérent), `Icons.kt` (noms d'icônes autorisés), `CourseIndex.kt` (distracteurs auto).
  - `arabic/` : normalisation, comparaison tolérante des réponses (`AnswerMatcher`), balisage `{ar|…}`.
  - `engine/` : déroulé d'une activité (`ActivitySession`), correction des exercices, progression
    (rien n'est verrouillé : l'utilisateur fait ce qu'il veut quand il veut).
  - `review/` : répétition espacée **FSRS-5** (`Fsrs.kt`), cartes, `ReviewPlanner` (séances de
    révision Vocabulaire / Grammaire).
  - `tools/ContentTool.kt` : tâches Gradle `validateContent` et `audioManifest`.
- `app/src/main/java/io/github/leroyjg/arabe/` (Compose, Material 3) :
  - `ui/main/MainScreen.kt` : barre d'onglets Apprendre / Réviser / Paramètres.
  - `ui/home/HomeScreen.kt` : parcours (chapitres en accordéon).
  - `ui/review/ReviewScreen.kt` : onglet Réviser.
  - `ui/settings/SettingsScreen.kt`, `data/Settings.kt` (SharedPreferences « reglages »).
  - `ui/activity/` : lecteur d'exercices (`ActivityScreen`, `PlayerViewModel`, `ScreenContents`
    pour les écrans classiques et les blocs d'astuce, `Games.kt` pour les mini-jeux, animations, confettis).
  - `ui/GenderTag.kt` (étiquette masculin/féminin), `ui/Decor.kt`, `ui/theme/Theme.kt`, `ui/text/ArabicText.kt`.
  - `data/ProgressDatabase.kt` (Room, version 1) et `data/ReviewDatabase.kt` (base séparée
    `revision.db`, version 1) ; `data/Backup.kt` (export/import JSON).
  - `audio/` : lecture des MP3 (sinon synthèse vocale du téléphone), effets sonores et vibrations.
- Icônes des mots : OpenMoji (CC BY-SA), PNG dans `res/drawable-nodpi/icon_<nom>.png`, liste dans
  `docs/licenses/OpenMoji.txt`. Ajouter une icône : voir `tools/render_icons.js`.
- `tools/deja_enseigne.py` (mots déjà enseignés, 1 ligne chacun) et `tools/nouveau_chapitre.py`
  (squelette de chapitre), `tools/controle_qualite.py` (typographie, structure) : économisent beaucoup de lecture pour une nouvelle leçon.
- `tools/generateur_contenu.py` (écrire un chapitre en Python plutôt qu'en JSON à la main) et `tools/lire_chapitre.py` (affichage lisible d'un chapitre pour la relecture) : voir la procédure `nouvelle-lecon`.
- `tools/make_sounds.py` : génère les effets sonores `res/raw/sfx_*.wav`.

## Reste à faire (par ordre de priorité)

Voir `docs/roadmap.md`.
