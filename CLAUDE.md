# Lurata — règles du projet

## Règle n° 1 : le contenu doit être juste à 100 %

C'est une application d'apprentissage : une seule erreur dans un cours (harakat, terminaison,
traduction, explication, bonne réponse d'un exercice, distracteur qui serait aussi juste) apprend
une faute à l'utilisateur. Aucune approximation n'est acceptable.

Pour tout ajout ou modification de contenu (`app/src/main/assets/content/`) :
- Vérifier chaque mot arabe lettre par lettre : harakat, shadda, tanwin, sukun, hamza, alif waṣla,
  tāʾ marbūṭa, cas grammatical selon la fonction dans la phrase, accord, pluriels.
- Vérifier chaque phrase française : sens exact, défini/indéfini identique à l'arabe, orthographe.
- Chaque explication doit être vraie, sans simplification qui la rendrait fausse.
- Chaque exercice : la réponse désignée est juste, aucune autre option n'est acceptable (y compris
  harakat masquées), et rien n'exige une notion pas encore enseignée à ce point du parcours.
- Rester fidèle aux supports du professeur (PDF des Tomes de Médine fournis par l'utilisateur).
- En cas de doute sur un point de langue : ne pas l'écrire, ou le signaler à l'utilisateur.
- Faire relire le contenu par des relecteurs indépendants (sous-agents) et vérifier soi-même chaque
  remarque avant de corriger. `./gradlew -p core test validateContent` doit passer.

## Autres règles

- Format du contenu : `docs/content-format.md` (ids stables, jamais renommés ni réutilisés).
- Interface et messages en français. L'app est 100 % hors ligne (pas de permission Internet).
- La progression de l'utilisateur ne doit jamais être perdue : pas de migration destructive,
  même clé de signature pour chaque version.
- Le SDK Android n'est pas accessible dans l'environnement cloud : l'app se compile uniquement sur
  GitHub Actions. `core/` (Kotlin pur) se compile et se teste localement.
- À chaque version publiée, donner à l'utilisateur le lien direct de l'APK
  (`https://github.com/Leroy-JG/language-learning-app/releases/download/v0.1.<n>/lurata.apk`).
