# Feuille de route

## Fait

- **Moteur :** le contenu est en JSON, un validateur fait échouer le build si le contenu est incohérent, et 14 types d'écrans fonctionnent.
  - Parmi eux, 4 mini-jeux : ranger, repérer, écrire lettre par lettre, mémoire.
  - Les astuces ont 7 sortes de blocs, dont comparer, décomposer, galerie et encadrés à ton.
  - Le guide de conception est `docs/pedagogie.md`.
- **Parcours :** chapitres 1 à 5 du Tome de Médine 1. Les chapitres s'affichent en accordéon et rien n'est verrouillé.
- **Réviser :** répétition espacée FSRS-5, deux onglets (Vocabulaire / Grammaire), niveaux de maîtrise, favoris, recherche.
- **Paramètres :**
  - harakat, translittération, taille de l'arabe ;
  - vitesse de la voix et lecture automatique ;
  - effets sonores et vibrations ;
  - exercices d'écriture et nombre de mots par séance ;
  - thème ;
  - exporter, importer ou réinitialiser la progression.
- **Motivation :** sons, vibrations, série 🔥, confettis, écran de fin avec XP.
- **Mots :** genre (masculin / féminin) affiché sous chaque nom.
- **Signature :** clé définitive, APK publié automatiquement à chaque push.

## À faire, par ordre de priorité

1. **Leçons suivantes du Tome de Médine 1**, avec la procédure `.claude/skills/nouvelle-lecon/SKILL.md`. Il faut que l'utilisateur joigne les PDF.
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
