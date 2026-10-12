#!/usr/bin/env python3
"""Génère un aperçu HTML lisible d'un cours, pour relire le contenu sans construire l'app.

Usage : python3 tools/preview_content.py app/src/main/assets/content/ar apercu.html
Le format des fichiers est décrit dans docs/content-format.md.
"""
import html
import json
import os
import re
import sys

ICONS = {
    "house": "🏠", "mosque": "🕌", "door": "🚪", "book": "📕", "pen": "🖊️", "key": "🔑",
    "bed": "🛏️", "chair": "🪑", "shirt": "👕", "star": "⭐", "doctor": "🧑‍⚕️", "boy": "👦",
    "man": "👨", "student": "🧑‍🎓", "teacher": "🧑‍🏫", "dog": "🐕", "cat": "🐈",
    "rooster": "🐓", "donkey": "🫏", "horse": "🐎", "camel": "🐪", "rock": "🪨", "milk": "🥛",
    "moon": "🌙", "sun": "☀️", "water": "💧", "bread": "🍞", "meal": "🍽️", "meat": "🍖",
    "fish": "🐟", "flower": "🌸", "eye": "👁️", "mouth": "👄", "hand": "✋",
}
MCQ_MODES = {"ar-fr": "arabe → français", "fr-ar": "français → arabe",
             "audio-ar": "audio → arabe", "icon-ar": "image → arabe"}
MATCH_MODES = {"ar-fr": "arabe ↔ français", "audio-ar": "audio ↔ arabe", "sing-plur": "singulier ↔ pluriel"}
ASK = {"fr": "à partir du français", "audio": "dictée", "question": "répondre à la question"}
ACT_TYPES = {"vocabulary": "Vocabulaire", "grammar": "Grammaire", "dialogue": "Dialogue",
             "review": "Révision", "quiz": "Quiz"}


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def ar(text, cls="ar"):
    return f'<bdi class="{cls}" dir="rtl" lang="ar">{html.escape(text)}</bdi>'


def fr(text):
    """Texte français : balises {ar|…} et **gras**."""
    out, pos = [], 0
    for m in re.finditer(r"\{ar\|([^{}]*)\}", text):
        out.append(html.escape(text[pos:m.start()]))
        out.append(ar(m.group(1)))
        pos = m.end()
    out.append(html.escape(text[pos:]))
    return re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", "".join(out))


def text_obj(o):
    return ar(o["ar"]) if "ar" in o else fr(o["fr"])


def context(sc, screens_by_id):
    c = sc.get("context")
    if not c:
        return ""
    if "icon" in c:
        return f'<div class="ctx"><span class="icon">{ICONS.get(c["icon"], "❓")}</span></div>'
    if "item" in c:
        return f'<div class="ctx">{ar(ITEMS[c["item"]]["ar"])}</div>'
    d = screens_by_id.get(c["dialogue"], {})
    return f'<div class="ctx">🔁 réécouter « {fr(d.get("title", c["dialogue"]))} »</div>'


def common(sc):
    parts = []
    if "instruction" in sc:
        parts.append(f'<div class="instr">{fr(sc["instruction"])}</div>')
    return "".join(parts)


def explain(sc):
    return f'<div class="explain">💡 {fr(sc["explain"])}</div>' if "explain" in sc else ""


def item_line(iid):
    it = ITEMS[iid]
    return f'{ar(it["ar"])} <span class="muted">{fr(it["fr"])}</span>'


def render_screen(sc, screens_by_id):
    t = sc["type"]
    head = f'<div class="stype">{t}</div>'
    body = ""
    if t == "present":
        it = ITEMS[sc["item"]]
        icon = f'<span class="icon">{ICONS.get(it["icon"], "")}</span>' if "icon" in it else ""
        rows = [f'<div class="big">{icon}{ar(it["ar"], "ar big-ar")}</div>',
                f'<div class="tr">{fr(it["fr"])}'
                + (f' · <i>{html.escape(it["translit"])}</i>' if "translit" in it else "") + "</div>"]
        if "plural" in it:
            rows.append(f'<div>Pluriel : {ar(it["plural"])}</div>')
        if "note" in it:
            rows.append(f'<div class="muted">{fr(it["note"])}</div>')
        if "example" in it:
            rows.append(f'<div class="ex">🔊 {ar(it["example"]["ar"])} — {fr(it["example"]["fr"])}</div>')
        body = "".join(rows)
    elif t == "tip":
        parts = [f'<h4>{fr(sc["title"])}</h4>']
        for b in sc["blocks"]:
            if b["type"] == "paragraph":
                parts.append(f"<p>{fr(b['text'])}</p>")
            elif b["type"] == "note":
                parts.append(f'<p class="note">{fr(b["text"])}</p>')
            elif b["type"] == "example":
                parts.append(f'<p class="ex">🔊 {ar(b["ar"])} — {fr(b["fr"])}</p>')
            elif b["type"] == "table":
                h = "".join(f"<th>{fr(c)}</th>" for c in b["header"])
                r = "".join("<tr>" + "".join(f"<td>{fr(c)}</td>" for c in row) + "</tr>" for row in b["rows"])
                parts.append(f"<table><thead><tr>{h}</tr></thead><tbody>{r}</tbody></table>")
        body = "".join(parts)
    elif t == "mcq":
        d = sc.get("distractors")
        dist = ", ".join(item_line(x) for x in d) if d else '<span class="muted">choisis automatiquement</span>'
        body = (f'<div>QCM {MCQ_MODES[sc["mode"]]}</div><div>✅ {item_line(sc["item"])}</div>'
                f'<div>Pièges : {dist}</div>')
    elif t == "match":
        rows = []
        for iid in sc["items"]:
            it = ITEMS[iid]
            right = ar(it["plural"]) if sc["mode"] == "sing-plur" else fr(it["fr"])
            rows.append(f'<li>{ar(it["ar"])} ↔ {right}</li>')
        body = f'<div>Associer : {MATCH_MODES[sc["mode"]]}</div><ul>{"".join(rows)}</ul>'
    elif t == "fill_gap":
        s = html.escape(sc["sentence"])
        s = re.sub(r"\{(\d+)\}", lambda m: f"<u>…{int(m.group(1)) + 1}…</u>", s)
        answers = " ; ".join(f"{i + 1} = " + " / ".join(ar(a) for a in g["answers"]) for i, g in enumerate(sc["gaps"]))
        body = (f'<div class="big"><bdi class="ar" dir="rtl" lang="ar">{s}</bdi></div>'
                f'<div class="muted">{fr(sc["fr"])}</div>'
                f'<div>Étiquettes : {" · ".join(ar(o) for o in sc["options"])}</div><div>✅ {answers}</div>')
    elif t == "order":
        sols = "<br>".join(ar(" ".join(a) + sc.get("punctuation", "")) for a in sc["answers"])
        dist = sc.get("distractors")
        body = (f'<div>Remettre dans l\'ordre : {fr(sc["fr"])}</div><div>✅ {sols}</div>'
                + (f'<div>Pièges : {" · ".join(ar(x) for x in dist)}</div>' if dist else ""))
    elif t == "typing":
        if "item" in sc:
            expected, prompt = [ITEMS[sc["item"]]["ar"]], fr(ITEMS[sc["item"]]["fr"])
        else:
            expected, prompt = sc["answers"], fr(sc["fr"])
        shown = {"fr": prompt, "audio": "🔊 (on entend la réponse)",
                 "question": ar(sc.get("question", ""))}[sc["ask"]]
        body = (f'<div>Écrire en arabe ({ASK[sc["ask"]]}) : {shown}</div>'
                f'<div>✅ {" / ".join(ar(a) for a in expected)}</div>'
                + (f'<div class="muted">{prompt}</div>' if sc["ask"] != "fr" else ""))
    elif t == "true_false":
        st = sc["statement"]
        stxt = f'{ar(st["ar"])} <span class="muted">({fr(st["fr"])})</span>' if "ar" in st else fr(st["fr"])
        body = f'<div>Vrai ou faux : {stxt}</div><div>✅ {"Vrai" if sc["answer"] else "Faux"}</div>'
    elif t == "question":
        ch = "".join(f'<li>{"✅ " if i == sc["answer"] else ""}{text_obj(c)}</li>' for i, c in enumerate(sc["choices"]))
        body = f'<div>{text_obj(sc["prompt"])}</div><ul>{ch}</ul>'
    elif t == "dialogue":
        sp = sc["speakers"]
        lines = "".join(
            f'<tr><td class="who">{html.escape(sp[ln["speaker"]]["name"])}</td><td>{ar(ln["ar"])}</td>'
            f'<td class="muted">{fr(ln["fr"])}</td></tr>' for ln in sc["lines"])
        body = (f'<h4>{fr(sc["title"])}</h4>' + (f'<p class="muted">{fr(sc["intro"])}</p>' if "intro" in sc else "")
                + f"<table>{lines}</table>")
    return (f'<div class="screen">{head}{common(sc)}{context(sc, screens_by_id)}{body}{explain(sc)}'
            f'<div class="sid">{html.escape(sc["id"])}</div></div>')


ITEMS = {}


def main(root, out_path):
    course = load(os.path.join(root, "course.json"))
    parts = []
    for level_dir in course["levels"]:
        level = load(os.path.join(root, level_dir, "level.json"))
        parts.append(f'<h1>{fr(level["title"])}</h1>')
        for ch_dir in level["chapters"]:
            croot = os.path.join(root, level_dir, ch_dir)
            ch = load(os.path.join(croot, "chapter.json"))
            for it in load(os.path.join(croot, "items.json")):
                ITEMS[it["id"]] = it
            parts.append(f'<h2>Chapitre {ch["number"]} — {ar(ch["title"]["ar"])} · {fr(ch["title"]["fr"])}</h2>')
            if "description" in ch:
                parts.append(f'<p class="muted">{fr(ch["description"])}</p>')
            for sec in ch["sections"]:
                parts.append(f'<h3>{fr(sec["title"])}</h3>')
                for code in sec["activities"]:
                    act = load(os.path.join(croot, code + ".json"))
                    by_id = {s["id"]: s for s in act["screens"]}
                    screens = "".join(render_screen(s, by_id) for s in act["screens"])
                    parts.append(
                        f'<details class="act"><summary><span class="badge {act["type"]}">'
                        f'{ACT_TYPES[act["type"]]}</span> {fr(act["title"])} '
                        f'<span class="muted">({len(act["screens"])} écrans)</span></summary>{screens}</details>')
    page = TEMPLATE.replace("{{BODY}}", "\n".join(parts))
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(page)


TEMPLATE = """<!doctype html>
<html lang="fr"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Aperçu du cours</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Naskh+Arabic:wght@400;700&display=swap">
<style>
:root { --bg:#f7f7f5; --card:#fff; --fg:#1d1d1f; --muted:#6b6b70; --line:#e3e3e0; --accent:#1a7f5a; --tip:#fff7e0; }
@media (prefers-color-scheme: dark) { :root { --bg:#141416; --card:#1e1e21; --fg:#ececee; --muted:#a0a0a8; --line:#33333a; --accent:#4cc48f; --tip:#2b2618; } }
* { box-sizing:border-box; }
body { margin:0; padding:16px; background:var(--bg); color:var(--fg); font:16px/1.5 system-ui, sans-serif; max-width:860px; margin-inline:auto; }
.ar { font-family:"Noto Naskh Arabic", "Amiri", serif; font-size:1.3em; line-height:2; }
.big-ar { font-size:2.2em; }
h1 { font-size:1.6em; } h2 { font-size:1.3em; margin-top:2em; } h3 { margin-top:1.6em; color:var(--accent); }
.act { background:var(--card); border:1px solid var(--line); border-radius:12px; margin:10px 0; padding:4px 14px; }
.act summary { cursor:pointer; padding:10px 0; font-weight:600; }
.badge { font-size:.75em; padding:2px 8px; border-radius:99px; background:var(--line); margin-right:6px; }
.badge.grammar { background:#e8e0ff; color:#3d2a8a; } .badge.vocabulary { background:#dff3ea; color:#14563d; }
.badge.dialogue { background:#ffe6d5; color:#7a3a0c; } .badge.quiz { background:#ffe0e6; color:#86132e; }
.screen { border-top:1px solid var(--line); padding:12px 0; position:relative; }
.stype { font-size:.75em; text-transform:uppercase; letter-spacing:.06em; color:var(--muted); }
.sid { font-size:.7em; color:var(--muted); text-align:right; }
.muted { color:var(--muted); } .instr { font-style:italic; } .note { background:var(--tip); padding:8px 12px; border-radius:8px; }
.explain { margin-top:6px; background:var(--tip); padding:6px 10px; border-radius:8px; }
.ctx { margin:6px 0; } .icon { font-size:2em; margin-inline-end:10px; }
.big { margin:4px 0; } .tr { font-weight:600; } .ex { margin-top:4px; }
table { border-collapse:collapse; margin:8px 0; width:100%; } td, th { border:1px solid var(--line); padding:4px 8px; text-align:start; }
.who { font-weight:600; white-space:nowrap; } ul { margin:4px 0; padding-inline-start:22px; }
</style></head><body>
<p class="muted">Aperçu généré depuis les fichiers du cours. Ouvre une activité pour voir ses écrans ; ✅ indique la bonne réponse.</p>
{{BODY}}
</body></html>
"""

if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit("Usage : preview_content.py <dossier du cours> <fichier html de sortie>")
    main(sys.argv[1], sys.argv[2])
