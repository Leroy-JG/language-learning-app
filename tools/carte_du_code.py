#!/usr/bin/env python3
"""Régénère docs/carte-du-code.md : pour chaque fichier Kotlin, ses déclarations principales avec
le numéro de ligne. Permet de lire seulement la bonne plage (Read offset/limit) au lieu du fichier.
Usage : python3 tools/carte_du_code.py
"""
import pathlib, re

r = pathlib.Path(__file__).resolve().parent.parent
decl = re.compile(r"^(?:(?:private|internal|public|data|sealed|enum|abstract|open|inline)\s+)*"
                  r"(?:class|object|interface|fun|@Composable\s+(?:private\s+|internal\s+)?fun)\s+([\w.<>]+)")
sortie = ["# Carte du code (générée par `tools/carte_du_code.py` — ne pas éditer à la main)\n",
          "Pour lire une fonction : `Read` avec `offset` = ligne et `limit` ≈ 60, plutôt que le fichier entier.",
          "Les fichiers `app/` ne se compilent que sur GitHub Actions.\n"]
for racine in ("core/src/main", "app/src/main/java"):
    for f in sorted((r / racine).rglob("*.kt")):
        lignes = f.read_text(encoding="utf-8").splitlines()
        noms = []
        for i, l in enumerate(lignes, 1):
            if l.startswith(" ") or l.startswith("\t"):
                continue
            s = l.replace("@Composable ", "@Composable ").strip()
            m = decl.match(s) or (re.match(r"^@Composable$", s) and None)
            if s == "@Composable" and i < len(lignes):
                m2 = decl.match(lignes[i].strip())
                if m2: noms.append(f"{m2.group(1)}:{i+1}")
                continue
            if m: noms.append(f"{m.group(1)}:{i}")
        sortie.append(f"- `{f.relative_to(r)}` ({len(lignes)} l.) — " + (", ".join(noms[:40]) or "—"))
(r / "docs/carte-du-code.md").write_text("\n".join(sortie) + "\n", encoding="utf-8")
print("docs/carte-du-code.md écrit")
