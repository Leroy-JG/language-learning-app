"""Générateur de contenu : écrire un chapitre en Python au lieu de JSON à la main.

Usage (voir .claude/skills/nouvelle-lecon/SKILL.md) : écrire un script `mon_chapitre.py` HORS du dépôt qui
fait `import sys; sys.path.insert(0, "<dépôt>/tools"); from generateur_contenu import *`, définit les
mots avec `item(...)`, écrit `activity("grammar-1", "grammar", "Titre", "icone", [ ...écrans... ])` pour
chaque activité (ids d'écran numérotés automatiquement : `#1`, `#2`…) et `write("items.json", [...])`.
Le numéro du chapitre vient de la variable d'environnement LURATA_CHAPITRE (ex. `LURATA_CHAPITRE=07`).
Les espaces avant ? ! : ; » et après « deviennent des espaces insécables, et le JSON est mis en forme
comme celui des chapitres existants. Le chapitre 6 a été écrit ainsi (modèle : docs/sources.md, leçon 6).
Après génération : `./gradlew -p core validateContent` puis `python3 tools/controle_qualite.py lNN`.
"""
import os
import json, pathlib, re

_N = int(os.environ.get("LURATA_CHAPITRE", "7"))
CH = f"medine-t1.l{_N:02d}"
OUT = pathlib.Path(__file__).resolve().parent.parent / f"app/src/main/assets/content/ar/medine-t1/l{_N:02d}"
NB = " "

def fr(s):
    """Typographie française : espace insécable avant ? ! : ; » et après «."""
    s = re.sub(r" ([?!:;»])", NB + r"\1", s)
    s = re.sub(r"« ", "«" + NB, s)
    return s

def conv(o, key=None):
    if isinstance(o, str):
        return o if key in ("ar", "id", "sentence", "answers", "words", "options", "distractors") else fr(o)
    if isinstance(o, list):
        return [conv(x, key) for x in o]
    if isinstance(o, dict):
        return {k: conv(v, k) for k, v in o.items()}
    return o

def one(o):
    if isinstance(o, dict):
        return "{ " + ", ".join(f"{json.dumps(k, ensure_ascii=False)}: {one(v)}" for k, v in o.items()) + " }"
    if isinstance(o, list):
        inner = ", ".join(one(x) for x in o)
        return "[" + (" " + inner + " " if any(isinstance(x, dict) for x in o) else inner) + "]"
    return json.dumps(o, ensure_ascii=False)

def dump(o, ind=0, width=116):
    pad = "  " * ind
    s = one(o)
    if len(pad) + len(s) <= width or not isinstance(o, (dict, list)):
        return s
    if isinstance(o, list):
        return "[\n" + ",\n".join("  " * (ind + 1) + dump(x, ind + 1, width) for x in o) + "\n" + pad + "]"
    return "{\n" + ",\n".join(f"{'  ' * (ind + 1)}{json.dumps(k, ensure_ascii=False)}: {dump(v, ind + 1, width)}" for k, v in o.items()) + "\n" + pad + "}"

def write(name, obj):
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_text(dump(conv(obj)) + "\n", encoding="utf-8")

# ---------- blocs d'astuce ----------
def P(text): return {"type": "paragraph", "text": text}
def EX(ar, fr_): return {"type": "example", "ar": ar, "fr": fr_}
def TB(header, rows): return {"type": "table", "header": header, "rows": rows}
def NOTE(text, tone=None):
    b = {"type": "note"}
    if tone: b["tone"] = tone
    b["text"] = text
    return b
def CMP(rows, title=None):
    out = []
    for r in rows:
        row = {"from": {"ar": r[0], "fr": r[1]}, "to": {"ar": r[2], "fr": r[3]}}
        if len(r) > 4: row["label"] = r[4]
        out.append(row)
    b = {"type": "compare"}
    if title: b["title"] = title
    b["rows"] = out
    return b
def BRK(ar, fr_, parts): return {"type": "breakdown", "ar": ar, "fr": fr_, "parts": [{"ar": a, "fr": f} for a, f in parts]}
def GAL(items, title=None):
    b = {"type": "gallery"}
    if title: b["title"] = title
    b["items"] = items
    return b

# ---------- écrans ----------
def tip(title, *blocks): return {"type": "tip", "title": title, "blocks": list(blocks)}
def present(item): return {"type": "present", "item": item}

def _extra(d, instruction=None, context=None, explain=None):
    if instruction: d["instruction"] = instruction
    if context: d["context"] = context
    if explain: d["explain"] = explain
    return d

def mcq(mode, item, distractors=None, instruction=None, explain=None):
    d = {"type": "mcq", "mode": mode, "item": item}
    if distractors: d["distractors"] = distractors
    return _extra(d, instruction, None, explain)

def match(mode, items, instruction=None):
    return _extra({"type": "match", "mode": mode, "items": items}, instruction)

def fill(sentence, fr_, answers, options, context=None, explain=None, instruction=None):
    gaps = answers if isinstance(answers[0], list) else [answers]
    d = {"type": "fill_gap", "sentence": sentence, "fr": fr_, "gaps": [{"answers": g} for g in gaps], "options": options}
    return _extra(d, instruction, context, explain)

def order(fr_, answers, punct=None, distractors=None, explain=None, instruction=None):
    d = {"type": "order", "fr": fr_, "answers": answers}
    if punct: d["punctuation"] = punct
    if distractors: d["distractors"] = distractors
    return _extra(d, instruction, None, explain)

def typ_item(item): return {"type": "typing", "ask": "fr", "item": item}
def typ_fr(fr_, answers, explain=None): return _extra({"type": "typing", "ask": "fr", "fr": fr_, "answers": answers}, None, None, explain)
def typ_audio(fr_, answers, explain=None): return _extra({"type": "typing", "ask": "audio", "fr": fr_, "answers": answers}, None, None, explain)
def typ_q(question, answers, fr_, context=None, instruction=None, explain=None):
    d = {"type": "typing", "ask": "question", "question": question, "answers": answers, "fr": fr_}
    return _extra(d, instruction, context, explain)

def tf(statement, answer, context=None, explain=None):
    st = {"fr": statement} if isinstance(statement, str) else {"ar": statement[0], "fr": statement[1]}
    return _extra({"type": "true_false", "statement": st, "answer": answer}, None, context, explain)

def _choice(c):
    if isinstance(c, dict): return c
    if isinstance(c, tuple): return {"ar": c[1]}
    return {"fr": c}

def qst(prompt, choices, answer, context=None, explain=None, shuffle=True, instruction=None):
    pr = {"fr": prompt} if isinstance(prompt, str) else prompt
    d = {"type": "question", "prompt": pr, "choices": [_choice(c) for c in choices], "answer": answer}
    if not shuffle: d["shuffle"] = False
    return _extra(d, instruction, context, explain)

def _cat(c):
    return {"ar": c[1]} if isinstance(c, tuple) else {"fr": c}

def sort(instruction, categories, cards, explain=None):
    cats = [_cat(c) for c in categories]
    cs = []
    for c in cards:
        if c[0] == "item": cs.append({"item": c[1], "category": c[2]})
        elif c[0] == "ar": cs.append({"ar": c[1], "category": c[2]})
        else: cs.append({"fr": c[1], "category": c[2]})
    return _extra({"type": "sort", "categories": cats, "cards": cs}, instruction, None, explain)

def spot(instruction, words, answers, fr_=None, explain=None):
    d = {"type": "spot", "instruction": instruction, "words": words, "answers": answers}
    if fr_: d["fr"] = fr_
    if explain: d["explain"] = explain
    return d

def build(item, extra=None):
    d = {"type": "build", "item": item}
    if extra: d["extra"] = extra
    return d

def memory(mode, items): return {"type": "memory", "mode": mode, "items": items}

def dialogue(title, intro, speakers, lines):
    return {"type": "dialogue", "title": title, "intro": intro,
            "speakers": {k: {"name": n, "voice": v} for k, (n, v) in speakers.items()},
            "lines": [{"speaker": s, "ar": a, "fr": f} for s, a, f in lines]}

def activity(name, type_, title, icon, screens):
    out = []
    for i, s in enumerate(screens, 1):
        code = s.pop("_code", str(i))
        d = {"id": f"{CH}.{name}#{code}"}
        d.update(s)
        out.append(d)
    obj = {"id": f"{CH}.{name}", "type": type_, "title": title}
    if icon: obj["icon"] = icon
    obj["screens"] = out
    write(f"{name}.json", obj)
    return obj

def item(id_, ar, fr_, translit, gender=None, plural=None, icon=None, note=None, example=None):
    kind = "phrase" if id_.startswith("p:") else "word"
    d = {"id": id_, "kind": kind, "ar": ar, "fr": fr_, "translit": translit}
    if plural: d["plural"] = plural
    if icon: d["icon"] = icon
    if gender: d["gender"] = gender
    if note: d["note"] = note
    if example: d["example"] = {"ar": example[0], "fr": example[1]}
    return d
