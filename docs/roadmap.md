# Feuille de route

## Fait

- **Moteur :** le contenu est en JSON, un validateur fait échouer le build si le contenu est incohérent, et 14 types d'écrans fonctionnent.
  - Parmi eux, 4 mini-jeux : ranger, repérer, écrire lettre par lettre, mémoire.
  - Les astuces ont 7 sortes de blocs, dont comparer, décomposer, galerie et encadrés à ton.
  - Le guide de conception est `docs/pedagogie.md`.
- **Parcours :** le Tome de Médine 1 est complet : chapitres 1 à 23 et deux chapitres de révision (`r01`, libellé « Révision 1 », placé après le chapitre 9 ; `r02`, libellé « Révision 2 », placé à la fin, après le chapitre 23). Les chapitres s'affichent en accordéon et rien n'est verrouillé.
- **Défis (quiz du professeur) :** neuf défis (après les leçons 4, 5, 8, 9, 10, 13, 17, 18, 21) et le grand défi final, notés sur 20 (réussi à 12/20) ; la réussite de l'examen valide le tome. Voir `docs/sources-quiz.md`.
- **Réviser :** répétition espacée FSRS-5, deux onglets (Vocabulaire / Grammaire), niveaux de maîtrise, favoris, recherche.
- **Paramètres :**
  - harakat, translittération, taille de l'arabe ;
  - vitesse de la voix et lecture automatique ;
  - effets sonores et vibrations ;
  - exercices d'écriture et nombre de mots par séance ;
  - thème ;
  - exporter, importer ou réinitialiser la progression.
- **Motivation :** sons, vibrations, série 🔥, confettis, écran de fin avec XP.
- **Durées estimées :** chaque leçon, chaque chapitre et le tome affichent une durée approximative (`TimeEstimate`, calculée d'après le contenu : temps moyen par type d'écran, plus le nombre d'éléments). Les temps par écran sont des estimations pour un débutant, à ajuster d'après l'usage réel.
- **Mots :** genre (masculin / féminin) affiché sous chaque nom.
- **Signature :** clé définitive, APK publié automatiquement à chaque push.

## À faire, par ordre de priorité

1. **Leçons suivantes du Tome de Médine 1**, avec la procédure `.claude/skills/nouvelle-lecon/SKILL.md`. Il faut que l'utilisateur joigne les PDF.
   - Prochaine : le Tome 2 (le tome 1 est terminé : chapitres 1 à 23 et révisions 1 et 2 ; la leçon 18 est décrite dans `docs/sources-lecon-18.md`, les autres dans `docs/sources.md`).
   - À confirmer avec le professeur (leçon 21) : `إِنْكِلْتِرَا` (graphie du livre, avec ك) face à `إِنْجِلْتِرَا`, et la traduction de `الْمَدِينَةُ` par « Médine » dans le texte.
   - À confirmer avec le professeur (leçon 20) : la forme `أُرُوبَّا` (lettres dans cet ordre) sur la carte du PDF, face à `أُورُبَّا` / `أُورُوبَّا` dans l'app (l'exercice 3 de la leçon 21 écrit `أُورُبَّا`, comme l'app).
   - À confirmer avec le professeur (leçon 19) : `قُدَامَى` comme pluriel de `قَدِيمٌ` (attesté dans les dictionnaires modernes, non retrouvé dans Lisān al-ʿArab / al-Wasīṭ).
   - À confirmer avec le professeur (leçon 17, voir `docs/sources-lecon-17.md`) : le pluriel de مَوْعِدٌ (le PDF donne مَوَاعِدُ, l'app met مَوَاعِيدُ), et la voyelle du milieu de الْحَمْزَاتُ / الطَّلْحَاتُ (non repris).
   - À confirmer avec le professeur (voir `docs/sources.md`, leçon 6) : le pluriel de قَهْوَةٌ (قَهَوَاتٌ ou قَهْوَاتٌ), le genre de قِدْرٌ (féminin ici, comme dans le livre), et les exemples du PDF non repris (سَمَاءٌ, مُوسَى ; سِكِّينٌ est présenté au masculin avec une note, leçon 8).
2. **Audio de qualité** avec `tools/generate_audio.py`, à écrire :
   - Lire le manifeste produit par `./gradlew -p core audioManifest` (`core/build/audio-manifest.json` : texte, voix, nom de fichier = empreinte).
   - Générer les MP3 avec edge-tts (voix de `course.json`) dans `app/src/main/assets/audio/ar/`.
   - Produire une page d'écoute HTML pour que l'utilisateur valide chaque audio.
   - Contrainte : `speech.platform.bing.com` est bloqué dans l'environnement cloud. Générer l'audio dans un workflow GitHub Actions, ou demander à l'utilisateur d'autoriser ce domaine.
3. **Sécurité de la progression :**
   - Versionner les schémas Room. La CI les publie déjà en artefact `schemas-0.1.<n>` : les copier dans `app/schemas/`.
   - Ajouter des tests de migration avant toute évolution de `ProgressDatabase` ou de `ReviewDatabase`. Les deux sont en version 1.
4. **Statistiques et série de jours :**
   - Calculer la série à partir de l'historique daté (robuste aux fuseaux horaires).
   - Ajouter un objectif quotidien et un calendrier d'activité.
5. **Rappel quotidien local** : une notification sans Internet, réglable dans les Paramètres.
6. **Tests d'interface** : captures d'écran avec Robolectric et Roborazzi.
