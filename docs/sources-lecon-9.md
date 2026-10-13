# Leçon 9 : sources et choix de contenu

> Ce fichier est séparé de `docs/sources.md` **pour éviter les conflits de fusion** : les leçons 7, 8 et 9
> ont été écrites en parallèle, dans trois conversations différentes. À la fusion, son contenu peut être
> copié dans `docs/sources.md` (sections « Ce qui est déjà enseigné » et « Choix faits face aux supports »).

## Support utilisé

- `TOME1-9.pdf` : cours 9 en entier (النَّعْتُ, l'adjectif ; مَمْنُوعٌ مِنَ الصَّرْفِ pour les adjectifs en
  ـان ; trois pages du livre ; exercices 1 à 4 ; vocabulaire nouveau). Supports lus page par page.

## Ce qui est enseigné (leçon 9)

- النَّعْتُ (l'adjectif épithète) et الْمَنْعُوتُ (le nom qualifié) : l'adjectif se place après le nom, il
  s'accorde en genre, en nombre (singulier seulement dans ce chapitre), en défini / indéfini et en cas ;
  il n'est pas indispensable à la phrase.
- Groupe de mots ou phrase : الْكِتَابُ الْجَدِيدُ (deux articles : groupe) / الْكِتَابُ جَدِيدٌ (un seul article :
  phrase) ; avec هٰذَا : هٰذَا بَيْتٌ كَبِيرٌ / هٰذَا الْبَيْتُ كَبِيرٌ / هٰذَا الْبَيْتُ الْكَبِيرُ.
- L'adjectif d'un nom propre prend l'article (زَيْدٌ الطَّوِيلُ) ; accord de l'adjectif en annexion
  (اِبْنُ الْمُدِيرِ الْجَدِيدِ / الْجَدِيدُ).
- Adjectifs d'état de forme فَعْلَانُ : privés de tanwin (كَسْلَانُ, جَوْعَانُ, عَطْشَانُ, غَضْبَانُ, مَلْآنُ), fatha
  à la place de la kasra sans article ni annexion, féminin en ـَى.
- Mots : النَّعْتُ, الْمَنْعُوتُ, كَبِيرٌ, صَغِيرٌ, طَوِيلٌ, قَصِيرٌ, شَهِيرٌ, لَذِيذٌ, سَهْلٌ, صَعْبٌ, مُجْتَهِدٌ, حَادٌّ,
  جَوْعَانُ, عَطْشَانُ, غَضْبَانُ, كَسْلَانُ, مَلْآنُ, لُغَةٌ, الْعَرَبِيَّةُ, الْإِنْكِلِيزِيَّةُ, الْفَرَنْسِيَّةُ, فَاكِهَةٌ,
  تُفَّاحٌ, طَائِرٌ, عُصْفُورٌ, يَوْمٌ, هُنَا, عِنْدَ, الثَّانَوِيَّةُ, مَدِينَةٌ, مُسْتَوْصَفٌ, مِرْوَحَةٌ, كُوبٌ, لُعْبَةٌ
  (liste complète, avec ids : `python3 tools/deja_enseigne.py`).

## Écriture en parallèle des leçons 7, 8 et 9

La leçon 9 a été écrite sans connaître le contenu des leçons 7 et 8 : elle ne s'appuie que sur les
chapitres 1 à 6. Conséquences :
- **Mots que le PDF suppose connus et qui ne sont pas dans son vocabulaire** : كَبِيرٌ, صَغِيرٌ, طَوِيلٌ,
  قَصِيرٌ, لُغَةٌ, الْعَرَبِيَّةُ, الْفَرَنْسِيَّةُ, مَدِينَةٌ, لَذِيذٌ (les pages du livre et les exercices les
  emploient). Le chapitre 9 présente lui-même ceux que les chapitres 1 à 7 n'ont pas déjà présentés.
- **Fusion avec la leçon 7 (faite)** : طَوِيلٌ et قَصِيرٌ étaient aussi présentés au chapitre 7 (avec leurs
  pluriels طِوَالٌ et قِصَارٌ) : le chapitre 9 ne les redéfinit plus. Dans `vocab-1`, leurs écrans de
  présentation (#4 et #5) et le jeu d'association #6 ont été retirés (ids jamais réutilisés), et deux écrans
  ont été ajoutés (#3b : كَبِيرٌ ou طَوِيلٌ ? ; #6b : association sans طَوِيلٌ ni قَصِيرٌ, car « grand, gros »
  et « grand (de taille), long » seraient ambigus dans un même exercice). La phrase du livre
  لِمَنْ تِلْكَ السَّيَّارَةُ الْجَمِيلَةُ ؟ est rétablie dans `dialogue-2`, تِلْكَ étant enseigné au chapitre 7.
- **Fusion avec la leçon 8 (faite)** : aucun doublon d'id. Le chapitre 8 enseigne الْمُبْتَدَأُ et الْخَبَرُ
  (le chapitre 9 renvoie à ces termes), جَلَسَ, خَلْفَ, سِكِّينٌ et مُسْتَشْفًى : trois phrases du livre
  écartées au départ sont rétablies (`practice-5` #13 et #14 : الطَّبِيبُ الْجَدِيدُ فِي الْمُسْتَشْفَى…,
  السِّكِّينُ الْكَبِيرُ حَادٌّ جِدًّا ; `practice-6` #15 : جَلَسَ الطَّالِبُ الْجَدِيدُ خَلْفَ حَامِدٍ), et حَادٌّ
  reprend son exemple du PDF (السِّكِّينُ حَادٌّ).
- **(Avant la fusion avec la leçon 8)** Si la leçon 8 présentait le même mot, il y aura un doublon d'id** (`w:…` est unique dans tout le
  cours) : à la fusion, garder la définition du chapitre le plus ancien, supprimer l'autre dans
  `items.json` du chapitre 9, et vérifier que la présentation (`present`) du mot reste avant son
  premier emploi (`./gradlew -p core validateContent`).
- `level.json` : `l09` est ajouté à la fin ; quand la leçon 8 arrivera, l'ordre doit être `l07`, `l08`, `l09`.

## Choix faits face au PDF

- **Phrases du livre reprises** : page « أ » → `practice-3` ; page « ب » → `dialogue-1` ; page « ج »
  (العربية لغة جميلة…) → `dialogue-3` ; page de la leçon (لِمَنْ … الْجَمِيلَةُ) → `dialogue-2` ;
  exercice 1 → `practice-5` ; exercices 2 et 3 → `practice-6` ; exercice 4 → `practice-4`.
- **Phrases adaptées** (mot non enseigné remplacé) :
  - لِمَنْ تِلْكَ السَّيَّارَةُ… : conservé dans `dialogue-2` (تِلْكَ est enseigné au chapitre 7) ; l'exercice `order` de la même activité emploie هٰذِهِ ;
  - مَنْ ذٰلِكَ الرَّجُلُ الطَّوِيلُ الَّذِي خَرَجَ… : la proposition relative (الَّذِي) est supprimée ;
  - ذَهَبَ إِلَى الْمَكْتَبَةِ → ذَهَبَ إِلَى الْبَيْتِ (مَكْتَبَةٌ non enseigné) ;
  - لِمَاذَا الْمُدَرِّسُ غَضْبَانُ الْيَوْمَ ؟ → الْمُدَرِّسُ غَضْبَانُ. (لِمَاذَا et الْيَوْمَ, accusatif, non
    enseignés) ;
  - الْقَاهِرَةُ مَدِينَةٌ كَبِيرَةٌ → مَكَّةُ مَدِينَةٌ كَبِيرَةٌ (pas de nouveau nom propre) ;
  - exercice 1, phrase 3 (هٰذَا دَرْسٌ سَهْلٌ) : omise (دَرْسٌ non enseigné) ; exercice 2 (les deux pages de
    phrases à compléter) : seules les phrases à mots connus sont reprises, les autres sont omises ou
    adaptées (جَلَسَ, خَلْفَ, الْمُسْتَشْفَى, الْمُسْتَوْصَفِ, السِّكِّينُ, الْغُرْفَةِ… non enseignés ou douteux) ;
  - exercice 3 : la consigne « mets un nom qui convient » devient un `fill_gap` à choix, une seule option
    étant accordée en genre avec l'adjectif ; exercice 4 repris tel quel (sauf لِمَاذَا) ;
  - السِّكِّينُ حَادٌّ (exemple de حَادٌّ dans le PDF) : d'abord non repris (سِكِّينٌ pouvant être masculin ou
    féminin), puis rétabli après la fusion avec la leçon 8, qui présente سِكِّينٌ au masculin.
- **Fautes ou formes douteuses du PDF** :
  - pluriel de حَادٌّ donné comme حَادَّةٌ : c'est le féminin singulier, pas un pluriel → omis ;
  - pluriel de طَائِرٌ donné comme طَائِرَةٌ (« avion » / féminin singulier) → omis (طَائِرٌ n'a pas de pluriel
    affiché dans l'app) ;
  - مُفْرَد « الثَّانَوِيَّةُ » : présenté avec l'article, comme dans le PDF (« le lycée ») ;
  - كُسَالَى, جِيَاعٌ, غِضَابٌ, عِطَاشٌ, مُجْتَهِدُونَ, فَوَاكِهُ, عَصَافِيرُ, مَرَاوِحُ, مُسْتَوْصَفَاتٌ, لُعَبٌ,
    أَيَّامٌ, أَكْوَابٌ : conservés tels que le PDF les donne (formes courantes).
- **Mots ajoutés** (absents du tableau de vocabulaire, nécessaires aux phrases du livre) :
  كَبِيرٌ, صَغِيرٌ, لُغَةٌ, الْعَرَبِيَّةُ, الْفَرَنْسِيَّةُ, مَدِينَةٌ, لَذِيذٌ, تُفَّاحٌ, مَلْآنُ,
  النَّعْتُ, الْمَنْعُوتُ.
- **Traductions** :
  - كَبِيرٌ = « grand, gros » ; طَوِيلٌ = « grand (de taille), long » (traduction du chapitre 7) : comme les
    deux traductions se recouvrent, aucun exercice du chapitre 9 ne les propose ensemble (association,
    choix) ;
  - غَضْبَانُ = « en colère, fâché » (le PDF dit « énervé »), جَوْعَانُ = « qui a faim, affamé », عَطْشَانُ =
    « qui a soif, assoiffé » ; عُصْفُورٌ = « un petit oiseau, un moineau » (le PDF dit « oiseau » pour
    طَائِرٌ comme pour عُصْفُورٌ) ; لُعْبَةٌ = « un jeu, un jouet » ; كُوبٌ = « un verre, un gobelet ».
- **Genre** : تُفَّاحٌ est un nom collectif masculin (une pomme : تُفَّاحَةٌ) ; الْعَرَبِيَّةُ, الْإِنْكِلِيزِيَّةُ,
  الْفَرَنْسِيَّةُ, الثَّانَوِيَّةُ sont féminins ; النَّعْتُ et الْمَنْعُوتُ sont masculins.
- **Explications ajoutées (données à l'oral, absentes du PDF)** :
  - le PDF dit que « les noms possédant un ajout de ان avant leur fin sont interdits de tanwin » :
    c'est trop large (إِنْسَانٌ, بُسْتَانٌ gardent leur tanwin). La leçon limite la règle aux adjectifs d'état de
    forme فَعْلَانُ (« en général ») et cite les contre-exemples ; elle ajoute les prénoms en ـان (عُثْمَانُ) ;
  - fatha à la place de la kasra pour ces adjectifs sans article ni annexion (comme مَكَّةُ), kasra avec
    l'article ;
  - le féminin en ـَى (عَطْشَى, غَضْبَى, جَوْعَى, كَسْلَى) ;
  - un nom propre est défini : son adjectif prend l'article (زَيْدٌ الطَّوِيلُ) ;
  - dans une annexion, la voyelle de l'adjectif montre le mot qu'il décrit (اِبْنُ الْمُدِيرِ الْجَدِيدِ /
    الْجَدِيدُ) ;
  - groupe de mots / phrase : « deux articles : on décrit ; un seul article : on affirme » ;
  - عِنْدَ n'est pas une préposition mais un premier terme d'annexion (comme تَحْتَ) ;
  - un nom de langue est féminin ; الْعَرَبِيَّةُ est le féminin de عَرَبِيٌّ ;
  - le pluriel en « -āt » existe aussi pour des noms masculins de choses (مُسْتَوْصَفَاتٌ, حَمَّامَاتٌ) ;
    كُسَالَى se termine par un ى du féminin, donc sans tanwin.
- **Validateur** : aucune règle ajoutée.
- **Icônes** : aucune nouvelle icône (الثَّانَوِيَّةُ réutilise `school`).
- **Relectures** : trois relecteurs indépendants (arabe et fidélité au PDF, exercices, français et
  pédagogie), puis une seconde relecture sur les corrections ; chaque remarque a été contrôlée avant
  correction. Corrigés notamment :
  - la règle « groupe de mots ou phrase » (trop simple : elle ne tient pas pour un nom propre ou une
    annexion définie) : réécrite en « nom défini + adjectif indéfini = phrase ; deux définis ou deux
    indéfinis = groupe de mots », avec la précision que l'adjectif de الْكِتَابُ جَدِيدٌ est un خَبَر ;
  - « l'adjectif prend le ة » nuancé (« en général ») à cause des adjectifs d'état en ـَى ;
  - une question de `practice-3` dont un distracteur était aussi juste (عَبَّاسٌ غَنِيٌّ est une phrase
    correcte) ; un distracteur trop proche (طَائِرٌ pour عُصْفُورٌ) ; une seconde réponse acceptée
    (ذَهَبَ placé après le sujet) ;
  - l'accusatif ajouté à l'accord en cas (قَرَأْتُ كِتَابًا جَدِيدًا) ; le féminin مَلْأَى de مَلْآنُ ;
  - `practice-3` déplacé avec les premières activités (il travaille « l'adjectif est supprimable »).
  - Non repris (optionnel) : les pluriels طُيُورٌ (de طَائِرٌ) et حِدَادٌ (de حَادٌّ), attestés mais non
    donnés par le PDF.
