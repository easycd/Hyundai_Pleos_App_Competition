# -*- coding: utf-8 -*-
"""Consistency checks for the 화면 설계서.
usage: python3 check_screens.py <docs dir> <wire dir> [layout.json]
exit 1 when anything is wrong. Prints one line per problem.
"""
import os, sys, re, json

DOCS = sys.argv[1]; WIRE = sys.argv[2]
LAYOUT = sys.argv[3] if len(sys.argv) > 3 else None
data = json.load(open(os.path.join(WIRE, 'screens.json'), encoding='utf-8'))
screens = data['screens']; ids = [s['id'] for s in screens]; idset = set(ids)
problems = []
def bad(msg): problems.append(msg)

# 1. ids unique, groups known
if len(ids) != len(idset): bad('중복 ID: ' + ', '.join(i for i in ids if ids.count(i) > 1))
gids = {g[0] for g in data['groups']}
for s in screens:
    if s['group'] not in gids: bad(f"{s['id']}: 모르는 묶음 {s['group']}")

# 2. callouts in wire == spec numbers
for s in screens:
    wire = open(os.path.join(WIRE, f"{s['id']}.html"), encoding='utf-8').read()
    cos = re.findall(r'data-co="([^"]+)"', wire)
    if len(cos) != len(set(cos)): bad(f"{s['id']}: 번호 중복 {sorted(set(c for c in cos if cos.count(c) > 1))}")
    spec = [r[0] for r in s['spec']]
    if set(cos) != set(spec): bad(f"{s['id']}: 그림 번호 {sorted(set(cos))} ≠ 표 번호 {sorted(set(spec))}")
    nums = sorted(int(c) for c in spec if c.isdigit())
    if nums != list(range(1, len(nums) + 1)): bad(f"{s['id']}: 번호가 1부터 이어지지 않음 {nums}")
    if not s['motion']: bad(f"{s['id']}: 모션 표 없음")
    for row in s['spec'] + s['motion']:
        for cell in row:
            if re.search(r'TODO|TBD|xxx|\?\?', str(cell)): bad(f"{s['id']}: 임시 글자 '{cell}'")
    if not s['when'] or not s['entry'] or not s['funcs']: bad(f"{s['id']}: 상황/들어오는 길/기능 비어 있음")
    if '이모지' not in wire and re.search(r'[\U0001F300-\U0001FAFF]', wire): bad(f"{s['id']}: 이모지 사용")

# 3. every S-xx mentioned anywhere must exist
texts = []
for s in screens:
    texts += [s['entry'], s['note']] + [c for r in s['spec'] for c in r] + [c for r in s['motion'] for c in r]
md = open(os.path.join(DOCS, '02_화면설계서.md'), encoding='utf-8').read()
html_ = open(os.path.join(DOCS, '02_화면설계서.html'), encoding='utf-8').read()
texts += [md]
for t in texts:
    for m in set(re.findall(r'S-\d\d[a-d]?', t)):
        if m not in idset: bad(f"없는 화면을 가리킴: {m}")

# 4. feature coverage F-01..F-15 (F-02 위치 버퍼 is background; F-16+ out of scope)
funcs = ' '.join(s['funcs'] for s in screens)
for i in range(1, 16):
    f = f'F-{i:02d}'
    if f not in funcs: bad(f"{f}: 어느 화면에도 없음")

# 5. flow: nodes exist, every screen appears in some flow, edges reference nodes
flow_nodes = set()
for gid, f in data['flows'].items():
    for n in f['nodes']:
        if n != 'NAV' and n not in idset: bad(f"흐름 {gid}: 없는 화면 {n}")
        flow_nodes.add(n)
    for a, b, l in f['edges']:
        if a not in f['nodes'] or b not in f['nodes']: bad(f"흐름 {gid}: 간선 {a}→{b} 의 상자가 없음")
for i in idset:
    if i not in flow_nodes: bad(f"{i}: 흐름도에 없음")

# 6. files: png per screen, gif per anim screen, md/html mention each screen
for s in screens:
    if not os.path.exists(os.path.join(DOCS, 'screens', f"{s['id']}.png")): bad(f"{s['id']}: PNG 없음")
    if s['gif'] and not os.path.exists(os.path.join(DOCS, 'screens', 'anim', f"{s['id']}.gif")): bad(f"{s['id']}: GIF 없음")
    if f"#### {s['id']} " not in md: bad(f"{s['id']}: md에 제목 없음")
    if f'id="{s["id"]}"' not in html_: bad(f"{s['id']}: html에 anchor 없음")
for m in re.findall(r'\]\((screens/[^)]+)\)', md):
    if not os.path.exists(os.path.join(DOCS, m)): bad(f"md가 없는 파일을 가리킴: {m}")
# stale pngs
for f in os.listdir(os.path.join(DOCS, 'screens')):
    if f.endswith('.png') and f[:-4] not in idset: bad(f"쓰지 않는 PNG: screens/{f}")

# 7. layout report from check_layout.js
if LAYOUT and os.path.exists(LAYOUT):
    for sid, issues in json.load(open(LAYOUT, encoding='utf-8')).items():
        for it in issues: bad(f"{sid}: {it}")

print(f"screens {len(ids)} · problems {len(problems)}")
for p in problems: print(' -', p)
sys.exit(1 if problems else 0)
