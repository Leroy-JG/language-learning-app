# Format du contenu des cours — version 1

Ce document définit comment les cours sont décrits. L'application ne contient **aucune leçon dans son
code** : tout ce qui est enseigné vient des fichiers JSON décrits ici. Ajouter un tome ou une autre
source revient à ajouter des fichiers.

Tous les fichiers sont en UTF-8, dans `app/src/main/assets/content/`.

---

## 1. Organisation des fichiers

```
content/
  ar/                               ← un cours (langue cible)
    course.json                     ← description du cours, voix, liste des niveaux
    medine-t1/                      ← un niveau
      level.json                    ← description du niveau, liste des chapitres
      l01/                          ← un chapitre (= une leçon du tome)
        chapter.json                ← description du chapitre, parties et activités
        items.json                  ← mots et phrases introduits dans ce chapitre
        vocab-1.json                ← une activité
        grammar-1.json
        …
```

Le nom des dossiers n'a pas d'importance pour l'app : seuls les **identifiants** et les **listes
ordonnées** (`levels`, `chapters`, `sections`) comptent. Le fichier d'une activité est
`<dossier du chapitre>/<code de l'activité>.json` (voir §2).

---

## 2. Identifiants (règle absolue)

La progression est enregistrée par identifiant. Un identifiant ne doit donc **jamais être renommé
ni réutilisé** pour autre chose, même après suppression. Corriger un texte, une traduction ou un
exercice est toujours possible : seul l'id reste figé.

| Élément | Forme | Exemple |
|---|---|---|
| Cours | `<langue>` | `ar` |
| Niveau | `<code>` | `medine-t1` |
| Chapitre | `<niveau>.<code>` | `medine-t1.l01` |
| Activité | `<chapitre>.<code>` | `medine-t1.l01.vocab-1` (fichier `l01/vocab-1.json`) |
| Écran | `<activité>#<code>` | `medine-t1.l01.vocab-1#3` |
| Élément (mot) | `w:<code>` | `w:kitab` |
| Élément (phrase) | `p:<code>` | `p:ma-hadha` |

- Caractères autorisés dans un code : `a-z`, `0-9`, `-`.
- Les ids d'éléments (`w:` / `p:`) sont **uniques dans tout le cours** : un mot vu au Tome 1 et
  réutilisé au Tome 3 garde le même id et n'est défini qu'une fois, dans le chapitre qui l'introduit.
- Le code d'un mot est sa translittération simplifiée (`kitab`, `miftah`). En cas d'homographes, on
  suffixe : `w:kalb`, `w:kalb-2`.
- Les codes d'écrans d'une activité sont libres mais uniques ; en pratique on les numérote.
  Insérer un écran au milieu d'une activité existante : lui donner un nouveau code (ex. `#3b`),
  sans renuméroter les autres.

---

## 3. Règles sur le texte arabe

**Règles de rédaction**
- Tout texte arabe destiné à être lu ou écouté est **entièrement vocalisé** (harakat, shadda,
  tanwin, sukun, alif suscrit ٰ). Masquer les harakat est un réglage d'affichage : on n'écrit jamais
  deux versions d'un texte. Seule exception : une astuce de grammaire peut montrer un mot non vocalisé
  quand c'est le sujet de l'explication (ex. « {ar|هذا} s'écrit sans alif »).
- Ponctuation arabe : `؟` `،` `؛` et le point `.`. Pas de `?` ni de `,` latins dans un texte arabe.
- Pour montrer un signe seul (tanwin, shadda…), on le pose sur le cercle pointillé `◌` (U+25CC) :
  `{ar|◌ٌ}`. C'est le seul support admis pour un signe isolé.
- Article : devant une lettre lunaire, le lām porte un sukūn (`الْكِتَابُ`) ; devant une lettre
  solaire, le lām reste nu et la lettre solaire porte la shadda (`الشَّمْسُ`). L'alif de l'article
  reste nu, sauf dans une astuce qui montre sa prononciation en début de phrase (`اَلْكِتَابُ`).

**Masquer les harakat (réglage d'affichage)**
- S'applique aux mots, aux phrases, aux exercices et aux dialogues.
- Ne s'applique **jamais** aux astuces (`tip`), qui expliquent justement ces signes, ni à un
  passage contenant `◌`.
- Conséquence pour la rédaction : dans un même exercice, deux choix, deux options ou deux étiquettes
  ne doivent jamais différer **seulement** par les harakat (`الْبَيْتُ` / `الْبَيْتٌ`) : une fois
  les harakat masquées, ils seraient identiques.

**Ce que les outils font automatiquement**
- Normalisation Unicode (NFC) au chargement : l'ordre de saisie des signes (shadda puis tanwin, ou
  l'inverse) est sans importance.

**Interdits (le build échoue)**
- formes de présentation arabes (U+FB50–U+FDFF, U+FE70–U+FEFF) ;
- lettres persanes ou ourdoues ressemblantes (`ی` `ک` `ە` `ہ` `ۀ`…) ;
- tatweel `ـ` ;
- caractères invisibles (U+200B–U+200F, U+202A–U+202E, U+2066–U+2069, U+FEFF) ;
- `?` ou `,` latins dans un texte arabe ;
- signe de vocalisation sans lettre (ou sans `◌`) avant lui.

**Texte français contenant de l'arabe**
- Dans tout texte français (traductions, consignes, astuces, titres), un passage arabe est balisé :
  `{ar|كِتَابٌ}`. Il s'affiche alors dans la police arabe et dans le bon sens, sans désordonner
  la phrase française.
- Seule autre mise en forme autorisée : `**gras**`. Pas de HTML.

---

## 4. `course.json`

```json
{
  "formatVersion": 1,
  "id": "ar",
  "title": "Arabe",
  "voices": {
    "m1": { "provider": "edge", "name": "ar-SA-HamedNeural", "rate": "-10%" },
    "f1": { "provider": "edge", "name": "ar-SA-ZariyahNeural", "rate": "-10%" }
  },
  "defaultVoice": "m1",
  "levels": ["medine-t1"]
}
```

- `formatVersion` : version de ce format. L'app refuse un contenu d'une version qu'elle ne connaît pas.
- `voices` : les voix utilisables, désignées partout ailleurs par leur clé (`m1`, `f1`…).
  `rate` est la vitesse de génération (la vitesse de lecture se règle en plus dans l'app).
- `levels` : ordre des niveaux ; chaque entrée est le nom d'un dossier contenant `level.json`.

## 5. `level.json`

```json
{
  "id": "medine-t1",
  "title": "Tome de Médine 1",
  "description": "Les bases : démonstratifs, questions, article, prépositions…",
  "source": "Durūs al-lugha al-ʿarabiyya li-ghayr al-nāṭiqīna bihā, tome 1",
  "chapters": ["l01", "l02", "l03"]
}
```

`chapters` : ordre des chapitres ; chaque entrée est un sous-dossier contenant `chapter.json`.

## 6. `chapter.json`

```json
{
  "id": "medine-t1.l01",
  "number": 1,
  "title": { "ar": "الدَّرْسُ الْأَوَّلُ", "fr": "Ceci est… : le démonstratif et les questions" },
  "description": "Montrer un objet proche, demander ce que c'est ou qui c'est, répondre par oui ou non.",
  "source": "Tome de Médine 1, leçon 1",
  "sections": [
    { "title": "Le démonstratif {ar|هٰذَا}", "activities": ["intro", "vocab-1", "grammar-1"] },
    { "title": "Bilan", "activities": ["quiz"] }
  ]
}
```

- Un chapitre est découpé en **parties** (`sections`), chacune contenant des activités.
- Rien n'est verrouillé : chaque activité s'ouvre quand on veut. L'ordre (parties puis activités)
  est celui du parcours conseillé ; l'accueil propose la première activité non terminée.

---

## 7. `items.json` : mots et phrases

```json
[
  {
    "id": "w:kitab",
    "kind": "word",
    "ar": "كِتَابٌ",
    "fr": "un livre",
    "translit": "kitābun",
    "plural": "كُتُبٌ",
    "icon": "book",
    "gender": "m",
    "note": "La double damma finale ({ar|◌ٌ}, le tanwin) correspond à « un ».",
    "example": { "ar": "هٰذَا كِتَابٌ.", "fr": "Ceci est un livre." }
  }
]
```

| Champ | Obligatoire | Rôle |
|---|---|---|
| `id` | oui | `w:` pour un mot, `p:` pour une phrase |
| `kind` | oui | `word` ou `phrase` |
| `ar` | oui | texte arabe vocalisé |
| `fr` | oui | traduction affichée ; unique parmi les éléments d'un même chapitre (sinon un QCM pourrait avoir deux bonnes réponses) |
| `translit` | non | translittération écrite à la main (jamais générée) |
| `plural` | non | pluriel (texte arabe vocalisé, avec son audio) |
| `icon` | non | nom d'une icône de l'app (permet les exercices « image → mot ») |
| `gender` | non | `m` ou `f`, affiché sous la traduction (« masculin · مُذَكَّرٌ »). Obligatoire pour les noms ; absent pour les particules, adjectifs et phrases |
| `note` | non | remarque en français affichée sous le mot |
| `example` | non | phrase d'exemple `{ ar, fr }`, avec son audio |
| `voice` | non | clé de voix, sinon `defaultVoice` |
| `tts` | non | texte envoyé à la synthèse vocale à la place de `ar`, pour corriger une mauvaise prononciation |
| `audioFile` | non | enregistrement manuel (`audio/manual/<nom>.mp3`) qui remplace la synthèse de `ar` |
| `review` | non | `false` pour exclure l'élément de la révision (défaut `true`) |

Un élément entre dans **ta mémoire de révision** (onglet Réviser) quand tu termines une activité
dont un écran `present` te l'a montré (voir §12).

---

## 8. Fichier d'activité

```json
{
  "id": "medine-t1.l01.vocab-1",
  "type": "vocabulary",
  "title": "À la maison et à la mosquée",
  "icon": "mosque",
  "screens": [ … ]
}
```

- `type` : `vocabulary`, `grammar`, `dialogue`, `review` ou `quiz`. Il détermine la couleur de
  l'activité ; le contenu est libre (une activité de vocabulaire peut contenir une astuce).
- `icon` (facultatif) : image de l'activité dans le parcours de l'accueil ; sinon le symbole de
  son type. Une activité `quiz` s'affiche toujours comme la carte « Bilan du chapitre ».
- Dans l'app, un chapitre s'appelle « Chapitre N » et chacune de ses activités est une « leçon »
  du parcours. Toutes les leçons sont ouvertes ; la leçon proposée est la première non terminée.
- Une activité `grammar` est un **point de grammaire** dans l'onglet Réviser : ses exercices
  servent aux révisions de grammaire, et ses astuces (`tip`) de rappel de la règle.
- `screens` : la suite d'écrans. Chaque écran a un `id` (`<activité>#<code>`) et un `type`.

**Déroulé** : un exercice raté revient en fin d'activité jusqu'à ce qu'il soit réussi. Le score
compte les réussites du premier coup. Un `quiz` est réussi à partir de **70 %** ; sinon on le recommence.

**Champs communs à tous les exercices** (tous les types sauf `present`, `tip` et `dialogue`)

| Champ | Rôle |
|---|---|
| `instruction` | consigne en français ; sinon une consigne par défaut selon le type |
| `context` | support affiché au-dessus de la question : `{ "icon": … }`, `{ "item": … }` ou `{ "dialogue": "<id d'écran>" }` (bouton « réécouter ») |
| `explain` | explication en français affichée dans le panneau de correction |

---

## 9. Types d'écrans

### 9.1 `present` : présentation d'un mot ou d'une phrase
```json
{ "id": "…#1", "type": "present", "item": "w:kitab" }
```
Affiche l'arabe, l'audio (lu automatiquement selon le réglage), la traduction, l'icône, la
translittération (selon le réglage), le pluriel, la note et l'exemple. Ajoute l'élément à la banque de mots.

### 9.2 `tip` : astuce de grammaire
```json
{
  "id": "…#1", "type": "tip",
  "title": "Ceci est…",
  "blocks": [
    { "type": "paragraph", "text": "{ar|هٰذَا} veut dire « ceci »." },
    { "type": "example", "ar": "هٰذَا بَيْتٌ.", "fr": "Ceci est une maison." },
    { "type": "table", "header": ["Arabe", "Français"], "rows": [["{ar|هٰذَا}", "ceci"]] },
    { "type": "note", "text": "En arabe, il n'y a pas de verbe « être » au présent." }
  ]
}
```
Blocs : `paragraph`, `example` (écoutable), `table` (cellules au format « texte français »),
`note` (encadré). Toutes les lignes d'un tableau ont autant de cellules que l'en-tête.

### 9.3 `mcq` : QCM sur un élément
```json
{ "id": "…#2", "type": "mcq", "mode": "ar-fr", "item": "w:kitab", "distractors": ["w:bab", "w:bayt"] }
```
| `mode` | La question montre | Les choix sont |
|---|---|---|
| `ar-fr` | l'arabe (+ audio) | des traductions françaises |
| `fr-ar` | le français | des textes arabes |
| `audio-ar` | uniquement l'audio | des textes arabes |
| `icon-ar` | l'icône | des textes arabes (l'élément doit avoir une `icon`) |

`distractors` (2 ou 3 éléments déjà présentés) est facultatif. Sinon l'app en choisit 2 parmi les
éléments déjà présentés **du même type** (`word` / `phrase`), en priorité dans la même activité, puis
le même chapitre, puis le même niveau, sans doublon de traduction ni de texte arabe. Les choix sont
mélangés à chaque fois.

### 9.4 `match` : associer les paires
```json
{ "id": "…#3", "type": "match", "mode": "ar-fr", "items": ["w:bayt", "w:bab", "w:kitab", "w:masjid"] }
```
3 à 5 éléments déjà présentés. `mode` :
- `ar-fr` : arabe ↔ français ;
- `audio-ar` : audio ↔ arabe ;
- `sing-plur` : singulier ↔ pluriel (chaque élément doit avoir un `plural`).

### 9.5 `fill_gap` : texte à trous
```json
{
  "id": "…#4", "type": "fill_gap",
  "sentence": "هٰذَا {0}.",
  "fr": "Ceci est une porte.",
  "gaps": [ { "answers": ["بَابٌ"] } ],
  "options": ["بَابٌ", "بَيْتٌ", "كِتَابٌ"]
}
```
- `{0}`, `{1}`… marquent les trous, dans l'ordre de `gaps`, chacun une seule fois.
- Chaque trou a une ou plusieurs `answers` acceptées, qui doivent toutes figurer dans `options`.
- La phrase complète (premier choix de chaque trou) est lue après la correction.

### 9.6 `order` : remettre les mots dans l'ordre
```json
{
  "id": "…#5", "type": "order",
  "fr": "Est-ce une maison ?",
  "answers": [ ["أَهٰذَا", "بَيْتٌ"] ],
  "punctuation": "؟",
  "distractors": ["مَا"]
}
```
- Les étiquettes sont **découpées à la main** : une particule collée reste collée (`أَهٰذَا`), une
  ponctuation intérieure reste sur son mot (`لَا،`).
- `answers` : une ou plusieurs suites valides, qui utilisent toutes exactement les mêmes étiquettes.
- `punctuation` (facultatif) est ajoutée automatiquement à la fin, ce n'est pas une étiquette.
- Les étiquettes s'affichent et se placent de droite à gauche.

### 9.7 `typing` : écrire en arabe
```json
{ "id": "…#6", "type": "typing", "ask": "fr", "item": "w:bayt" }
{ "id": "…#7", "type": "typing", "ask": "audio", "fr": "Ceci est une maison.", "answers": ["هٰذَا بَيْتٌ."] }
{ "id": "…#8", "type": "typing", "ask": "question", "question": "مَا هٰذَا؟", "context": { "icon": "key" }, "answers": ["هٰذَا مِفْتَاحٌ."] }
```
- La réponse attendue est soit le texte arabe de `item`, soit la liste `answers` (avec `fr`, la
  traduction montrée après la correction).
- `ask` : ce qu'on te montre pour te faire écrire :
  - `fr` : la traduction française ;
  - `audio` : une dictée (on entend la première réponse) ;
  - `question` : une question en arabe (`question`, avec son audio), à laquelle il faut répondre.
- Correction tolérante :
  - **correct** si identique en ignorant les harakat, l'alif suscrit, la ponctuation et les espaces
    en trop ;
  - **correct avec remarque** si la seule différence porte sur la hamza ou l'alif (أ إ آ ا),
    ة/ه, ى/ي, ou si des harakat ont été saisies et sont fausses ;
  - **faux** sinon.

### 9.8 `true_false` : vrai ou faux
```json
{
  "id": "…#9", "type": "true_false",
  "context": { "icon": "book" },
  "statement": { "ar": "هٰذَا بَيْتٌ.", "fr": "Ceci est une maison." },
  "answer": false
}
```
- `statement` est soit `{ ar, fr }` (l'arabe est affiché et lu ; la traduction n'apparaît
  qu'après la réponse), soit `{ fr }` seul (affirmation en français, ex. sur la grammaire).

### 9.9 `question` : question à choix (compréhension, grammaire)
```json
{
  "id": "…#10", "type": "question",
  "context": { "dialogue": "medine-t1.l01.dialogue-1#1" },
  "prompt": { "fr": "Qu'est-ce que Ḥāmid montre en premier ?" },
  "choices": [ { "fr": "Une maison" }, { "fr": "Une mosquée" }, { "fr": "Une porte" } ],
  "answer": 0
}
```
- `prompt` et chaque choix sont `{ "fr": … }` ou `{ "ar": … }`. 2 à 4 choix, tous différents.
- `answer` est l'index du bon choix **dans le fichier** ; l'app mélange les choix à l'affichage
  (sauf `"shuffle": false`, par exemple pour garder « oui » avant « non »).

### 9.10 `dialogue` : conversation audio
```json
{
  "id": "…#1", "type": "dialogue",
  "title": "Qu'est-ce que c'est ?",
  "intro": "Yāsir découvre le quartier de Ḥāmid.",
  "speakers": {
    "A": { "name": "Yāsir", "voice": "m2" },
    "B": { "name": "Ḥāmid", "voice": "m1" }
  },
  "lines": [
    { "speaker": "A", "ar": "مَا هٰذَا؟", "fr": "Qu'est-ce que c'est ?" }
  ]
}
```
Une ligne = un audio. Pendant la lecture, la ligne en cours est surlignée ; on peut rejouer une
ligne en la touchant et afficher ou masquer les traductions.

---

## 10. Audio

Aucun nom de fichier audio n'est écrit dans le contenu. Chaque texte arabe qui se lit a un audio :
élément (`ar`), pluriel, exemple, bloc `example`, phrase complète d'un `fill_gap` ou d'un `order`,
réponse dictée et question d'un `typing`, énoncé arabe d'un `true_false`, question et choix arabes
d'un `question`, ligne de dialogue. Pour chacun, l'app et le script calculent :

```
empreinte = sha256( "<provider>|<name>|<rate>|<texte NFC envoyé à la synthèse>" ) → 20 premiers caractères hexadécimaux
fichier   = assets/audio/<empreinte>.mp3
```

Le texte envoyé est `tts` s'il est défini (uniquement pour le `ar` d'un élément), sinon le texte arabe.
La voix est celle du locuteur (dialogue), de l'élément (`voice`), sinon `defaultVoice`.
Conséquences :
- modifier un texte ou une voix change l'empreinte : le script génère le nouveau fichier et signale
  l'ancien comme inutilisé ;
- un même texte avec la même voix n'est généré qu'une fois, même s'il apparaît dans dix exercices ;
- `audioFile` (enregistrement manuel) court-circuite tout ce mécanisme pour l'élément concerné.

---

## 11. Ce que le validateur vérifie (le build échoue sinon)

- `formatVersion` connu ; JSON conforme à ce document (champs obligatoires, types, valeurs permises,
  aucun champ inconnu — une faute de frappe dans un nom de champ est une erreur, pas un oubli silencieux).
- Identifiants bien formés, uniques, et cohérents avec leur parent et leur fichier.
- Toutes les références existent (éléments, voix, icônes, écrans de dialogue).
- Un exercice n'utilise que des éléments **déjà présentés** (dans la même activité avant lui, ou dans
  une activité ou un chapitre précédent) ; il en va de même pour les distracteurs.
- Traductions `fr` uniques dans un chapitre ; distracteurs différents de la bonne réponse.
- `mcq` : 2 ou 3 distracteurs ; en mode `icon-ar`, l'élément a une icône ; sans distracteurs
  explicites, au moins 2 candidats disponibles.
- `match` : 3 à 5 éléments ; en mode `sing-plur`, tous ont un pluriel, sans doublon.
- `fill_gap` : chaque `{n}` présent une fois et correspondant à `gaps` ; chaque réponse dans `options` ;
  options différentes même sans harakat.
- `order` : toutes les `answers` utilisent exactement les mêmes étiquettes ; étiquettes et
  distracteurs différents même sans harakat (ou strictement identiques, donc interchangeables).
- `typing` : `item` ou `answers` (pas les deux) ; `question` présent si et seulement si `ask` vaut `question`.
- `question` : 2 à 4 choix différents même sans harakat, `answer` dans les bornes.
- Article vocalisé selon la règle lunaire / solaire (§3).
- Pas de démonstratif masculin (`هٰذَا`, `ذٰلِكَ`) directement suivi d'un nom marqué féminin
  (`gender: "f"`) : les démonstratifs féminins seront enseignés plus tard.
- `context.dialogue` désigne un écran `dialogue` déjà vu.
- Règles du texte arabe (§3) et balises `{ar|…}` bien formées.
- Chaque audio attendu existe dans `assets/audio/`.

---

## 12. Révisions (onglet Réviser)

Les révisions utilisent la répétition espacée **FSRS** (l'algorithme d'Anki) : chaque mot et chaque
point de grammaire a une stabilité (en jours) et une difficulté ; la prochaine révision tombe quand
la probabilité de s'en souvenir descend à 90 %.

- **Entrée en mémoire** : à la fin d'une activité, chaque élément présenté (`present`, avec
  `review` ≠ `false`) devient une carte ; une activité `grammar` devient un point de grammaire.
- **Notes** (jamais demandées à l'utilisateur) : réponse fausse au premier essai → « oublié » ;
  juste avec une remarque → « difficile » ; juste → « bien ». Un point de grammaire est noté selon la
  part de réponses justes du premier coup (≥ 85 % bien, ≥ 60 % difficile, sinon oublié).
- **Séance de vocabulaire** : les cartes dues d'abord (les plus effacées en premier), puis les plus
  fragiles. Exercice selon la solidité du souvenir : reconnaître (arabe → français) quand il est
  fragile, retrouver l'arabe (français → arabe, écoute, image) ensuite, l'écrire quand il est solide.
  Les distracteurs viennent du même chapitre puis du même niveau, sans doublon de traduction ni de
  texte arabe (même sans harakat).
- **Séance de grammaire** : exercices de la leçon du point, mélangés, précédés du rappel de la règle
  (ses astuces) quand le point est fragile ou jamais révisé.
- **Maîtrise affichée** : à travailler (oubli récent, souvenir très fragile ou déjà effacé), à revoir,
  acquis (souvenir solide au moins trois semaines et encore frais).
