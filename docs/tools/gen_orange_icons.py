import re,os,sys
MIRROR=len(sys.argv)>2 and sys.argv[2]=='mirror'
OFF=10 if MIRROR else 0
NAME='OrangeTenMirror' if MIRROR else 'OrangeTen'
SHEET='주황_시안10_반전' if MIRROR else '주황_시안10'
SVGDIR='orange-ten-mirror' if MIRROR else 'orange-ten'
TITLE='주황 배경 시안 10 · 자리 바꿈' if MIRROR else '주황 배경 시안 10'
OR='#E4572E'; W='#FFFFFF'
PIN='M50 90C50 90 20 60 20 41a30 30 0 1 1 60 0C80 60 50 90 50 90Z'
RPIN='M50 88C50 88 18 62 18 42a32 32 0 1 1 64 0C82 62 50 88 50 88Z'
SPOON='M0 -31C8.5 -31 12.5 -22 12.5 -13C12.5 -6.5 9.5 -2 6 0C4.4 0.9 3.3 1.8 3.2 3.4L3.8 27.5A3.8 3.8 0 0 1 -3.8 27.5L-3.2 3.4C-3.3 1.8 -4.4 0.9 -6 0C-9.5 -2 -12.5 -6.5 -12.5 -13C-12.5 -22 -8.5 -31 0 -31Z'
STICKS='M-3.75 -27.5v55M3.75 -27.5v55'
def spoon(mark,bg=None):
    extra=f' stroke="{bg}" stroke-width="3" stroke-linejoin="round" paint-order="stroke"' if bg else ''
    return f'<path transform="rotate({38 if MIRROR else -38})" d="{SPOON}" fill="{mark}"{extra}/>'
def sticks(mark,bg=None):
    halo=f'<path d="{STICKS}" stroke="{bg}" stroke-width="11.25"/>' if bg else ''
    return f'<g transform="rotate({-38 if MIRROR else 38})" fill="none" stroke-linecap="round">{halo}<path d="{STICKS}" stroke="{mark}" stroke-width="6.25"/></g>'
def mark(front,mark,bg,cx,cy,s):
    inner = spoon(mark)+sticks(mark,bg) if front=='sticks' else sticks(mark)+spoon(mark,bg)
    return f'<g transform="translate({cx} {cy}) scale({s})">{inner}</g>'
def col(kind,front):
    if kind=='line':   return f'<path d="{PIN}" fill="none" stroke="{W}" stroke-width="7" stroke-linejoin="round"/>'+mark(front,W,OR,50,43,0.8)
    if kind=='rline':  return f'<path d="{RPIN}" fill="none" stroke="{W}" stroke-width="8" stroke-linejoin="round"/>'+mark(front,W,OR,50,44,0.84)
    if kind=='fill':   return f'<path d="{PIN}" fill="{W}" stroke="{W}" stroke-width="4" stroke-linejoin="round"/>'+mark(front,OR,W,50,43,0.8)
    if kind=='rfill':  return f'<path d="{RPIN}" fill="{W}" stroke="{W}" stroke-width="3" stroke-linejoin="round"/>'+mark(front,OR,W,50,44,0.84)
    if kind=='badge':  return f'<path d="{PIN}" fill="{W}" stroke="{W}" stroke-width="4" stroke-linejoin="round"/><circle cx="50" cy="41" r="23" fill="{OR}"/>'+mark(front,W,OR,50,41,0.62)
cols=[('line','1'),('rline','2'),('fill','3'),('rfill','4'),('badge','5')]
heads=['① 흰 테두리 · 안 주황','② 흰 테두리 · 둥근 굵은 핀','③ 흰 핀 · 주황 도안','④ 흰 핀 · 둥근 핀','⑤ 흰 핀 · 주황 원 배지']
rows=[('sticks','A','A 젓가락 앞','숟가락 위로 젓가락이 지나감' if not MIRROR else '숟가락과 젓가락 자리를 서로 바꿈'),('spoon','B','B 숟가락 앞','젓가락 위로 숟가락이 올라옴' if not MIRROR else '숟가락과 젓가락 자리를 서로 바꿈')]
tiles=[]; n=0
for front,rl,rt,rd in rows:
    tiles.append(f'    <div style="display: flex; flex-direction: column; gap: 4px; padding-top: 56px"><span style="font-size: 22px; font-weight: 600">{rt}</span><span style="font-size: 16px; color: #525C6B">{rd}</span></div>')
    for kind,cn in cols:
        n+=1
        tiles.append(f'    <figure style="margin: 0; display: flex; flex-direction: column; align-items: center; gap: 10px"><svg width="160" height="160" viewBox="0 0 100 100" role="img" aria-label="{rl}{cn}{"m" if MIRROR else ""}" style="border-radius: 36px; background: {OR}">{col(kind,front)}</svg><figcaption style="font-size: 18px; color: #374151">{n+OFF:02d}</figcaption></figure>')
head_cells='\n'.join(f'    <div style="text-align: center">{h}</div>' for h in heads)
html=f'''<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<title>{TITLE}</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
body{{margin:0;background:#F4F6FA}}
</style>
</helmet>
<div style="width: 1300px; height: 760px; box-sizing: border-box; background: #F4F6FA; color: #111827; font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; padding: 56px 64px; display: flex; flex-direction: column; gap: 28px; word-break: keep-all">

  <div style="display: flex; align-items: baseline; justify-content: space-between">
    <h1 style="margin: 0; font-size: 36px; font-weight: 700">{TITLE}</h1>
    <p style="margin: 0; font-size: 20px; color: #525C6B">세로 = 앞에 오는 것 2가지 · 가로 = 핀 처리 5가지</p>
  </div>

  <div style="display: grid; grid-template-columns: 172px repeat(5, minmax(0, 1fr)); gap: 0 40px; align-items: center; font-size: 17px; color: #525C6B">
    <div></div>
{head_cells}
  </div>

  <div style="display: grid; grid-template-columns: 172px repeat(5, minmax(0, 1fr)); gap: 24px 40px; align-items: start">
{chr(10).join(tiles)}
  </div>

  <p style="margin: 8px 0 0; font-size: 16px; color: #525C6B">배경 #E4572E · 핀/도안 #FFFFFF · 숟가락은 이음새 없는 한 덩어리 면, 젓가락은 선 · 겹치는 쪽은 뒤 색으로 살짝 띄움</p>
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":1300,"height":760}}}}'>
class Component extends DCLogic {{
  renderVals() {{
    return {{}};
  }}
}}
</script>
</body>
</html>
'''
open(f'icon_canvas/project/{NAME}.dc.html','w',encoding='utf-8').write(html)
s=re.sub(r'<script src="\./support\.js"></script>\n','',html)
s=s.replace('<x-dc>\n<helmet>','').replace('</helmet>','').replace('</x-dc>','')
s=re.sub(r'<script type="text/x-dc".*?</script>\n','',s,flags=re.S)
os.makedirs('or_render',exist_ok=True); open(f'or_render/{SHEET}.html','w',encoding='utf-8').write(s)
repo=sys.argv[1]
pat=re.compile(r'<svg width="160" height="160" viewBox="0 0 100 100" role="img" aria-label="([^"]+)" style="border-radius: 36px; background: ([^;"]+)[^"]*">(.*?)</svg><figcaption[^>]*>(\d+)</figcaption>',re.S)
out=os.path.join(repo,SVGDIR); os.makedirs(out,exist_ok=True)
for cell,bg,inner,num in pat.findall(s):
    svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 100 100"><rect width="100" height="100" rx="22" fill="{bg}"/>{inner}</svg>\n'
    open(os.path.join(out,f'orange-{num}-{cell}.svg'),'w',encoding='utf-8').write(svg)
print('ok')
