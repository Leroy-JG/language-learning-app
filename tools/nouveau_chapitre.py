#!/usr/bin/env python3
"""Crée le squelette d'un chapitre : python3 tools/nouveau_chapitre.py 5 "Titre français" "الدَّرْسُ الْخَامِسُ"

Crée medine-t1/l05/ (chapter.json avec un bilan, items.json vide) et l'ajoute à level.json.
Les activités (grammar-N.json, vocab-N.json…) restent à écrire : voir docs/content-format.md §8-9
et copier un fichier du chapitre précédent comme modèle.
"""
import json, pathlib, sys

if len(sys.argv) != 4:
    sys.exit(__doc__)
n, titre_fr, titre_ar = int(sys.argv[1]), sys.argv[2], sys.argv[3]
racine = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/content/ar/medine-t1"
nom = f"l{n:02d}"
dossier = racine / nom
if dossier.exists():
    sys.exit(f"{dossier} existe déjà")
dossier.mkdir()
chapitre = {
    "id": f"medine-t1.{nom}", "number": n,
    "title": {"ar": titre_ar, "fr": titre_fr},
    "description": "À écrire.", "source": f"Tome de Médine 1, leçon {n}",
    "sections": [{"title": "À écrire", "activities": ["grammar-1", "vocab-1"]},
                 {"title": "Bilan du chapitre", "activities": ["quiz"]}],
}
(dossier / "chapter.json").write_text(json.dumps(chapitre, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
(dossier / "items.json").write_text("[]\n", encoding="utf-8")
niveau_f = racine / "level.json"
niveau = json.loads(niveau_f.read_text(encoding="utf-8"))
niveau["chapters"].append(nom)
niveau_f.write_text(json.dumps(niveau, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("Créé", dossier)
