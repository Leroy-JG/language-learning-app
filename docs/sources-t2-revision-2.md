# Tome de Médine 2, révision 2 (`medine-t2.r02`)

Deuxième chapitre de **révision** du Tome 2 (niveau `medine-t2`), placé après le chapitre 21 : dossier `medine-t2/r02/`, libellé « Révision 2 »
(champ `label`), `number` 23 (le dernier cours couvert ; non contrôlé pour un chapitre à libellé, mais les leçons suivantes devront porter un numéro supérieur à 23). Aucune modification de `core/` ni de l'app : seulement du
contenu (`app/src/main/assets/content/ar/medine-t2/r02/`) et la ligne `"r02"` à la fin de `medine-t2/level.json`. Aucune nouvelle icône.
Chapitre généré avec `tools/generateur_contenu.py` (script hors du dépôt : `g.CH = "medine-t2.r02"` et `g.OUT` réglés après l'import).

## Support

Un seul PDF de diapositives (« Livre de Médine, Tome 2 », cours numéros **22 et 23**, tous deux intitulés « révision », 29 pages dont une page blanche, deux logos et une page de droits) :

- **Cours 22, `إِعْرَابُ الْفِعْلِ الْمُضَارِعِ`, révision** (pages 4 à 14, puis page de livre 15) : le plan du verbe (`مُعْرَبٌ` / `مَبْنِيٌّ`, `إِعْرَابٌ أَصْلِيٌّ` / `فَرْعِيٌّ`, `نُونُ النِّسْوَةِ` et `نُونُ التَّوْكِيدِ`),
  les trois marques de base (`الرَّفْعُ` damma, `النَّصْبُ` fatha, `الْجَزْمُ` sukūn), les deux cas de l'`إِعْرَابٌ فَرْعِيٌّ` (les trois pronoms sujets `ي`, `ا`, `و` ; les trois lettres finales `و`, `ي`, `ى`), `الْأَفْعَالُ الْخَمْسَةُ`,
  `الْفِعْلُ الْمُعْتَلُّ الْآخِرِ`, le grand schéma du présent, puis la page du livre `حَالَاتُ الْمُضَارِعِ الثَّلَاثُ` : le présent de `ذَهَبَ` à dix personnes, au nominatif, avec `لَنْ` et avec `لَمْ`. **Tout cela a déjà été enseigné** (chapitres 11 à 13 du tome 2 et « Révision 1 » : `r01.revue-1` à `revue-5`) ; seul le tableau de `ذَهَبَ` est nouveau comme support.
- **Cours 23, `إِعْرَابٌ`, `جَمْعُ الْمُذَكَّرِ السَّالِمِ`, révision** (pages 16 à 27) : `إِعْرَابُ الِاسْمِ الْأَصْلِيُّ` (`كِتَابٌ` / `كِتَابًا` / `كِتَابٍ`), `الْإِعْرَابُ الْفَرْعِيُّ` (quatre sortes de noms, avec un exemple par cas) et `إِعْرَابُ جَمْعِ الْمُذَكَّرِ السَّالِمِ`
  (`و` / `ي` / `ي`) : rappels de `medine-t2.l20.revue-1` ; puis le **texte du livre** (deux pages : les nouveaux enseignants, la classe de trente étudiants, le livre « قِصَصُ النَّبِيِّينَ », le prix des sacs), les **exercices 1 à 12** (images de pages) et le **vocabulaire nouveau** (neuf noms, deux verbes, dont `نَجَحَ` déjà enseigné).

## Plan du chapitre

| Partie | Activités |
|---|---|
| Le présent : les trois états | `revue-1` (le tableau du livre : `ذَهَبَ` à dix personnes, trois états ; sort, fill_gap à deux trous, spot, typing), `revue-2` (le plan complet du présent : verbe ordinaire, cinq verbes, finale défectueuse ; marques `ثُبُوتُ النُّونِ`, `حَذْفُ النُّونِ`, `حَذْفُ حَرْفِ الْعِلَّةِ`) |
| Les cas du nom | `revue-3` (marques de base, pluriel masculin régulier, `و` / `ي` / `ي`) |
| Des mots nouveaux | `vocab-1` (`قِصَّةٌ`, `أُسْرَةٌ`, `اِجْتِمَاعٌ`, `قَاعَةٌ`), `vocab-2` (`قَصْرٌ`, `أَمِيرٌ`, `فَضِيلَةٌ`, `جَائِزَةٌ`), `vocab-3` (`رَسَبَ`, avec `نَجَحَ` dans le tableau des quatre formes), `vocab-4` (`ثَانِيَةٌ`, `بُرْتُقَالَةٌ`, `بَاكِسْتَانِيٌّ`) |
| Une négation qui se répète | `grammar-1` (`لَا` devant un verbe au passé, **point de grammaire nouveau** : exercice 12 du livre) |
| Le texte du livre | `dialogue-1` (les nouveaux enseignants, la classe), `dialogue-2` (le livre, les pages, le sac) |
| Les exercices du livre | `practice-1` (ex. 1, six questions sur le texte), `practice-2` (ex. 2 et 3 : le pluriel masculin régulier), `practice-3` (ex. 4 et 5 : les dizaines, les trois cas), `practice-4` (ex. 6), `practice-5` (ex. 7 et 8), `practice-6` (ex. 9), `practice-7` (ex. 10 et 11), `practice-8` (ex. 12) |
| Bilan | `quiz` (20 écrans) |

Outils de la palette : `sort` dans presque chaque activité (états du verbe, marque de base ou secondaire, pluriel régulier ou brisé, annexion ou préposition, cas du nombre, singulier ou pluriel, passé / présent / impératif), `spot` (le verbe invariable, le pluriel régulier, le nombre, les deux `لَا`),
`fill_gap` (dont deux trous : `أَنْتُمْ لَمْ {0} وَأَنْتُنَّ لَمْ {1}`, les trois options sont toujours des mots différents par une lettre, jamais par les seuls harakat), `order`, `typing` (`question` pour les six questions du livre, réponses courtes acceptées), `question` à choix arabes
(analyses du professeur : `فِعْلٌ مُضَارِعٌ مَرْفُوعٌ وَعَلَامَةُ رَفْعِهِ ثُبُوتُ النُّونِ`…), `compare` (`مَا أَكَلْتُ` → `لَا أَكَلْتُ وَلَا شَرِبْتُ`, les quatre pluriels brisés, du pays à l'habitant), `memory` (`vocab-1`), `build` (`قَاعَةٌ`, `جَائِزَةٌ`, `بُرْتُقَالَةٌ`), `match` (`sing-plur` pour les huit pluriels de l'exercice 3).

## Éléments nouveaux (12 éléments, 1 point de grammaire)

- **Noms** (9) : `قِصَّةٌ` (`قِصَصٌ`), `أُسْرَةٌ` (`أُسَرٌ`), `اِجْتِمَاعٌ` (le PDF ne donne pas de pluriel), `قَاعَةٌ` (`قَاعَاتٌ`), `أَمِيرٌ` (`أُمَرَاءُ`), `فَضِيلَةٌ` (`فَضَائِلُ`), `جَائِزَةٌ` (`جَوَائِزُ`), `ثَانِيَةٌ` (`ثَوَانٍ`), `قَصْرٌ`
  (`قُصُورٌ`, de l'exemple du professeur `أَمِيرُ الْمَدِينَةِ فِي الْقَصْرِ`). Le tableau du professeur contient aussi `مُؤْمِنٌ` (déjà enseigné au chapitre 17) : réutilisé, non redéfini.
- **Verbe** (1) : `رَسَبَ` (quatre formes du PDF dans `vocab-3` : `يَرْسُبُ`, `اُرْسُبْ`, `رُسُوبًا` ; `نَجَحَ` : `يَنْجَحُ`, `اِنْجَحْ`, `نَجَاحًا`, sans carte nouvelle).
- **Mots des exercices et du texte** (2) : `بُرْتُقَالَةٌ` (exercice 9-7 ; `بُرْتُقَالٌ` est le collectif du chapitre 7) et `بَاكِسْتَانِيٌّ` (exercice 2-6).
- **Point de grammaire** : `grammar-1`, `لَا` + passé (règle du professeur : il faut répéter `لَا` quand on rapporte un fait).
- Tous les autres termes (`مُعْرَبٌ`, `مَبْنِيٌّ`, `ثُبُوتُ النُّونِ`, `حَذْفُ النُّونِ`, `حَذْفُ حَرْفِ الْعِلَّةِ`, `جَمْعُ الْمُذَكَّرِ السَّالِمُ`, `الْمَرْفُوعُ` / `الْمَنْصُوبُ` / `الْمَجْرُورُ`, `عُقُودٌ` cité en passant avec sa traduction…) sont réemployés sans carte.

## Écarts avec le PDF et choix faits

- **Un seul chapitre pour deux cours** : les deux cours 22 et 23 sont des « révisions » ; sur demande de l'utilisateur, ils forment un chapitre `r02` (libellé « Révision 2 »), comme `r01` du tome 2, et non deux chapitres `l22` et `l23`. Les rappels sont des `review` sans carte ; le vocabulaire, le texte, les exercices et le point de grammaire nouveau suivent le modèle de `l20`.
- **Cours 22** : toutes les diapositives de la page 5 à la page 14 reprennent la partie 2 de la « Révision 1 » (`r01.revue-1` à `revue-5`) ; elles ne sont pas redonnées en rappel complet, seulement résumées dans le plan de `revue-2`. Le tableau du livre (page 15) est repris dans `revue-1` : dix lignes seulement, le duel (`هُمَا`, `أَنْتُمَا`) n'y figure pas.
- **Fautes de frappe du PDF corrigées** : `رَأَيْتُ ٥٠ طَالِبًا يَا الْقَاعَةِ` (exercice 9-2) → `فِي الْقَاعَةِ` ; l'exercice 5 écrit `قُلْتُ لِلْمُدَرِّسَاتِ` dans la colonne du pluriel masculin (`ي`) : l'app écrit `قُلْتُ لِلْمُدَرِّسِينَ` (**à confirmer**, le livre veut peut-être donner le féminin sain) ;
  `أَيْنَ الْمُهَنْدِسُونَ.` (exercice 2-2) est une question : l'app écrit `؟` ; l'impératif de `رَسَبَ` est écrit `أُرْسُبْ` (hamza) dans le PDF : l'app écrit `اُرْسُبْ` (alif de liaison, comme `اُكْتُبْ`) ; le texte écrit `اِجْتِمَاعٍ…` (points de suspension) : l'app termine la phrase par un point.
- **Texte du livre** : revocalisé entièrement (le PDF ne l'est que partiellement) et coupé en deux dialogues. `بِكَمِ اشْتَرَيْتَهَا` : le PDF écrit la kasra de liaison sur le `م` ; l'app la garde (dans `l20` : `بِكَمْ اِشْتَرَيْتَهُ`). `النَّدْوِيِّ` : le PDF écrit le `د` avec une fatha ; l'app écrit la nisba usuelle `النَّدْوِيِّ` (sukūn), du nom de la Nadwa (`نَدْوَةُ الْعُلَمَاءِ`) — **à confirmer**.
  `وَلَكِنَّنِي` : orthographe du reste de l'app (le PDF écrit `وَلٰكِنَّنِي`). `فَبَقِيَ سَبْعٌ وَثَلَاثُونَ صَفْحَةً` : le PDF met le verbe au masculin devant un nombre qui compte un nom féminin ; repris tel quel.
- **Pluriel de `جَدِيدٌ`** : le texte emploie `جُدُدٌ` pour des personnes (`مُدَرِّسُونَ جُدُدٌ`) ; c'est le pluriel de `w:jadid`, repris tel quel.
- **Exemples du tableau remplacés** : `أَخَذَ زَيْدٌ جَائِزَةً عَلَى نَجَاحِهِ` (emploie `نَجَاحٌ`, présenté dans `vocab-3` seulement) devient `أَخَذَ زَيْدٌ جَائِزَةً` ; `فِي الْقَاعَةِ اجْتِمَاعٌ عَظِيمٌ` (`عَظِيمٌ` non enseigné) devient `فِي الْقَاعَةِ اجْتِمَاعٌ كَبِيرٌ` ; l'exemple de `ثَانِيَةٌ`, `اِنْتَظِرْ ثَانِيَةً`, de `قِصَّةٌ`, `قَرَأْتُ قِصَّةَ النَّبِيِّ نُوحٍ`, d'`أَمِيرٌ`, `أَمِيرُ الْمَدِينَةِ فِي الْقَصْرِ`,
  de `فَضِيلَةٌ`, `فِي زَيْدٍ فَضَائِلُ`, et de `أُسْرَةٌ`, `هٰذِهِ أُسْرَةُ زَيْدٍ` sont ceux du PDF.
- **Ordre du cours** : les exercices des rappels (`revue-1` à `revue-3`) n'emploient que des mots déjà enseignés (aucun des douze mots nouveaux). `قَاعَةٌ` est présenté après `اِجْتِمَاعٌ` (son exemple l'emploie). L'exercice 12 est donné par `grammar-1` avant le texte, dont la première phrase (`لَا دَخَلُوا الْفُصُولَ وَلَا هُمْ …`) emploie la même construction.
- **Exercice 1** : six questions, reprises en `typing` (`question`) avec la réponse en français dans la consigne ; le PDF ne donne pas de corrigé, les réponses sont celles du texte. La question 3 est écrite `لِمَنِ الْكِتَابُ` (kasra de liaison) ; les guillemets autour du titre sont retirés (le validateur interdit « » dans l'arabe).
- **Exercice 2** : six phrases à lire, toutes avec un pluriel au nominatif ; reprises dans une astuce, puis exercées (`question`, `fill_gap`, `spot`). **Exercice 3** : les huit pluriels, en `match` (`sing-plur`) et en `typing`.
- **Exercice 4** : les huit dizaines en astuce (le livre donne les chiffres et les mots), puis `sort` des trois cas. Le PDF les appelle `عُقُودٌ` : cité avec sa traduction (« dizaines »), sans carte.
- **Exercice 5** : le tableau (trois lignes × trois cas) est repris en astuce.
- **Exercices 6 à 8** : phrases à trous du livre, reprises en `fill_gap` (jamais deux options qui ne diffèrent que par les harakat) et `typing`. Exercice 6-1 : le livre donne `الطُّلَّابَ` et ne met entre parenthèses que `الْمُجْتَهِدُونَ` : le trou est l'adjectif.
- **Exercice 9** : dix phrases à écrire en lettres, reprises en `typing`, `fill_gap`, `order`, `spot`. Le mois est le mois lunaire (29 ou 30 jours). `سِنِّي ثَلَاثُونَ سَنَةً` : `سِنِّي` est « mon âge ».
- **Exercices 10 et 11** : les nombres de 21 à 30 devant `طَالِبَةً` (astuce et `fill_gap`) et les six phrases de l'exercice 11, dont `ثَلَاثٌ وَثَلَاثُونَ رُوبِيَّةً وَخَمْسَةٌ وَأَرْبَعُونَ رِيَالًا` (`رُوبِيَّةٌ` féminin, `رِيَالٌ` masculin : le PDF écrit les chiffres seulement).
  `فِي سُورَةِ الرَّحْمٰنِ ثَمَانٍ وَسَبْعُونَ آيَةً` : la sourate ar-Raḥmān compte bien 78 versets.
- **Exercice 12** : quatre phrases et la règle `إِذَا دَخَلَتْ لَا عَلَى الْفِعْلِ الْمَاضِي وَجَبَ تَكْرَارُهَا عِنْدَ الْإِخْبَارِ` : reprises en `grammar-1` (astuce, `compare`, `fill_gap`, `spot`, `order`, `typing`) puis en `practice-8`.

## Explications ajoutées (données à l'oral, absentes du PDF)

- **`لَا` + passé** : la règle de répétition vaut « quand on rapporte un fait » ; elle n'est pas obligatoire dans une invocation (`دُعَاءٌ`) — règle classique (Ibn Hishām, `مُغْنِي اللَّبِيبِ`). Pour nier un seul verbe au passé, on emploie `مَا`.
- **Pluriel masculin régulier** : le `ن` final porte une fatha (`◌ُونَ`, `◌ِينَ`), au duel une kasra (`◌َانِ`, `◌َيْنِ`) ; en général il désigne des personnes de sexe masculin et les adjectifs qui les décrivent ; un adjectif peut être au pluriel régulier même si le nom décrit est un pluriel brisé (`طُلَّابٌ مُجْتَهِدُونَ`).
- **Dizaines** : elles ne changent pas selon le genre et se terminent comme le pluriel masculin régulier (`◌ُونَ` / `◌ِينَ`) ; le nom compté est au singulier, à l'accusatif.
- **`النَّبِيُّونَ` / `النَّبِيِّينَ`** : pluriel masculin régulier de `نَبِيٌّ`, à côté de `أَنْبِيَاءُ` (titre du livre `قِصَصُ النَّبِيِّينَ`).
- **`أَمِيرُ الْمُؤْمِنِينَ`**, titre des califes ; **`فَضِيلَةُ الشَّيْخِ`**, titre de respect (« Son Éminence le cheikh ») ; `أُمَرَاءُ`, `جَوَائِزُ`, `فَضَائِلُ` sont des noms sans tanwin (modèle `مَفَاعِلُ` / `فُعَلَاءُ`).
- **Voyelle de l'alif de l'impératif** selon la voyelle de la lettre du milieu au présent : `يَرْسُبُ` → `اُرْسُبْ`, `يَنْجَحُ` → `اِنْجَحْ`.
- **Du pays à l'habitant** : `بَاكِسْتَانُ` → `بَاكِسْتَانِيٌّ`, comme `الْهِنْدُ` → `هِنْدِيٌّ` ; pluriel régulier `بَاكِسْتَانِيُّونَ` (et `هُنُودٌ`, pluriel brisé, pour les Indiens).
- **`ثَانِيَةٌ`** : féminin de `ثَانٍ` (« deuxième »).
