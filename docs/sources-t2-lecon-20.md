# Tome de Médine 2, leçon 20 (`medine-t2.l20`)

Vingtième chapitre du **Tome 2** (niveau `medine-t2`) : un cours de **révision de l'`إِعْرَابُ` du nom** (les marques de base et les marques secondaires, le duel à ses trois cas), avec un texte du livre (Bashīr, ʿUmar, Muʿāwiya et leur père), dix exercices et un vocabulaire nouveau. La seule notion nouvelle est la construction **`أَحَدُهُمَا … وَالْآخَرُ`** (« l'un des deux, l'autre »), que le professeur ne donne que par les exemples des exercices 8 et 9. Aucune modification de `core/` ni de l'app : seulement du contenu
(`app/src/main/assets/content/ar/medine-t2/l20/`) et la ligne `"l20"` dans `medine-t2/level.json` (après `"l19"`). Aucune nouvelle icône. Chapitre généré avec `tools/generateur_contenu.py` (scripts hors du dépôt).

## Support

Un seul PDF de diapositives (« Livre de Médine, Tome 2 », cours numéro 20, 17 pages dont une page blanche, deux logos et une page de droits) :

- **`الْمُثَنَّى`, révision** : `إِعْرَابُ الِاسْمِ الْأَصْلِيُّ` (les trois marques de base, avec `كِتَابٌ` / `كِتَابًا` / `كِتَابٍ`), `الْإِعْرَابُ الْفَرْعِيُّ` (quatre sortes de noms : pluriel masculin régulier, duel, pluriel féminin régulier, nom sans tanwin, avec un exemple pour chaque cas) et `إِعْرَابُ الْمُثَنَّى` (le `ا` au nominatif, le `ي` à l'accusatif et au génitif) ;
- le **texte du livre** (une page : un père interroge ses fils sur les sourates qu'ils ont mémorisées, puis va au marché ; le texte du PDF s'arrête à `اِشْتَرَيْتُهُ بِرِيَالَيْنِ.`, il est peut-être coupé) ;
- les **exercices 1 à 10** (images de pages) : répondre d'après le texte (1), le même nom aux trois cas (2), répondre « deux » avec le duel à des questions `كَمْ` (3, 4, 5), mettre au duel le mot souligné (6), une phrase avec chacun de quatre duels (7), `أَحَدُهُمَا وَالْآخَرُ` (8) et `إِحْدَاهُمَا وَالْأُخْرَى` (9), `ذُو` et `ذَاتُ` devant un duel (10) ;
- le **vocabulaire nouveau** (huit noms avec pluriel et exemple, trois verbes à quatre formes). Le haut du tableau est lisible (colonnes : singulier, pluriel, exemple, traduction).

## Plan du chapitre

| Partie | Activités |
|---|---|
| Revoir : les marques du cas et le duel | `revue-1` (rappel `review`, sans carte : marques de base et marques secondaires), `revue-2` (le duel : l'alif ou le yā) |
| Des mots nouveaux | `vocab-1` (`مُشْطٌ`, `مِخَدَّةٌ`, `زِرٌّ`, `مِرْآةٌ`), `vocab-2` (`لِصٌّ`, `جُنَيْهٌ`, `حَادِثٌ`, `تَفْسِيرٌ`), `vocab-3` (`ذَبَحَ`, `شَرَحَ`, `كَفَى`, `أَكْرَمَ`, tableau des quatre formes), `vocab-4` (`مَنَارَةٌ`, `مُفِيدٌ`, `خُرُوجٌ`) |
| L'un des deux, l'autre | `grammar-1` (`أَحَدُهُمَا` / `الْآخَرُ`, `إِحْدَاهُمَا` / `الْأُخْرَى`, les onze phrases des exercices 8 et 9) |
| Le texte du livre | `dialogue-1` (sourates, deux enseignants, deux mots nouveaux), `dialogue-2` (au marché, le stylo à deux couleurs) |
| Les exercices du livre | `practice-1` (ex. 1, 2), `practice-2` (ex. 3), `practice-3` (ex. 4), `practice-4` (ex. 5, phrases 1 à 5), `practice-5` (ex. 5, phrases 6 à 10), `practice-6` (ex. 6, phrases 1 à 7), `practice-7` (ex. 6, phrases 8 à 14), `practice-8` (ex. 7), `practice-9` (ex. 10) |
| Bilan | `quiz` (20 écrans) |

Outils de la palette : `sort` (marque de base / secondaire ; les trois cas ; `ا` / `ي` ; `أَحَدُهُمَا` / `إِحْدَاهُمَا` ; `ذُو` / `ذَاتُ` ; passé / présent / impératif ; la raison du yā), `spot` (le duel au génitif, le nom à marque secondaire), `memory` (`مُشْطٌ`, `مِخَدَّةٌ`, `زِرٌّ`, `مِرْآةٌ`, sans image), `build` (`مِخَدَّةٌ`, `مَنَارَةٌ`, `حَادِثٌ`), `match` (singulier / pluriel), `fill_gap` (le duel au nominatif, à l'accusatif, au génitif : les trois options sont le même mot à trois cas, qui se distinguent par une lettre), `order`, `typing` (dont des `question` avec la réponse en français dans la consigne), `question`, `true_false`.

## Notions et éléments nouveaux (17)

- **Mots du tableau du PDF** (8 noms) : `مُشْطٌ` (`أَمْشَاطٌ`), `مِخَدَّةٌ` (`مِخَدَّاتٌ`), `زِرٌّ` (`أَزْرَارٌ`), `مِرْآةٌ` (`مَرَايَا`), `جُنَيْهٌ` (`جُنَيْهَاتٌ`), `لِصٌّ` (`لُصُوصٌ`), `تَفْسِيرٌ` (`تَفَاسِيرُ`), `حَادِثٌ` (`حَوَادِثُ`, id `w:hadith-accident`, car `w:hadith` est `حَدِيثٌ`).
- **Verbes** (3 + 1) : `ذَبَحَ`, `شَرَحَ`, `كَفَى` (quatre formes du PDF) et `أَكْرَمَ` (de l'exemple `أَكْرِمِ الطَّالِبَاتِ`).
- **Mots du texte et des exercices** (3) : `مَنَارَةٌ`, `مُفِيدٌ`, `خُرُوجٌ`.
- **Construction nouvelle** (2) : `أَحَدُهُمَا` et `إِحْدَاهُمَا` (avec `الْآخَرُ` / `الْأُخْرَى` : `آخَرُ` est enseigné au chapitre 16 et `أُخْرَى` au chapitre 10 ; `أَحَدٌ` / `إِحْدَى` viennent des nombres, chapitre 3).
- Éléments **déjà enseignés et réutilisés sans carte** : `الْمُثَنَّى`, `جَمْعُ الْمُذَكَّرِ السَّالِمُ`, `جَمْعُ الْمُؤَنَّثِ السَّالِمُ`, `الْإِعْرَابُ الْأَصْلِيُّ`, `الْإِعْرَابُ الْفَرْعِيُّ`, `الْمَمْنُوعُ مِنَ الصَّرْفِ`, `كَمْ` + tamyiz, `ذُو` / `ذَاتُ`, `يَا أَبَتِ`, `الْمُتَوَسِّطَةُ` et `الثَّانَوِيَّةُ` (chapitres 12 et 9 du tome 1, repris sans article), `مَرْفُوعٌ` / `مَنْصُوبٌ` / `مَجْرُورٌ` (utilisés dans les analyses des chapitres 12 et 13).

## Écarts avec le PDF et choix faits

- **Texte du livre** : entièrement revocalisé (le PDF ne l'est que partiellement). Les guillemets autour de `الْمِشْطُ` et `الْمِخَدَّةُ` ne sont pas repris dans l'arabe (le validateur interdit « … » dans un texte arabe) ; ils sont dans la traduction. `بِكَمْ اِشْتَرَيْتَهُ` : le PDF écrit `بِكَمْ` avec un sukūn suivi de l'alif de liaison de `اشتريته` ; l'app écrit `بِكَمْ اِشْتَرَيْتَهُ` (comme `أَنْتَ اِشْتَرَيْتَ`).
- **`مُشْطٌ` / `مِشْطٌ`** : le tableau donne `مُشْطٌ` (damma) et le texte du livre `الْمِشْطُ` (kasra). Les deux formes existent dans les dictionnaires (Lisān al-ʿArab : `الْمُشْطُ وَالْمِشْطُ`). L'élément est `مُشْطٌ`, la note cite `مِشْطٌ`, le dialogue garde la forme du livre. La correction de la saisie accepte l'autre forme (mêmes lettres, harakat différentes : « juste avec remarque »). **À confirmer.**
- **Exemples du tableau remplacés** : les exemples du PDF pour `مُشْطٌ` (`تَرَجَّلَ زَيْدٌ بِمُشْطٍ`), `مِخَدَّةٌ` (`الْمِخَدَّةُ وِسَادَةٌ`), `زِرٌّ` (`فَكَّ زَيْدٌ أَزْرَارَ قَمِيصِهِ`), `لِصٌّ` (`اللِّصُّ تُقْطَعُ يَدُهُ`) et `حَادِثٌ` (`أَعُوذُ بِاللَّهِ مِنَ الْحَوَادِثِ`) emploient des mots pas encore enseignés (`تَرَجَّلَ`, `وِسَادَةٌ`, `فَكَّ`, `تُقْطَعُ`, `أَعُوذُ`) : l'app écrit une phrase simple, et cite l'exemple du professeur dans la note quand elle est instructive. Les exemples de `مِرْآةٌ`, `جُنَيْهٌ` et `تَفْسِيرٌ` sont ceux du PDF.
- **`جُنَيْهٌ`** : traduit « une livre égyptienne (monnaie) » (le PDF dit « Monnaie égyptienne ») ; c'est la livre égyptienne.
- **`أَكْرِمِ`** : le PDF écrit `أَكْرِمْ الطَّالِبَاتِ` ; l'app met la kasra devant l'article (`أَكْرِمِ`), comme la rencontre de deux sukūn (chapitre 7) ; la note de `أَكْرَمَ` l'explique. Le professeur ne donne que cet impératif : le présent `يُكْرِمُ` et le nom d'action `إِكْرَامٌ` ont été ajoutés (forme IV, certains).
- **Exercice 1** : trois questions, reprises en `typing` (`question`), avec la réponse en français dans la consigne. Le PDF n'en donne pas la réponse ; les réponses sont celles du texte.
- **Exercice 2** : le tableau est repris en astuce, puis en `sort` à trois catégories.
- **Exercices 3, 4, 5** : chaque question du livre est reprise en `typing` ou en `fill_gap` / `order` (le nom au duel). Le PDF écrit `بِكَمْ جَامِعَةً دَرَسْتَ؟` (exercice 5-9) : `بِ` est ici lu « dans » (comme `أَقَامَ بِمَكَّةَ`), la question porte sur le nombre d'universités ; l'app accepte `دَرَسْتُ بِجَامِعَتَيْنِ` et `دَرَسْتُ فِي جَامِعَتَيْنِ`. **À confirmer avec le professeur.** Dans les exercices 5-7 et 5-8, le livre met le nom après `كَمْ` à l'accusatif (`إِلَى كَمْ صَيْدَلِيَّةً`, `فِي كَمْ صَحِيفَةً`) : c'est repris tel quel, et la note de `practice-4` rappelle la remarque du professeur (`بِكَمْ رِيَالًا؟ بِكَمْ رِيَالٍ؟ كِلَاهُمَا صَحِيحٌ`) ; le chapitre 9 enseignait déjà le génitif.
- **Exercice 6** : quatorze phrases, deux activités. La phrase 11 (`فِي بَلَدِنَا لُغَةٌ وَاحِدَةٌ`) : le PDF ne souligne que `لُغَةٌ` ; au duel, `وَاحِدَةٌ` disparaît (`فِي بَلَدِنَا لُغَتَانِ`). La phrase 14 (`يَكْفِينِي الْآنَ جُنَيْهٌ`) : le sujet, donc `جُنَيْهَانِ` (alif).
- **Exercice 7** (une phrase avec chacun de quatre mots) : exercice libre dans le livre ; l'app donne des phrases modèles, puis des `fill_gap` et des `typing`.
- **Exercices 8 et 9** : leurs onze phrases sont des exemples « à étudier » dans le livre ; elles sont reprises en astuces dans `grammar-1` (avec leurs traductions), puis exercées. L'exercice 9-3 emploie `مُتَوَسِّطَةٌ` et `ثَانَوِيَّةٌ` (« collège » et « lycée ») sans article, comme adjectifs de `مَدْرَسَتَانِ` : ce sont les mots des chapitres 12 et 9 du tome 1.
- **Exercice 10** : le PDF écrit `حدًّا` à la phrase 1 (`مُفِيدٌ جِدًّا`, faute de lecture du PDF ; le `أ` final isolé est un artefact). `غَسَلْتُ الْقَمِيصَ ذَا الْجَيْبَيْنِ` : `ذَا` est l'accusatif de `ذُو` (le PDF l'écrit sans harakat). Les phrases du livre sont lues telles quelles ; le dialogue « qui parle à qui » de la phrase 2 (la question à la mère, puis la réponse) est repris comme dans le livre.
- **Ordre du cours** : les exercices des parties de rappel (`revue-1`, `revue-2`) n'emploient que des mots déjà enseignés. L'impératif `أَكْرِمِ` du tableau du professeur est cité dans l'astuce de `revue-1` (aperçu, glosé) et enseigné dans `vocab-3`.

## Explications ajoutées (données à l'oral, absentes du PDF)

- **Après `كَمْ` précédé d'une préposition**, le nom peut être à l'accusatif ou au génitif (règle de l'Alfiyya : le professeur le dit en une ligne à la fin de l'exercice 5).
- **Les cas de `أَحَدُ` et de `إِحْدَى`** : `أَحَدُ` se décline (`أَحَدَهُمَا`, `أَحَدِهِمَا`), `إِحْدَاهُمَا` garde son alif ; avec plus de deux, `أَحَدُهُمْ`, `إِحْدَاهُنَّ`.
- **`مَعْنَاهُمَا`** : le `ى` d'un nom en `ى` devient `ا` devant un pronom collé (comme `إِحْدَاهُمَا`).
- **`ذُو`, `ذَا`, `ذِي`** : leurs trois cas (comme `أَبُو`, `أَبَا`, `أَبِي`) et `ذَاتُ` qui se décline comme un nom ordinaire ; **`ذُو وَجْهَيْنِ`** : au sens figuré, quelqu'un de double, d'hypocrite.
- **`مِرْآةٌ`** : le `آ` est un `ء` suivi d'un `ا` long.
- **`كَفَى`** : `يَكْفِينِي` (« il me suffit ») ; l'impératif `اِكْفِ` perd son `ي`.
- **Le verbe reste au singulier devant un sujet au duel** (`جَاءَ طَالِبَانِ`) : il s'accorde seulement en genre (`جَاءَتْ مُدَرِّسَتَانِ`).

## Pas encore enseigné (donc sans exercice)

`كِلَاهُمَا` (cité en note, « les deux »), `وِسَادَةٌ`, `فَكَّ`, `تَرَجَّلَ`, `أَعُوذُ`, `الطَّبُّ`, `لِئَلَّا`, `كَيْ`, `حَتَّى`, `إِذَنْ`.

## Points à confirmer avec le professeur

`مُشْطٌ` / `مِشْطٌ` (laquelle préfère-t-il ?) ; `بِكَمْ جَامِعَةً دَرَسْتَ؟` (le sens de `بِ`) ; le texte du livre est-il coupé après `اِشْتَرَيْتُهُ بِرِيَالَيْنِ.` ? ; `جُنَيْهٌ` ; le genre de `مِرْآةٌ` (féminin, comme dans tous les dictionnaires).
