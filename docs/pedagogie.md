# Guide de conception des leçons

Ce guide donne **un cadre fixe** (ce qui ne change jamais, pour que toute l'app reste cohérente)
et **une palette d'outils** dans laquelle on choisit librement, leçon par leçon, la meilleure façon
de présenter chaque notion. Si aucun outil ne convient, la partie 4 explique comment en créer un
nouveau sans casser la cohérence.

Le format technique de chaque outil est dans `docs/content-format.md` ; la procédure complète
d'ajout d'une leçon dans `.claude/skills/nouvelle-lecon/SKILL.md`. La règle n° 1 (`CLAUDE.md`)
prime sur tout : un bel exercice faux est pire que pas d'exercice.

---

## 1. Le cadre fixe

**Structure** : niveau → chapitre (une leçon du livre) → parties → activités → écrans. Le dernier
élément d'un chapitre est toujours le **Bilan du chapitre** (`quiz`).

**Rythme d'une activité** (8 à 16 écrans) :
1. On **présente** avant d'exercer : un mot apparaît dans un `present` avant tout exercice qui
   l'utilise ; une règle apparaît dans un `tip` avant d'être exigée.
2. On **alterne** : jamais plus de 3 écrans de présentation d'affilée, jamais 3 fois de suite le
   même type d'exercice.
3. La difficulté **monte** : reconnaître (`mcq`, `match`, `memory`, `sort`) → comprendre
   (`question`, `true_false`, `spot`) → produire (`fill_gap`, `order`, `build`, `typing`).
4. On **termine** sur un exercice de production ou un jeu (pas sur une astuce).

**Textes** :
- français : tutoiement, phrases courtes, typographie française ;
- arabe : entièrement vocalisé ;
- titres d'activité : courts et concrets (« Le corps », « Lune ou soleil ? ») ;
- consignes : la consigne par défaut du type suffit en général ; une consigne propre seulement si elle aide vraiment.

**Interface (pour tout nouvel écran)** :
- **Composants communs :**
  - `ChoiceCard`, `TokenChip` et `MatchTile`, avec les états `ChoiceState` ;
  - `ArabicText` (police Naskh, interligne large) et `RichText` (français balisé) ;
  - `SpeakerButton`, `ContentIcon`.
- **Couleurs du thème uniquement :**
  - vert = juste ;
  - rouge = faux ;
  - ambre = remarque ;
  - couleur principale = sélection et accents.
- **Pas de couleur sur une partie d'un mot arabe** : cela casse la liaison des lettres. Pour montrer les morceaux d'un mot, on les sépare (bloc `breakdown`).
- **Retour à chaque geste :**
  - son et vibration (`SoundEffects`, `rememberHaptics`) ;
  - rebond quand c'est juste, tremblement quand c'est faux (`answerAnimation`) ;
  - le panneau de correction en bas.
- **Arabe de droite à gauche** dans les rangées d'étiquettes (`RtlFlow`).
- **Accessibilité :**
  - zones à toucher d'au moins 48 dp ;
  - lisible en thème sombre et sur un petit téléphone.

**Encadrés** :
- `note` neutre pour un complément ;
- `warning` pour un piège ou une exception ;
- `memo` pour un moyen de retenir ;
- `culture` pour le sens d'un mot, l'histoire ou la culture (toujours vérifié, jamais anecdotique au point d'être douteux).

---

## 2. La palette

### Présenter

| Outil | Idéal pour | À éviter |
|---|---|---|
| `present` | chaque nouveau mot ou phrase (genre, pluriel, note, exemple) | une règle de grammaire |
| `tip` + `paragraph` | expliquer une règle en 2 ou 3 phrases | les longs pavés (découper en plusieurs `tip`) |
| `example` | une phrase modèle à écouter | une liste de mots isolés (→ `gallery`, `table`) |
| `table` | un système (lettres lunaires, pronoms, terminaisons) | deux colonnes « avant / après » (→ `compare`) |
| `compare` | une **transformation** : indéfini → défini, singulier → pluriel, masculin → féminin, nominatif → génitif… | des formes sans lien de transformation |
| `breakdown` | un mot ou un groupe **composé** : article + nom, particule collée (وَ, بِ, لِ), nom + pronom suffixe, annexion | un mot simple |
| `gallery` | un **rappel visuel** de mots déjà vus (début de rappel, fin de thème) | des mots nouveaux (ils doivent passer par `present`) |
| `note` + `tone` | exception, piège, moyen de retenir, culture | le cœur de la règle (→ `paragraph`) |
| `dialogue` | la langue en situation, avec les mots du chapitre | du vocabulaire jamais présenté |

### Exercer

| Outil | Ce qu'il entraîne | Idéal pour |
|---|---|---|
| `mcq` (`ar-fr`, `fr-ar`, `audio-ar`, `icon-ar`) | reconnaître un mot | juste après la présentation |
| `match` (`ar-fr`, `audio-ar`, `sing-plur`) | relier des formes | 3 à 5 mots d'un même thème, les pluriels |
| `memory` | consolider en jouant | fin d'un thème de vocabulaire illustré |
| `sort` | **classer** selon une règle | lunaire / solaire, masculin / féminin, chose / personne (مَا / مَنْ), proche / loin, nom / verbe / particule |
| `question` | comprendre une règle ou un dialogue | une question de grammaire, la compréhension d'un dialogue |
| `true_false` | vérifier une affirmation | un piège fréquent, la compréhension |
| `spot` | **repérer** une notion dans une phrase | « touche le mot défini », « touche la particule », « touche la préposition » |
| `fill_gap` | choisir la bonne forme en contexte | accords, terminaisons, choix du mot outil |
| `order` | construire une phrase | ordre des mots, phrase nominale |
| `build` | **orthographe** d'un mot nouveau | mots dont l'écriture surprend (alif long, ة, lettres proches) |
| `typing` | produire de mémoire | fin d'activité, réponse à une question |

---

## 3. La liberté : choisir pour chaque leçon

Pour chaque leçon, partir des notions du PDF et se demander pour chacune : **quelle est la façon
la plus claire de la montrer, puis la plus efficace de l'entraîner ?**

1. Lister les notions (mots, règle, exception, prononciation, explication d'oral à ajouter).
2. Pour chaque notion, choisir dans la palette :
   - une transformation se montre avec `compare` ;
   - une composition se montre avec `breakdown` ;
   - une classification s'entraîne avec `sort` ;
   - une notion à reconnaître en contexte s'entraîne avec `spot` ;
   - une orthographe s'entraîne avec `build`.
3. Varier d'une leçon à l'autre : un chapitre doit contenir au moins un jeu (`memory`, `sort`, `build` ou `spot`) en plus des exercices classiques.
4. Écrire le plan du chapitre (parties, activités, outils choisis) avant le JSON.

Exemples tirés des leçons 1 à 4 :
- `sort` مَا / مَنْ dans `l01/grammar-3` ;
- `memory` des animaux dans `l01/vocab-5` ;
- `build` de مِفْتَاحٌ dans `l01/vocab-2` ;
- `gallery` dans `l02/recap` ;
- `compare` et `breakdown` de l'article dans `l03/grammar-2` ;
- `spot` de l'article dans `l03/grammar-3` ;
- `sort` lunaire / solaire dans `l03/practice-1` ;
- `compare` de la kasra après une préposition dans `l04/grammar-1` ;
- `sort` à trois catégories (nom propre / démonstratif / article) dans `l04/grammar-3` ;
- `breakdown` de مِنْ أَيْنَ dans `l04/grammar-6` ;
- `memory` des pays, avec les drapeaux, dans `l04/vocab-3` ;
- `build` de إِلَى (le ى, pas le ي) dans `l04/vocab-1` et de مَكَّةُ (le ة, pas le ه) dans `l04/grammar-5`.

### Entraîner une voyelle finale (kasra, fatha…)

Le validateur refuse deux choix qui ne diffèrent que par les harakat (`الْبَيْتِ` / `الْبَيْتُ`), et le
réglage « masquer les harakat » les rendrait identiques. Pour faire travailler une terminaison :
- montrer la transformation avec `compare` (dans une astuce, où les harakat ne sont jamais masqués) ;
- poser la question en français avec `question` (« une kasra / une damma / une fatha ») ;
- faire repérer avec `spot` (« touche le nom qui suit la préposition ») ;
- ranger avec `sort` en choisissant des cartes dont la catégorie se déduit du reste du texte (la
  présence d'une préposition) : l'exercice reste juste même avec les harakat masquées ;
- faire écrire en dictée avec `typing` (la correction tolère les harakat absentes, et signale celles
  qui sont fausses).

---

## 4. Étendre la palette (quand rien ne convient)

On crée un nouveau type **seulement** si un vrai besoin pédagogique revient (au moins deux leçons) et
qu'aucun outil existant, même combiné, ne le couvre. Sinon, on utilise la palette.

Liste de contrôle, dans l'ordre :
1. **Modèle** : `core/.../content/Model.kt`. Pour un nouvel écran, une `data class` `@SerialName` qui implémente `Exercise` ou `Screen` ; pour une nouvelle façon de présenter, un nouveau `Block`.
2. **Validateur** : `Validator.kt`, avec des règles strictes (bornes, éléments déjà présentés, réponses uniques). Ajouter le nom dans `typeName`.
3. **Audio** : ajouter chaque texte arabe qui se lit dans `Audio.needs`.
4. **Moteur** : `engine/Exercises.kt` (`PreparedX` et sa correction, préparation dans `ExerciseFactory`).
5. **Tests** :
   - un test dans `EngineTest` ;
   - le type dans « tout le cours se joue » ;
   - des mutations dans `ValidatorTest`.
6. **App** :
   - `PlayerViewModel` : état remis à zéro dans `showCurrent`, `prepare`, `canCheck`, `check` ou un geste `tapX` ;
   - un composable qui respecte le cadre de la partie 1 (fichier `ui/activity/Games.kt` pour un jeu) ;
   - l'aiguillage dans `ActivityScreen.ScreenBody` ;
   - le bouton « Vérifier » dans `BottomArea` ;
   - les chaînes dans `strings.xml`.
7. **Aperçu** : `tools/preview_content.py`.
8. **Docs** :
   - `docs/content-format.md` : le format, et ce que vérifie le validateur ;
   - ce guide : la palette.
9. Utiliser le nouveau type dans une leçon. Faire relire le contenu, vérifier que la CI passe, puis publier.

L'app ne se compile pas localement (voir `CLAUDE.md`) : relire le code Compose avec soin et
vérifier la CI après le push.
