# Sources et choix de contenu

Ce fichier garde la mémoire du contenu : d'où vient chaque leçon, ce qu'elle enseigne (une leçon
suivante ne peut s'appuyer que sur ce qui est déjà enseigné), et les choix faits quand l'app
s'écarte ou complète les supports du professeur. **À mettre à jour à chaque leçon.**

## Les supports du professeur

PDF en français d'un professeur (cours vidéo sur le Tome de Médine 1), fournis par l'utilisateur.
Ils **ne sont pas dans le dépôt** : le dépôt est public, et l'auteur autorise l'usage personnel mais
interdit la modification et l'usage commercial de ses supports. Pour une nouvelle leçon, demander à
l'utilisateur de joindre les PDF dans la conversation.

Supports utilisés pour les leçons 1 à 3 :
- `TOME1-1-1.pdf` : introduction (nom, verbe, particule) et cours 1, partie 1 (هٰذَا).
- `TOME1-1-2.pdf` : cours 1, partie 2 (interrogatifs أَ et مَا).
- `TOME1-1-3.pdf` : cours 1, partie 3 (مَنْ).
- `TOME1-2.pdf` : cours 2 (ذٰلِكَ, vocabulaire nouveau).
- `TOME1-3.pdf` : cours 3, parties 1 et 2 (indéfini / défini, article, lettres lunaires et solaires).

## Ce qui est déjà enseigné (cumulatif)

**Leçon 1** (`l01`) :
- les trois sortes de mots : nom, verbe, particule (définitions des grammairiens) ;
- le verbe : passé, présent ou futur (المُضَارِع), impératif ;
- هٰذَا (singulier, masculin, proche ; choses et personnes) ;
- pas de verbe « être » dans la phrase nominale ;
- l'alif suscrit (هٰذَا) ;
- les questions avec أَ, مَا (chose, animal), مَنْ (personne) ;
- les réponses نَعَمْ et لَا ;
- le tanwin = « un / une » ;
- les premiers pluriels (vus comme vocabulaire).

**Leçon 2** (`l02`) :
- ذٰلِكَ (éloigné) ;
- وَ (et) ;
- أَذٰلِكَ ؟ ;
- le classement des mots connus : noms interrogatifs et démonstratifs, particules interrogative, de réponse et de coordination ;
- les noms de matière (« du sucre », « du lait »).

**Leçon 3** (`l03`) :
- indéfini (نَكِرَةٌ) et défini (مَعْرِفَةٌ) ;
- le tanwin : son nom, ses trois formes, les noms propres, la pause ;
- l'article الْ, qui fait perdre le tanwin ;
- la phrase « le stylo est cassé » (adjectif attribut) ;
- la hamza de liaison :
  - en début de phrase ;
  - après وَ ;
  - مَنِ devant l'article ;
  - الِاسْمُ ;
  - la différence avec la hamza coupante ;
- les lettres lunaires et solaires (avec la shadda) ;
- le genre des noms :
  - ة marque le plus souvent le féminin, et sa prononciation (t / h) ;
  - les parties du corps qui vont par paire sont féminines pour la plupart.

**Pas encore enseigné** (ne pas l'exiger dans un exercice) :
- هٰذِهِ et les démonstratifs féminins ou pluriels ;
- les pronoms ;
- l'annexion (« le … de … », seulement signalée) ;
- les cas (les terminaisons ◌ُ ◌َ ◌ِ seulement signalées) ;
- l'accord de l'adjectif au féminin ;
- les prépositions ;
- les verbes conjugués.

Le validateur refuse هٰذَا ou ذٰلِكَ devant un nom marqué féminin.

## Choix faits face aux supports

- **Pluriel de مِفْتَاحٌ :** مَفَاتِيحُ en principal, avec la note « on trouve aussi مَفَاتِحُ » (la forme du PDF). Les deux formes sont attestées (Lisān al-ʿArab).
- **سُكَّرٌ « garde la même forme au pluriel » :** précisé comme nom de matière.
- **أَلْبَانٌ :** « des sortes de lait ».
- **Intro, écran « كِتَابَةٌ désigne une action » :**
  - Juste selon le PDF : une action peut être un nom (اسم), et le nom n'indique pas de temps.
  - La raison est enseignée avant la question, et la question est formulée sans piège.
- **Leçon 3, PDF :** il cite هٰذِهِ بِنْتٌ, هُمْ رِجَالٌ et هٰذِهِ كُتُبٌ comme exemples d'indéfini. Ils sont donnés dans une note, sans exercice, puisque هٰذِهِ et هُمْ viennent plus tard.
- **Transcription de هٰذَا الْكِتَابُ :** on prononce « hādha l-kitābu », car le ā devient bref devant l'article.
- **الْمُعَرَّفُ بِأَلْ :** cette forme est retenue. Le PDF l'écrit sans voyelles.
- **Explications ajoutées (données à l'oral, absentes des PDF) :** chacune a été vérifiée par deux relecteurs indépendants. La liste figure dans le message du commit « Ajoute les explications qu'un enseignant donnerait à l'oral ».

## Audio

Aucun MP3 n'est encore généré : l'app utilise la synthèse vocale arabe du téléphone en attendant
(voir `docs/roadmap.md`).
