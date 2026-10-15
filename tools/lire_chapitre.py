"""Affiche lisiblement les activités d'un chapitre (pour la relecture) :
python3 tools/lire_chapitre.py 06 grammar-1.json practice-2.json   (depuis la racine du dépôt ; `r01` pour un chapitre de révision ; LURATA_NIVEAU=medine-t2 pour le tome 2)."""
import json,sys,pathlib,re
_d=sys.argv.pop(1)
import os
root=pathlib.Path(__file__).resolve().parent.parent/('app/src/main/assets/content/ar/'+os.environ.get('LURATA_NIVEAU','medine-t1')+'/'+(_d if _d[0].isalpha() else 'l'+_d))
items={e['id']:e for e in json.load(open(root/'items.json',encoding='utf-8'))}
def t(x): return re.sub(r'\{ar\|([^}]*)\}',r'«\1»',x) if isinstance(x,str) else x
def blk(b):
    k=b['type']
    if k=='paragraph': return t(b['text'])
    if k=='example': return f"EX {b['ar']} = {b['fr']}"
    if k=='table': return 'TABLE '+' | '.join(b['header'])+'\n      '+'\n      '.join(' | '.join(t(c) for c in r) for r in b['rows'])
    if k=='note': return f"NOTE[{b.get('tone','info')}] {t(b['text'])}"
    if k=='compare': return 'COMPARE '+'; '.join(f"{r['from']['ar']} ({r['from']['fr']}) -> {r['to']['ar']} ({r['to']['fr']})"+(f" [{r['label']}]" if 'label' in r else '') for r in b['rows'])
    if k=='breakdown': return f"BREAK {b['ar']} = {b['fr']} :: "+' + '.join(f"{p['ar']}({p['fr']})" for p in b['parts'])
    return str(b)
def scr(s):
    k=s['type']; i=s['id'].split('#')[1]
    pre=f"  #{i} {k}"
    if k=='present': 
        e=items[s['item']]; return f"{pre} {e['ar']} = {e['fr']} [{e.get('gender','')}] pl={e.get('plural','')}"
    if k=='tip': return f"{pre} «{t(s['title'])}»\n    "+'\n    '.join(blk(b) for b in s['blocks'])
    d={kk:v for kk,v in s.items() if kk not in('id','type')}
    return f"{pre} "+json.dumps(d,ensure_ascii=False)
for f in sys.argv[1:]:
    a=json.load(open(root/f,encoding='utf-8'))
    print('=====',a['id'],a['title'])
    for s in a['screens']: print(scr(s))
