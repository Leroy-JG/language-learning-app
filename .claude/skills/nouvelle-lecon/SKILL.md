---
name: nouvelle-lecon
description: Procédure complète pour ajouter (ou corriger) une leçon du cours d'arabe Lurata à partir des PDF du professeur (Tomes de Médine) — rédaction du contenu JSON, vérifications, relecture indépendante, publication de l'APK. À utiliser dès que l'utilisateur demande une nouvelle leçon, un nouveau chapitre ou une modification du contenu des cours.
---

# Ajouter une leçon à Lurata

Règle n° 1 (voir `CLAUDE.md`) : **tout doit être juste à 100 %**. Prendre le temps qu'il faut.
Mieux vaut une leçon de moins qu'une seule erreur.

## 0. Avant de commencer

1. Lire `CLAUDE.md`, `docs/pedagogie.md` (cadre fixe et palette d'outils), `docs/content-format.md`
   (le format, en entier) et `docs/sources.md` (choix déjà faits, notions déjà enseignées).
2. Il faut **les PDF du professeur** pour la leçon. Ils ne sont pas dans le dépôt (dépôt public) :
   si l'utilisateur ne les a pas joints à la conversation, les lui demander. Ne jamais rédiger une
   leçon de mémoire sans ses supports.
3. Lire chaque page des PDF **visuellement** (outil Read avec `pages`), pas avec `pdftotext`, qui
   mélange les harakat. Noter : le vocabulaire (avec harakat exactes), les règles, les exemples, les
   exercices, les pluriels donnés.
4. Faire l'inventaire de ce qui est **déjà enseigné** : tous les `items.json` et les astuces des
   leçons précédentes (`app/src/main/assets/content/ar/medine-t1/lNN/`). Une nouvelle leçon ne peut
   s'appuyer que là-dessus et sur ce qu'elle enseigne elle-même, **avant** l'exercice concerné.

## 1. Plan du chapitre

Copier la structure des leçons existantes (`l01` à `l03`), qui marche bien :
- Dossier `medine-t1/lNN/`, chapitre ajouté à la fin de `chapters` dans `medine-t1/level.json`.
- `chapter.json` : 3 à 5 parties (`sections`). Schéma habituel : éventuel rappel (`recap`) →
  notion de grammaire (`grammar-N`, astuces + exercices) → vocabulaire (`vocab-N`, 4 à 6 mots par
  activité) → pratique (`practice-N`) → pluriels (`plurals-N`) → dialogue (`dialogue-N`) →
  **Bilan du chapitre** (`quiz`, toujours en dernier).
- Chaque activité : 8 à 16 écrans, selon le **cadre fixe** de `docs/pedagogie.md` (présenter avant
  d'exercer, alterner, difficulté qui monte, finir sur une production ou un jeu).
- **Choisir pour chaque notion l'outil le plus adapté** dans la palette de `docs/pedagogie.md` :
  transformation → `compare`, mot composé → `breakdown`, classement → `sort`, repérage → `spot`,
  orthographe → `build`, thème illustré → `memory`, rappel → `gallery`, piège → `note` `warning`…
  Au moins un jeu par chapitre. Si rien ne convient, suivre la partie 4 du guide (nouveau type).
- Écrire ce plan (parties, activités, outils choisis et pourquoi) avant le JSON.
- Le titre d'activité est court et parlant (« Le corps », « À la mosquée et à table »).

## 2. Rédaction

`items.json` (§7 du format) — pour chaque mot ou phrase :
- `id` stable `w:<translit>` / `p:<…>` (jamais réutiliser un id existant, même supprimé).
- `ar` entièrement vocalisé (y compris tanwin, shadda, sukun, hamza de liaison écrite comme dans les
  leçons existantes : `اِسْمٌ`, `الْكِتَابُ` ; `هٰذَا` avec alif suscrit). `fr` exact (« un/une » pour le tanwin, « du/de la » pour
  une matière). `translit` saisie à la main, cohérente avec les leçons précédentes.
- `gender` (`m`/`f`) pour **chaque nom** ; jamais pour adjectifs, particules, phrases.
- `plural` si le PDF le donne (vérifié dans un dictionnaire de référence), `icon` si une icône
  existe (`core/.../content/Icons.kt`), `note` pour ce qu'il faut savoir, `example` court.

Activités : respecter exactement le §9 du format. Points d'attention :
- QCM : les distracteurs doivent être **faux sans ambiguïté**. Ne pas laisser un distracteur ne
  différer que par هٰذَا/ذٰلِكَ (le validateur le refuse) ni par un détail que l'élève ne peut pas
  trancher. En cas de doute, écrire des `distractors` explicites.
- `typing` / `order` / `fill_gap` : lister **toutes** les réponses justes (`answers`), y compris
  l'ordre des mots alternatif et les réponses courtes acceptables.
- Audio : une question ne doit pas être trahie par son audio (ex. demander l'arabe d'un mot en le
  faisant entendre). Utiliser alors une consigne en français.
- Astuces (`tip`) : texte français balisé `{ar|…}`, `**gras**`, tableaux, exemples écoutables,
  encadrés `note`. Typographie française, phrases courtes.
- **Explications d'oral** : chercher ce qu'un bon professeur ajouterait (règle générale derrière
  une exception, prononciation, famille de mots, faux amis avec le français…). Ne l'ajouter que si
  c'est certain et pertinent à ce point du cours. Sources acceptables : règles classiques
  (Ibn ʿAqīl, Ibn Hishām…), dictionnaires (Lisān al-ʿArab, al-Muʿjam al-Wasīṭ).
- Insérer un écran entre deux autres : id avec suffixe (`…#7b`), jamais renuméroter.

## 2 bis. Pièges rencontrés au chapitre 4 (à relire avant de rédiger)

Les relecteurs les ont tous trouvés ; autant les éviter dès l'écriture.
- **Voyelles masquées** : l'option « masquer les harakat » rend ambigus مِنْ / مَنْ, فِي / فَيْ…
  dans les cartes `sort` (arabe seul) et les consignes. Reformuler ou changer de mot.
- **Ordre des notions** : ne citer aucun mot (même dans un exemple ou une dictée) avant sa
  présentation dans l'ordre des sections. Un exercice du livre qui utilise des mots d'une partie
  ultérieure se place après cette partie (documenter dans `docs/sources.md`).
- **Règles sans condition** : toujours « en général », « la plupart », « sans article ni annexion »
  quand il existe des exceptions (fatha des noms sans tanwin, kasra après préposition, tanwin,
  noms propres féminins, pluriel en -ūna = personnes de sexe masculin).
- **Défini / indéfini** : le français et l'arabe doivent concorder (شَمْسٌ ≠ « le soleil »).
- **Traductions** : « salle de bains », « salle de classe », « d'Inde », « de la part de » ; une
  phrase française ambiguë (« à La Mecque ») doit être reformulée ; les pays n'ont pas de `gender`
  (point à trancher avec l'utilisateur, voir `docs/sources.md`).
- **Réponses acceptées** : ajouter à `answers` toute autre réponse juste (dialogues) ; ne pas
  accepter un ordre des mots non standard.
- **Fautes du PDF** : les corriger dans l'app et le noter dans `docs/sources.md`.
- **Longueur** : un bilan de 16 écrans suffit ; ne pas répéter les mêmes mots entre deux écrans du
  bilan. Ids de bilan non contigus : normal, ne pas renuméroter.
- **Nouvelle icône** : PNG OpenMoji (`tools/render_icons.js`) + `Icons.kt` + `IconImages.kt` +
  `docs/licenses/OpenMoji.txt` ; la CI compile l'app, elle seule vérifie les imports Compose.
- **Environnement** : Maven Central répond parfois 429 → relancer Gradle en boucle (`-q`). Une limite
  d'usage peut interrompre les relecteurs : les reprendre avec SendMessage. Chaque push crée une
  release : ne donner à l'utilisateur que le lien du **dernier** run réussi (les précédents sont
  annulés ou non relus).
- **Méthode qui a marché** : plan écrit d'abord → rédaction → validateur → 4 relecteurs (arabe,
  exercices, français/pédagogie, fidélité au PDF) → vérification personnelle de chaque remarque →
  corrections → 2e relecture ciblée sur le diff → docs → push → CI.

## 3. Vérifications automatiques

```
./gradlew -p core test validateContent
python3 tools/preview_content.py app/src/main/assets/content/ar <scratchpad>/apercu.html
```
Le validateur doit dire `OK`. Lire l'aperçu HTML en entier, écran par écran, comme un élève.

## 4. Relecture indépendante (obligatoire)

Lancer **en parallèle** au moins trois sous-agents `relecteur-arabe` (voir `.claude/agents/` ; si ce
type d'agent n'est pas proposé, lancer un agent général en lui demandant de lire et d'appliquer
`.claude/agents/relecteur-arabe.md`), avec des angles différents :
1. **Arabe** : chaque mot lettre par lettre, cas, accords, pluriels, genres, fidélité aux PDF
   (donner le chemin des PDF).
2. **Exercices** : chaque bonne réponse est juste, aucun distracteur n'est aussi juste, toutes les
   réponses justes sont acceptées, rien n'exige une notion pas encore vue (ordre des sections).
3. **Français et pédagogie** : traductions exactes, explications vraies, explications d'oral
   manquantes et certaines.

Puis **vérifier soi-même chaque remarque** (les relecteurs se trompent aussi) et ne corriger que
ce qui est confirmé. Relancer une relecture sur le diff des corrections.

## 5. Publication

1. Mettre à jour `docs/sources.md` (notions enseignées, choix faits face au PDF, points reportés).
2. Commit en français, clair. Push sur la branche de la conversation.
3. Suivre la CI GitHub jusqu'au bout ; corriger si elle échoue.
4. Donner à l'utilisateur, en français : ce qui a été ajouté, les choix faits (et pourquoi), ce
   qui reste incertain, et le lien direct
   `https://github.com/Leroy-JG/language-learning-app/releases/download/v0.1.<n>/lurata.apk`.
5. Proposer de fusionner la branche dans `main` (pull request) pour que la prochaine conversation
   parte de ce travail.
