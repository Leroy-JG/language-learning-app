---
name: nouvelle-lecon
description: Procédure complète pour ajouter (ou corriger) une leçon du cours d'arabe Loura à partir des PDF du professeur (Tomes de Médine) — rédaction du contenu JSON, vérifications, relecture indépendante, publication de l'APK. À utiliser dès que l'utilisateur demande une nouvelle leçon, un nouveau chapitre ou une modification du contenu des cours.
---

# Ajouter une leçon à Loura

Règle n° 1 (voir `CLAUDE.md`) : **tout doit être juste à 100 %**. Prendre le temps qu'il faut.
Mieux vaut une leçon de moins qu'une seule erreur.

## 0. Avant de commencer

0. **Économie de ressources** (limite d'usage de l'utilisateur) : ne pas tout relire. Lire
   `CLAUDE.md` (déjà chargé), `docs/pedagogie.md`, et dans `docs/content-format.md` seulement §2, §3,
   §7, §8 et les types d'écrans (§9) réellement utilisés. De `docs/sources.md`, lire seulement
   « Choix faits face aux supports » (dernière leçon). Le reste (mots déjà enseignés) :
   `python3 tools/deja_enseigne.py` (1 ligne par mot, remplace tous les `items.json`).
   Copier comme modèle **un** fichier d'activité du chapitre précédent plutôt que relire le format.
   Squelette du chapitre : `python3 tools/nouveau_chapitre.py <n> "<titre fr>" "<titre ar>"`.
2. Il faut **les PDF du professeur** pour la leçon. Ils ne sont pas dans le dépôt (dépôt public) :
   si l'utilisateur ne les a pas joints à la conversation, les lui demander. Ne jamais rédiger une
   leçon de mémoire sans ses supports.
3. Lire chaque page des PDF **visuellement** (outil Read avec `pages`), pas avec `pdftotext`, qui
   mélange les harakat. Noter : le vocabulaire (avec harakat exactes), les règles, les exemples, les
   exercices, les pluriels donnés.
4. Inventaire de ce qui est **déjà enseigné** : `python3 tools/deja_enseigne.py` (+ astuces des
   leçons précédentes seulement si nécessaire). Une nouvelle leçon ne peut
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

## 2 ter. Pièges rencontrés au chapitre 6 (genre et appartenance)

- **Générer le JSON par script** : `tools/generateur_contenu.py` (builders `tip`, `fill`, `sort`, `activity`, `item`… ; ids d'écran numérotés automatiquement ; mise en forme compacte comme les fichiers existants ; espaces insécables avant ? ! : ; ») évite les fautes d'id et de syntaxe. Écrire son script de chapitre hors du dépôt (dossier temporaire), avec `LURATA_CHAPITRE=07` ; l'en-tête du fichier explique l'usage. Pour relire sans ouvrir les JSON : `python3 tools/lire_chapitre.py 07 grammar-1.json …` (à donner aussi aux relecteurs).
- **Vrai/faux** : l'arabe affiché et sa traduction `fr` doivent dire la même chose ; la valeur de vérité doit venir d'une image (`context.icon`) ou d'une règle enseignée, jamais du « bon sens ».
- **Phrase fausse à corriger** : le validateur refuse un démonstratif de mauvais genre devant un nom (هٰذَا سَيَّارَةٌ). Poser la correction en `fill_gap` (le trou est le démonstratif) ou en `question` en français, jamais en choix d'une phrase fausse.
- **Règles absolues** : « tout nom en ة est féminin », « ce qui ne va pas par paire est masculin », « le pluriel en -ūna = hommes », « l'adjectif prend ة » ont toutes des exceptions (حَمْزَةُ, سِنٌّ, groupes mixtes, أَحْمَرُ). Écrire « en général » et nommer l'exception quand elle est enseignée.
- **Compter ce qu'on a vu** : « trois sortes de particules » est faux si le cours en a déjà montré d'autres (وَ, نَعَمْ, لَا) : écrire « parmi… ».
- **Mots écrits pareil sans harakat** (رَجُلٌ / رِجْلٌ) : le contrôle هٰذِهِ / هٰذَا ignore les formes dont les genres diffèrent.
- **Icônes** : éviter deux icônes qu'on peut prendre l'une pour l'autre (tasse de café et théière) ; vérifier chaque image rendue avant de l'ajouter.
- **Locuteurs de dialogue** : un personnage ne parle pas de lui à la 3ᵉ personne (« la montre de ʿAlī » dite par ʿAlī) ; une voix féminine (`f1`) existe.
- **Contenu du PDF douteux** : سَمَاءٌ (hamza issue d'un wāw) n'est pas un exemple d'alif du féminin ; مُوسَى est un nom étranger ; قَهْوَةٌ → قَهَوَاتٌ (la forme du PDF قَهْوَاتٌ n'est qu'une variante) ; سِكِّينٌ est masculin ou féminin. Ne pas les reprendre sans certitude.
- **Environnement** : `./gradlew -p core …` renvoie souvent 429 (Maven Central) : le relancer en boucle ; une fois les dépendances en cache, il est très rapide.

## 2 quater. Chapitre de révision (modèles : `r01`, « Révision 1 », et `r02`, « Révision 2 », placé après la leçon 23)

Quand les PDF sont surtout des **rappels** avec quelques éléments nouveaux :
- dossier `rNN`, `chapter.json` avec `"label": "Révision N"` (le numéro n'est pas contrôlé ; `number` = celui de la dernière leçon) ; l'ajouter à `level.json` à la place voulue ;
- **aucun doublon de cartes** : les rappels sont des activités de type `review`, **sans écran `present`** (donc aucune carte de vocabulaire, aucun point de grammaire) et n'emploient que des éléments déjà enseignés ; ne jamais redéfinir un mot déjà présent (`python3 tools/deja_enseigne.py`) — un mot du PDF déjà enseigné est simplement réutilisé ;
- seuls les éléments **nouveaux** passent par `present` (un nouvel id, jamais un id existant : `w:rajaa-2` pour `رَاجَعَ` car `w:rajaa` est `رَجَعَ`) ; une notion de grammaire nouvelle = une activité `grammar` ;
- script du chapitre : définir `g.CH = "medine-t1.rNN"` et `g.OUT` après `import generateur_contenu as g` (le générateur suppose `lNN` par défaut) ; lire avec `python3 tools/lire_chapitre.py r01 …` ;
- le chapitre `r02` ajoute un point de grammaire (`grammar-1`, avec deux nouveaux termes), six rappels `review` (`revue-1` à `revue-6`), onze activités de vocabulaire (mots du PDF, avec tableaux des quatre formes des verbes en aperçu) et cinq textes en `dialogue` ; avant d'ajouter un mot, vérifier qu'aucun élément existant n'a les mêmes lettres (le validateur ne voit que les ids : `w:imam` existait déjà au chapitre 2) ;
- les textes du professeur qui contiennent des formes pas encore enseignées (suffixes, conjugaisons, complément d'objet, adjectif épithète) se reprennent en **dialogue** avec une astuce « Des petites formes à repérer » (aperçu, rien n'est exigé en exercice) ; ne créer ni `grammar` ni exercice sur ces formes avant la leçon qui les enseigne.

## 3. Vérifications automatiques

```
./gradlew -p core test validateContent
python3 tools/preview_content.py app/src/main/assets/content/ar <scratchpad>/apercu.html
```
Le validateur doit dire `OK`. Lire l'aperçu HTML en entier, écran par écran, comme un élève.

Ajouter : `python3 tools/controle_qualite.py` (typographie française, nombre d'écrans, bilan en
dernier, voyelles, translit) : doit afficher « Aucun avertissement ».

## Socle de qualité (non négociable, même si l'usage est limité)

Économiser sur la lecture, **jamais** sur ceci : (1) lire les PDF visuellement, page par page ;
(2) validateur + `controle_qualite.py` verts ; (3) relecture indépendante par 3 relecteurs
(arabe / exercices / français-pédagogie) avant tout push ; (4) vérifier soi-même chaque remarque ;
(5) relire l'aperçu HTML ; (6) chercher les explications d'oral ; (7) mettre à jour `docs/sources.md`.
Si la limite d'usage menace : s'arrêter proprement (leçon partielle non publiée) plutôt que de
sauter une de ces étapes, et prévenir l'utilisateur.

## 4. Relecture indépendante (obligatoire)

Lancer **en parallèle** trois sous-agents (pas plus : chacun coûte cher ; les 4 du chapitre 4 n'ont pas apporté plus que 3) `relecteur-arabe` (voir `.claude/agents/` ; si ce
type d'agent n'est pas proposé, lancer un agent général en lui demandant de lire et d'appliquer
`.claude/agents/relecteur-arabe.md`), avec des angles différents :
1. **Arabe** : chaque mot lettre par lettre, cas, accords, pluriels, genres, fidélité aux PDF
   (donner le chemin des PDF).
2. **Exercices** : chaque bonne réponse est juste, aucun distracteur n'est aussi juste, toutes les
   réponses justes sont acceptées, rien n'exige une notion pas encore vue (ordre des sections).
3. **Français et pédagogie** : traductions exactes, explications vraies, explications d'oral
   manquantes et certaines.

Leur demander un rapport **court** (erreurs certaines seulement, pas de recopie du contenu). La 2e relecture se limite au `git diff` des corrections (un seul agent).

Puis **vérifier soi-même chaque remarque** (les relecteurs se trompent aussi) et ne corriger que
ce qui est confirmé. Relancer une relecture sur le diff des corrections.

## 5. Publication

1. Mettre à jour `docs/sources.md` (notions enseignées, choix faits face au PDF, points reportés).
2. Commit en français, clair. Push sur la branche de la conversation.
3. Suivre la CI GitHub jusqu'au bout ; corriger si elle échoue.
4. Donner à l'utilisateur, en français : ce qui a été ajouté, les choix faits (et pourquoi), ce
   qui reste incertain, et le lien direct
   `https://github.com/Leroy-JG/language-learning-app/releases/download/v0.1.<n>/loura.apk`.
5. Proposer de fusionner la branche dans `main` (pull request) pour que la prochaine conversation
   parte de ce travail.
