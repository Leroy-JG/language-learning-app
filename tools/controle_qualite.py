#!/usr/bin/env python3
"""Contrôles de qualité automatiques (complètent `validateContent`, ne remplacent pas les relecteurs).

Usage : python3 tools/controle_qualite.py [lNN ...]   (par défaut : tous les chapitres)
Signale : activité hors de 6-16 écrans, bilan pas en dernier, mot sans translit/genre,
typographie française (espace avant ? ! : ; », après «), tanwin/ids suspects, mot arabe non vocalisé.
Les avertissements sont à vérifier à la main ; code de sortie 1 s'il y en a.
"""
import json, pathlib, re, sys

racine = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/content/ar/medine-t1"
chapitres = sys.argv[1:] or json.loads((racine / "level.json").read_text(encoding="utf-8"))["chapters"]
HARAKAT = re.compile("[ً-ْٰ]")
LETTRES = re.compile("[ء-ي]")
avert = []

def texte_fr(t, ou):
    for m in re.finditer(r"\S[?!:;»](?=\s|$)", t):
        if t[m.start()] not in "   " and "{ar|" not in t[max(0, m.start()-60):m.start()+1]:
            avert.append(f"{ou}: espace manquante avant « {t[m.start()+1]} » : …{t[max(0,m.start()-15):m.end()+5]}…")
    if re.search(r"«\S|«[^\s  ]", t):
        avert.append(f"{ou}: espace manquante après « : {t[:60]}")

def parcourir(o, ou):
    if isinstance(o, dict):
        for k, v in o.items():
            if k in ("fr", "text", "prompt", "explanation", "note", "body") and isinstance(v, str):
                texte_fr(v, ou)
            parcourir(v, ou)
    elif isinstance(o, list):
        for v in o: parcourir(v, ou)

for c in chapitres:
    d = racine / c
    ch = json.loads((d / "chapter.json").read_text(encoding="utf-8"))
    dernieres = ch["sections"][-1]["activities"]
    # Les chapitres « Quiz N » et « Examen final » (QCM du professeur) n'ont qu'un test noté, sans bilan.
    note = dernieres in (["test"], ["exam"])
    if dernieres != ["quiz"] and not note:
        avert.append(f"{c}: le bilan « quiz » doit être seul, dans la dernière partie")
    for e in json.loads((d / "items.json").read_text(encoding="utf-8")):
        ou = f"{c}/{e['id']}"
        if not e.get("translit"): avert.append(f"{ou}: translit manquante")
        if e["kind"] == "word" and not e["ar"].startswith(("ال",)) and "gender" not in e \
                and not e["id"].startswith(("w:adj", "w:prep", "w:part")):
            pass  # genre des noms : vérifié par les relecteurs (adjectifs/particules n'en ont pas)
        if LETTRES.search(e["ar"]) and not HARAKAT.search(e["ar"]):
            avert.append(f"{ou}: arabe sans aucune voyelle : {e['ar']}")
        parcourir(e, ou)
    for f in sorted(d.glob("*.json")):
        if f.name in ("chapter.json", "items.json"): continue
        a = json.loads(f.read_text(encoding="utf-8"))
        ecrans = a.get("screens", a if isinstance(a, list) else [])
        n = len(ecrans)
        if not 6 <= n <= 30 and f.stem != "quiz" and not note: avert.append(f"{c}/{f.stem}: {n} écrans (cadre : 6 à 16)")
        ids = [x.get("id") for x in ecrans if isinstance(x, dict)]
        if len(ids) != len(set(ids)): avert.append(f"{c}/{f.stem}: ids d'écran en double")
        parcourir(a, f"{c}/{f.stem}")
print("\n".join(avert) or "Aucun avertissement.")
sys.exit(1 if avert else 0)
