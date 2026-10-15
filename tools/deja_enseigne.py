#!/usr/bin/env python3
"""Liste compacte de tous les mots/phrases déjà enseignés (une ligne par élément), tous niveaux confondus.

Remplace la lecture de tous les items.json : python3 tools/deja_enseigne.py
Format : leçon | id | arabe | français | genre | pluriel   (la leçon est préfixée du niveau pour le tome 2 et suivants :
`t2/l01`).
"""
import json, pathlib, sys

contenu = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/content/ar"
cours = json.loads((contenu / "course.json").read_text(encoding="utf-8"))
for nom_niveau in cours["levels"]:
    racine = contenu / nom_niveau
    niveau = json.loads((racine / "level.json").read_text(encoding="utf-8"))
    prefixe = "" if nom_niveau == "medine-t1" else nom_niveau.replace("medine-", "") + "/"
    for lecon in niveau["chapters"]:
        fichier = racine / lecon / "items.json"
        if not fichier.exists():
            continue
        for e in json.loads(fichier.read_text(encoding="utf-8")):
            print(" | ".join([prefixe + lecon, e["id"], e["ar"], e["fr"], e.get("gender", ""), e.get("plural", "")]))
