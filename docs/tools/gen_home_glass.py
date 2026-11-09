# -*- coding: utf-8 -*-
"""홈 화면(S-01) 리퀴드 글라스 시안 생성. usage: python3 gen_glass.py <out dir>"""
import sys, os
OUT = sys.argv[1]; os.makedirs(OUT, exist_ok=True)
ICON = open('/home/claude/hyundai_pleos_app_competition/docs/design/icons/final/app_icon.svg', encoding='utf-8').read().replace('width="512" height="512"', 'width="56" height="56"')

THEMES = {
 'dark': dict(name='다크', scheme='dark', bg='#15100E', text='#FBF6F2', muted='#B8A69B', faint='#8C7B72',
   glass='rgba(255,255,255,0.08)', glass2='rgba(255,255,255,0.12)', edge='rgba(255,255,255,0.22)', spec='rgba(255,255,255,0.45)',
   shadow='0 24px 60px rgba(0,0,0,0.45)', accent='#E4572E', accent_text='#FF8A62', amber='#F2B544', amber_bg='rgba(242,181,68,0.16)',
   blob1='rgba(228,87,46,0.55)', blob2='rgba(255,138,98,0.35)', blob3='rgba(120,60,40,0.6)', road='rgba(255,255,255,0.07)', mic_glow='rgba(228,87,46,0.35)'),
 'light': dict(name='라이트', scheme='light', bg='#F6EFEA', text='#1F1512', muted='#6E5B53', faint='#9A867C',
   glass='rgba(255,255,255,0.55)', glass2='rgba(255,255,255,0.7)', edge='rgba(255,255,255,0.9)', spec='rgba(255,255,255,0.95)',
   shadow='0 24px 60px rgba(120,60,40,0.18)', accent='#E4572E', accent_text='#B83A18', amber='#8A5A00', amber_bg='rgba(242,181,68,0.28)',
   blob1='rgba(228,87,46,0.35)', blob2='rgba(255,170,130,0.45)', blob3='rgba(255,220,200,0.9)', road='rgba(120,60,40,0.10)', mic_glow='rgba(228,87,46,0.28)'),
}

def ic(path, size, color, sw=2):
    return f'<svg width="{size}" height="{size}" viewBox="0 0 24 24" fill="none" stroke="{color}" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{path}</svg>'
MIC = '<path d="M12 15a4 4 0 0 0 4-4V6a4 4 0 1 0-8 0v5a4 4 0 0 0 4 4Z"/><path d="M19 11a7 7 0 0 1-14 0"/><path d="M12 18v3"/>'
NAV = '<path d="m3 11 19-9-9 19-2-8Z"/>'
GEAR = '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1Z"/>'
UTENSIL = '<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/>'
PIN = '<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/>'
CHEV = '<path d="m9 5 7 7-7 7"/>'

def page(t):
    T = THEMES[t]
    return f'''<!doctype html>
<html lang="ko"><head><meta charset="utf-8"><title>다음에 가게 홈 · 리퀴드 글라스 · {T['name']}</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
body{{margin:0;background:{T['bg']}}}
.root{{position:relative;width:1280px;height:1080px;overflow:hidden;background:{T['bg']};color:{T['text']};font-family:'Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif;word-break:keep-all}}
/* 콘텐츠 층: 흐린 지도 느낌 */
.bgl{{position:absolute;inset:0;overflow:hidden}}
.blob{{position:absolute;border-radius:50%;filter:blur(90px)}}
.roads{{position:absolute;inset:0}}
/* 유리 */
.glass{{position:relative;background:{T['glass']};-webkit-backdrop-filter:blur(28px) saturate(170%);backdrop-filter:blur(28px) saturate(170%);border:1px solid {T['edge']};box-shadow:inset 0 1px 0 {T['spec']},inset 0 -1px 0 rgba(0,0,0,0.08),{T['shadow']}}}
.glass::before{{content:"";position:absolute;inset:0;border-radius:inherit;pointer-events:none;background:linear-gradient(160deg,rgba(255,255,255,0.22) 0%,rgba(255,255,255,0) 38%,rgba(255,255,255,0) 70%,rgba(255,255,255,0.08) 100%)}}
.pill{{border-radius:999px}}
.card{{border-radius:36px}}
.top{{position:absolute;left:40px;right:40px;top:28px;height:88px;display:flex;align-items:center;gap:16px}}
.brand{{height:88px;padding:0 28px 0 16px;display:flex;align-items:center;gap:16px;font-size:32px;font-weight:700}}
.brand svg{{border-radius:14px}}
.loc{{height:88px;padding:0 30px;display:flex;align-items:center;gap:14px;color:{T['muted']};font-size:26px}}
.iconbtn{{width:88px;height:88px;display:flex;align-items:center;justify-content:center;color:{T['text']}}}
.spacer{{flex:1}}
main{{position:absolute;left:0;right:0;top:140px;bottom:0;display:grid;grid-template-columns:600px minmax(0,1fr);gap:48px;padding:16px 48px 48px;box-sizing:border-box}}
.talk{{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px}}
.mic{{width:360px;height:360px;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;position:relative;
  background:radial-gradient(circle at 35% 30%,rgba(255,255,255,0.35),rgba(255,255,255,0) 45%),linear-gradient(160deg,rgba(255,255,255,0.18),rgba(255,255,255,0) 50%),{T['accent']};
  box-shadow:inset 0 2px 0 rgba(255,255,255,0.55),inset 0 -14px 30px rgba(120,30,10,0.35),0 0 0 24px {T['mic_glow']},0 0 0 56px rgba(228,87,46,0.10),0 30px 70px rgba(228,87,46,0.40)}}
.mic::after{{content:"";position:absolute;inset:14px;border-radius:50%;border:1px solid rgba(255,255,255,0.35)}}
.hint h2{{margin:0;font-size:40px;font-weight:600;text-align:center}}
.hint p{{margin:14px 0 0;font-size:26px;line-height:1.5;color:{T['muted']};text-align:center}}
.side{{display:flex;flex-direction:column;justify-content:center;gap:24px;min-width:0}}
.row{{height:124px;padding:0 36px;display:flex;align-items:center;gap:24px;color:{T['text']};text-decoration:none;box-sizing:border-box}}
.row .ib{{width:68px;height:68px;border-radius:22px;background:{T['glass2']};display:flex;align-items:center;justify-content:center;flex:none;border:1px solid {T['edge']}}}
.row .tx{{flex:1;min-width:0;display:flex;flex-direction:column;gap:4px}}
.row .t{{font-size:34px;font-weight:600}}
.row .s{{font-size:24px;color:{T['muted']}}}
.recent{{padding:28px 36px 32px;display:flex;flex-direction:column;gap:18px;color:{T['text']};text-decoration:none;box-sizing:border-box}}
.recent .meta{{display:flex;align-items:center;justify-content:space-between;font-size:24px;color:{T['muted']}}}
.chip{{display:inline-flex;align-items:center;height:48px;padding:0 20px;border-radius:24px;background:{T['amber_bg']};color:{T['amber']};font-size:22px;font-weight:600}}
.recent .name{{display:flex;align-items:center;gap:20px}}
.recent .name b{{font-size:38px;font-weight:700;flex:1}}
.recent .name span{{font-size:24px;color:{T['muted']}}}
.recent .note{{font-size:24px;color:{T['faint']}}}
</style></head><body>
<div class="root">
  <div class="bgl" aria-hidden="true">
    <div class="blob" style="left:-120px;top:-80px;width:620px;height:620px;background:{T['blob1']}"></div>
    <div class="blob" style="right:-160px;top:220px;width:700px;height:700px;background:{T['blob2']}"></div>
    <div class="blob" style="left:300px;bottom:-260px;width:760px;height:560px;background:{T['blob3']}"></div>
    <svg class="roads" viewBox="0 0 1280 1080" fill="none" stroke="{T['road']}" stroke-width="22" stroke-linecap="round">
      <path d="M-40 300 C 260 260, 420 420, 700 380 S 1120 220, 1340 300"/>
      <path d="M180 -40 C 220 300, 120 600, 260 1120"/>
      <path d="M-40 760 C 300 700, 620 860, 900 780 S 1200 660, 1340 720"/>
      <path d="M980 -40 C 920 300, 1080 620, 960 1120"/>
      <path d="M560 -40 C 600 260, 520 500, 640 1120" stroke-width="12"/>
    </svg>
  </div>

  <header class="top">
    <div class="glass pill brand">{ICON}<span>다음에 가게</span></div>
    <div class="glass pill loc">{ic(NAV,26,T['accent_text'])}<span>강남대로 · 북쪽</span></div>
    <div class="spacer"></div>
    <button type="button" aria-label="설정" class="glass pill iconbtn" style="border:1px solid {T['edge']};cursor:pointer">{ic(GEAR,40,T['text'])}</button>
  </header>

  <main>
    <section class="talk" aria-label="말하기">
      <button type="button" aria-label="눌러서 말하기" class="mic" style="border:0;cursor:pointer">{ic(MIC,168,'#fff',1.8)}</button>
      <div class="hint"><h2>눌러서 말하기</h2><p>“방금 지난 가게 저장해줘”<br>“뭐 먹을까”</p></div>
    </section>
    <section class="side" aria-label="바로가기">
      <a href="#" class="glass card row"><span class="ib">{ic(UTENSIL,36,T['accent_text'])}</span><span class="tx"><span class="t">뭐 먹을까</span><span class="s">근처 저장 가게 중 3곳 추천</span></span>{ic(CHEV,32,T['faint'],2.4)}</a>
      <a href="#" class="glass card row"><span class="ib">{ic(PIN,36,T['accent_text'])}</span><span class="tx"><span class="t">내 맛집</span><span class="s">12곳 · 미확인 3곳</span></span>{ic(CHEV,32,T['faint'],2.4)}</a>
      <a href="#" class="glass card recent" aria-label="최근 저장 서울갈비">
        <span class="meta"><span>최근 저장 · 어제 18:42</span><span class="chip">미확인</span></span>
        <span class="name"><b>서울갈비</b><span>고깃집 · 1.2km</span></span>
        <span class="note">멈추면 어느 가게인지 확인해 주세요</span>
      </a>
    </section>
  </main>
</div>
</body></html>'''

for t, fn in (('dark','S-01_홈_글라스'),('light','S-01_홈_글라스_라이트')):
    open(os.path.join(OUT, fn+'.html'), 'w', encoding='utf-8').write(page(t))
print('ok')
