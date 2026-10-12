#!/usr/bin/env python3
"""Liste compacte de tous les mots/phrases déjà enseignés (une ligne par élément).

Remplace la lecture de tous les items.json : python3 tools/deja_enseigne.py
Format : leçon | id | arabe | français | genre | pluriel
"""
import json, pathlib, sys

racine = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/content/ar/medine-t1"
niveau = json.loads((racine / "level.json").read_text(encoding="utf-8"))
for lecon in niveau["chapters"]:
    for e in json.loads((racine / lecon / "items.json").read_text(encoding="utf-8")):
        print(" | ".join([lecon, e["id"], e["ar"], e["fr"], e.get("gender", ""), e.get("plural", "")]))
