# Tome de Médine 2, leçon 1 (`medine-t2.l01`)

Premier chapitre du **Tome 2** : un nouveau niveau, `medine-t2`, ajouté à `course.json` après `medine-t1`
(l'accueil permet de passer d'un tome à l'autre ; le tome 2 n'a pas encore de défi ni d'examen).
Les outils de `tools/` acceptent la variable `LURATA_NIVEAU=medine-t2` (générateur, `controle_qualite.py`,
`lire_chapitre.py`) ; `deja_enseigne.py` liste maintenant tous les niveaux (les éléments du tome 2 sont
préfixés `t2/l01`).

## Support

Deux PDF de diapositives (« Livre de Médine, Tome 2 », cours numéro 1) :

- **1a** (23 p.) : `الْجُمْلَةُ الِاسْمِيَّةُ` deuxième partie (sujet et prédicat), `النَّوَاسِخُ`, `إِنَّ وَأَخَوَاتُهَا`
  (`إِنَّ` / `أَنَّ`, `لَعَلَّ`), le pronom comme `اِسْمُ إِنَّ`, exercices 3, 4 et 5 ;
- **1b** (28 p.) : reprise des diapositives de `إِنَّ`, schéma de `الْإِعْرَابُ` (trois emplois de l'accusatif),
  texte du livre (deux pages) et exercices 1 et 2, `أَمْ` (avec l'exercice 7), `الْعَدَدُ` deuxième partie
  (vue d'ensemble, `مِائَةٌ وَأَلْفٌ`, exercice 12), vocabulaire nouveau et expressions nouvelles.

Les deux PDF se recoupent (1b reprend les diapositives de `إِنَّ` de 1a) : le contenu est fusionné.

## Plan du chapitre

| Partie | Activités |
|---|---|
| Revoir la phrase nominale | `revue-1` (rappel `review`, aucune carte) |
| Ceux qui abrogent : `النَّوَاسِخُ` | `grammar-1` |
| Intelligent, marié, célibataire | `vocab-1` |
| Certes… : `إِنَّ` | `grammar-2` (sujet à l'accusatif), `grammar-3` (pronoms), `practice-1` (exercices 4 et 5) |
| `لَعَلَّ`, `أَنَّ` et les six particules | `grammar-4`, `grammar-5` (+ trois emplois de la fatha) |
| Cent et mille | `grammar-6`, `vocab-2` |
| Prix, expressions et exercice 12 | `grammar-7` (`بِ`), `vocab-3`, `vocab-4` (`ذُو`), `practice-2` (exercice 12) |
| Choisir entre deux réponses : `أَمْ` | `grammar-8` (exercice 7) |
| Le texte du livre | `dialogue-1`, `dialogue-2`, `practice-3` (exercices 1 et 2) |
| Bilan | `quiz` |

## Notions et éléments nouveaux (28)

- **Termes** : `النَّاسِخُ` (pl. `النَّوَاسِخُ`), `اِسْمُ إِنَّ`, `خَبَرُ إِنَّ`, `التَّوْكِيدُ`, `التَّرَجِّي`.
- **Particules** : `إِنَّ`, `أَنَّ`, `لَعَلَّ`, `بِ`, `فَقَطْ`, `ذُو`.
- **Mots** : `ذَكِيٌّ`, `مُتَزَوِّجٌ`, `عَزَبٌ`, `نَاجِحٌ`, `مُعْجَمٌ`, `تِلْفَازٌ`, `دُولَارٌ`, `رُوبِيَّةٌ`, `صَفْحَةٌ`, `مِائَةٌ`, `أَلْفٌ`.
- **Phrases** : `ذُو خُلُقٍ`, `ذُو عِلْمٍ`, `ذُو مَالٍ`, `لَعَلَّكَ بِخَيْرٍ.`, `بِكَمْ هٰذَا؟`, `بِكَمْ هٰذَا الْكِتَابُ؟`.
- Réutilisés sans redéfinir (déjà au tome 1) : `غَنِيٌّ`, `أَمْ`, `رِيَالٌ`, `الْمُبْتَدَأُ`, `الْخَبَرُ`, `الضَّمِيرُ`, `الظَّرْفُ`, les nombres de 3 à 10…
- **Aucune nouvelle icône, aucune modification du moteur** (seul un test de `core` a été corrigé : il supposait un seul niveau).

## Écarts avec le PDF

- **Fautes du PDF corrigées** :
  - exercice 12 : le PDF écrit `1000 = مِائَةُ رَجُلٍ…` et `1050 = أَلْفُ رَجُلٍ…` ; l'app donne `100` et `1000` (cent, mille) ;
  - `التِلْفَازُ` (sans shadda) est écrit `التِّلْفَازُ` (le `ت` est une lettre solaire) ;
  - `بِكَمْ هٰذَا الْكِتَابِ` (kasra finale du PDF) est écrit `الْكِتَابُ` : c'est le démonstratif et son complément, au nominatif.
- **Lecture du PDF** : exercice 7, phrase 2 (« هي / ١لصين / اليابان ») lue `هِيَ / الصِّينِ / الْيَابَانِ` ; exercice 4, phrase 4 (`أَذْكِيَاءُ`) vérifiée sur le tableau de vocabulaire.
- **Exercices du livre repris** :
  - exercice 3 (tableau des dix combinaisons) → `grammar-3` ;
  - exercice 4 (dix phrases) et exercice 5 (trois phrases) → `practice-1` ;
  - exercices 1 et 2 → `practice-3` (les questions 2 et 4 sont posées en choix, car elles contiennent `أَيُحِبُّهُ` ; les phrases `أ` / `ب` de l'exercice 2 sont reprises en choix et en vrai/faux) ;
  - exercice 7 → `grammar-8` : les phrases 4 (`يَهُودٌ`) et 9 (`إِيرَانُ`) sont **omises** (mots non enseignés) ; l'ordre des mots est celui des modèles du livre (le choix juste après `أَ`), l'autre ordre valide est accepté en dictée ;
  - exercice 12 → `practice-2` : les cinq phrases, avec les nombres donnés en chiffres dans la consigne française.
- **Exemples du PDF remplacés ou cités en aperçu** :
  - `يَاسِرٌ عَزَبٌ يُرِيدُ الزَّوَاجَ` : seul le début sert d'exemple de la carte ; la phrase complète est citée en note (`يُرِيدُ`, `الزَّوَاجُ` viendront plus tard) ;
  - `زَيْدٌ مُتَزَوِّجٌ بِفَاطِمَةَ` : repris dans `grammar-7`, après l'enseignement de `بِ` (l'exemple de la carte `مُتَزَوِّجٌ` est `حَامِدٌ مُتَزَوِّجٌ` : `بِ` n'est pas encore enseigné) ;
  - `هٰذَا مُعْجَمٌ عَرَبِيٌّ فَرَنْسِيٌّ` : repris dans une astuce (`عَرَبِيٌّ` et `فَرَنْسِيٌّ` : adjectifs, aucun exercice) ;
  - `دَرَسَ زَيْدٌ النَّحْوَ فَقَطْ` : cité avec `فَقَطْ` (`دَرَسَ` : aperçu, sans exercice) ;
  - `إِنَّ زَيْدًا قَائِمٌ` (schéma `الْإِعْرَابُ`) : remplacé par `إِنَّ زَيْدًا جَالِسٌ` (diapositive de l'arbre, mot déjà enseigné) ;
  - `كَانَ`, `لَيْسَ`, `أَظُنُّ` (diapositive d'introduction de `النَّوَاسِخُ`) : **aperçus** sans carte ni exercice, hormis deux questions de reconnaissance (« quel mot nie ? », « quel mot confirme ? ») ;
  - `يَكْتُبُ` (prédicat verbal du schéma `الْمُبْتَدَأُ وَالْخَبَرُ`) : cité en aperçu.
- **Texte du livre** : presque sans voyelles dans le PDF ; vocalisation de l'app. Le texte est coupé en deux dialogues (`dialogue-1` : salutations, origines, montre ; `dialogue-2` : prix, frères et sœurs). Il s'arrête sur la question du professeur (`أَطَالِبَاتٌ هُنَّ؟`), comme le livre. Aperçus signalés dans une astuce (rien n'est exigé) : `أُحِبُّكَ`, `كَثِيرًا`, `الَّذِي`.
  - `أَنَا أُحِبُّكَ كَثِيرًا` est traduit « Je t'aime beaucoup » (le verbe est `أُحِبُّ`, celui du chapitre 11) ;
  - `الْمَدِينَةِ الْمُنَوَّرَةِ` est traduit « Médine » (comme au chapitre 5) ;
  - `لَعَلَّكَ بِخَيْرٍ` : le PDF met un point d'interrogation ; l'app met un point (c'est un souhait, « j'espère que tu vas bien »).
- **Titres des astuces** : les guillemets imbriqués vus dans l'outil de lecture (`««إِنَّ» ou «أَنَّ» ?»`) viennent de l'outil, pas de l'app (l'arabe balisé `{ar|…}` s'affiche en police arabe, sans guillemets).
- **Pluriels non repris** : aucun pluriel de `مِائَةٌ` (`مِئَاتٌ`), `أَلْفٌ` (`آلَافٌ`) ni de `صَفْحَةٌ` (`صَفَحَاتٌ` / `صَفْحَاتٌ`, variantes) : le PDF n'en donne pas.

## Choix de contenu

- **Une phrase du PDF devient une vraie règle, pas une liste** : la règle de `إِنَّ` est donnée dès `grammar-2` avec le tableau des cas (sujet : nominatif → accusatif, prédicat : nominatif), puis `grammar-3` (pronoms affixes), `grammar-4` (`لَعَلَّ`), `grammar-5` (`أَنَّ`, les six sœurs).
- **Les accusatifs du schéma `الْإِعْرَابُ`** (adverbe, nom de `إِنَّ`, `تَمْيِيزٌ` après `كَمْ`) sont repris dans `grammar-5` avec un tri, car les trois ont été enseignés au tome 1 (chapitres 8, 15 et 18).
- **Nombres** : seul `مِائَةٌ وَأَلْفٌ` est enseigné. Les nombres de 11 à 99 sont cités en **aperçu** (diapositive du PDF) avec leur règle (singulier à l'accusatif, comme après `كَمْ`) mais sans exercice ; le détail viendra dans une autre leçon.
- **Les `fill_gap` de pronoms** évitent de proposer ensemble `إِنَّكَ` / `إِنَّكِ` (identiques harakat masquées) ; `إِنِّي` et `إِنَّنِي` sont tous deux acceptés (`answers` du `typing`, `order` à un seul mot).
- **Ordre du parcours** : la préposition `بِ` (partie 7) est enseignée avant les phrases qui l'emploient ; `vocab-2` (monnaies) vient après `مِائَةٌ وَأَلْفٌ` ; le texte du livre vient après tout le reste.
- Deux voix (`m1` le professeur, `m2` Hāshim), comme dans les textes des chapitres précédents.

## Explications ajoutées (données à l'oral, absentes du PDF)

- `نَاسِخٌ` = « celui qui abroge » ; les `النَّوَاسِخُ` sont des verbes ou des particules (les quatre exemples : `كَانَ`, `لَيْسَ`, `إِنَّ`, `أَظُنُّ` et le tableau des cas de chacun : sujet / prédicat).
- `إِنَّ وَأَخَوَاتُهَا` réunit **six particules** en tout (`إِنَّ` et cinq sœurs : `أَنَّ`, `كَأَنَّ`, `لَكِنَّ`, `لَيْتَ`, `لَعَلَّ`, vers de l'Alfiyya), avec la nuance de chacune (`التَّوْكِيدُ`, `التَّشْبِيهُ`, `الِاسْتِدْرَاكُ`, `التَّمَنِّي`, `التَّرَجِّي`) ; `لَيْتَ` (souhait d'une chose difficile) ≠ `لَعَلَّ` (espoir d'une chose possible) ; `لَكِنَّ` (avec shadda) agit comme `إِنَّ`, alors que `لٰكِنْ` (chapitre 21 du tome 1) n'agit d'ordinaire pas ; `إِنَّ` et ses sœurs font l'inverse de `كَانَ`.
- `لَعَلَّ` exprime l'espoir pour une chose souhaitée et l'inquiétude pour une chose redoutée (`التَّرَجِّي` et `الْإِشْفَاقُ`) ; avec « je » : `لَعَلِّي` ou `لَعَلَّنِي`.
- Après `إِنَّ`, « je » et « nous » ont deux formes correctes : `إِنِّي` = `إِنَّ` + `ي`, `إِنَّنِي` avec le `ن` « de protection » (`نُونُ الْوِقَايَةِ`), de même `إِنَّنَا` / `إِنَّا` ; le pronom affixe tient la place de `اِسْمُ إِنَّ` (accusatif) mais reste invariable.
- Après `إِنَّ`, un nom **singulier (ou pluriel irrégulier)** prend la fatha ; le duel et les pluriels réguliers gardent leurs marques de l'accusatif (`ـَيْنِ`, `ـِينَ`, la kasra du pluriel féminin) ; le prédicat s'accorde quand c'est un nom ou un adjectif (pas un groupe « préposition + nom »).
- `أَنَّ` ne commence pas la phrase ; exemple d'aperçu `أَعْلَمُ أَنَّ زَيْدًا طَالِبٌ`.
- De 11 à 99, le dénombré est au singulier et à l'accusatif (comme après `كَمْ`) ; `مِائَةٌ` s'écrit aussi `مِئَةٌ`.
- `بِ` : l'alif de `الْ` reste écrit (`بِالْمَدِينَةِ`), contrairement à `لِ` (`لِلْمُدِيرِ`) ; sens selon le mot qui suit (lieu, prix, état) ; `كَمْ` ne change pas après `بِ`.
- `ذُو` : toujours annexé à un nom (au génitif) ; `ذَا` à l'accusatif, `ذِي` au génitif ; féminin `ذَاتُ` ; l'un des « cinq noms » (`الْأَسْمَاءُ الْخَمْسَةُ`).
- `أَمْ` : le premier choix suit `أَ`, le second suit `أَمْ` ; on répond en choisissant l'un des deux termes, pas par oui ou non. L'ordre « le choix juste après `أَ` » est celui du livre (et de la grammaire classique) ; l'autre ordre se rencontre aussi et reste accepté en dictée.
- `بِ` a aussi le sens instrumental (`بِالْقَلَمِ`, « avec le stylo »), cité hors exercice ; `لَعَلَّكَ بِخَيْرٍ` est dit à un homme (à une femme : `لَعَلَّكِ بِخَيْرٍ`).
- Racine de `دَرَسَ` / `دَرْسٌ` ; `غَنِيُّ الْقَلْبِ` : premier terme d'annexion, d'où la perte du tanwin.

## Corrections faites après la relecture (3 relecteurs + relecture du diff)

- « `إِنَّ` a six sœurs » → six particules en tout ; le pronom « je » (`ي`, pas `نِي`) ; « le prédicat s'accorde toujours » → seulement nom ou adjectif ; « le nom prend la fatha » → singulier ou pluriel irrégulier ; condition de `أَمْ` (l'exemple `أَزَيْدٌ دَخَلَ أَمْ يَاسِرٌ` contredisait « se suivent directement »).
- Cartes de tri `كَمْ كِتَابًا عِنْدَكَ؟` et `كَمْ طَالِبًا عِنْدَكَ؟` remplacées (`عِنْدَ` est lui-même un adverbe à l'accusatif, la carte avait deux réponses).
- Réponses acceptées ajoutées : `مِئَةُ` / `مِئَةِ`, réponses courtes (`بِمِائَةِ رِيَالٍ.`), réponses sans `الْمُنَوَّرَةِ`, `أَهُنَّ خَالَاتُكَ أَمْ عَمَّاتُكَ؟` (consigne précisée « à un homme »). Refusé : l'ordre `مِائَةُ صَفْحَةٍ فِي هٰذَا الْكِتَابِ` (non standard pour le cours).
- Rythme : plus de trois présentations d'affilée ni de trois exercices identiques de suite ; `practice-3` finit sur une production.
- Note de `grammar-1` : `كَانَ`, `لَيْسَ`, `أَظُنُّ` à retenir seulement par leur sorte et leur nuance. « Pourvu que… » pour `لَعَلَّ` avec « je ». Ligne `أَغَالِيَةٌ هِيَ؟` ajoutée au tableau du texte (`غَالٍ` : tome 1, chapitre 17).

## Pas encore enseigné (donc sans exercice)

`كَانَ`, `لَيْسَ`, `أَظُنُّ`, `أَعْلَمُ`, `كَأَنَّ`, `لَكِنَّ`, `لَيْتَ`, le détail des nombres de 11 à 99, `يَكْتُبُ`, `دَرَسَ`, `يُرِيدُ`, `الزَّوَاجُ`, `الْقَلْبُ`, `أُحِبُّكَ`, `كَثِيرًا`, `الَّذِي`, `عَرَبِيٌّ`, `فَرَنْسِيٌّ`.

## À confirmer avec le professeur

- `عَزَبٌ` (PDF) face à `أَعْزَبُ` : l'app suit le PDF ; le féminin de `عَزَبٌ` n'est pas donné.
- La vocalisation de `هَاشِمٌ` (le PDF met un tanwin dans la marge) : le prénom n'est cité qu'au vocatif (`يَا هَاشِمُ`) ou dans les textes de l'app.
- `بِكَمْ هٰذَا الْكِتَابِ` (PDF, kasra finale) : l'app écrit le nominatif (voir plus haut).
