# -*- coding: utf-8 -*-
"""Generate docs/02_화면설계서.html / .md (+ standalone wireframe pages for PNG·GIF export) for 다음에 가게.

usage: python3 gen_screens.py <docs dir> <wire dir>
  <wire dir>/S-xx.html        static page (animations off)  -> PNG
  <wire dir>/live/S-xx.html   animated page                  -> GIF
  <wire dir>/screens.json     data for check_screens.py
"""
import os, sys, html, re, json

OUT_DIR = sys.argv[1] if len(sys.argv) > 1 else '.'
WIRE_DIR = sys.argv[2] if len(sys.argv) > 2 else os.path.join(OUT_DIR, 'wires')
LIVE_DIR = os.path.join(WIRE_DIR, 'live')
os.makedirs(WIRE_DIR, exist_ok=True); os.makedirs(LIVE_DIR, exist_ok=True)
DATE = '2026-11-09'

# ---------------------------------------------------------------- icons (Lucide style, 24 grid, stroke 2)
ICONS = {
 'mic': '<path d="M12 15a4 4 0 0 0 4-4V6a4 4 0 1 0-8 0v5a4 4 0 0 0 4 4Z"/><path d="M19 11a7 7 0 0 1-14 0"/><path d="M12 18v3"/>',
 'back': '<path d="m15 5-7 7 7 7"/>',
 'gear': '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1Z"/>',
 'fork': '<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/>',
 'list': '<path d="M8 6h13"/><path d="M8 12h13"/><path d="M8 18h13"/><path d="M3 6h.01"/><path d="M3 12h.01"/><path d="M3 18h.01"/>',
 'check': '<path d="m5 12 5 5L20 7"/>',
 'pin': '<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/>',
 'share': '<path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><path d="m16 6-4-4-4 4"/><path d="M12 2v13"/>',
 'edit': '<path d="M17 3a2.8 2.8 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/>',
 'trash': '<path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>',
 'close': '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
 'car': '<path d="M5 17H3v-5l2-5h14l2 5v5h-2"/><circle cx="7.5" cy="17.5" r="2.5"/><circle cx="16.5" cy="17.5" r="2.5"/><path d="M5 12h14"/>',
 'search': '<circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>',
 'navi': '<path d="m3 11 19-9-9 19-2-8Z"/>',
 'volume': '<path d="M11 5 6 9H2v6h4l5 4Z"/><path d="M15.5 8.5a5 5 0 0 1 0 7"/><path d="M19 5a9 9 0 0 1 0 14"/>',
 'undo': '<path d="M3 7v6h6"/><path d="M21 17a9 9 0 0 0-15-6.7L3 13"/>',
 'alert': '<path d="m21.7 18-8-14a2 2 0 0 0-3.4 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.7-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/>',
 'clock': '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
 'download': '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="m7 10 5 5 5-5"/><path d="M12 15V3"/>',
 'plus': '<path d="M12 5v14"/><path d="M5 12h14"/>',
 'keyboard': '<rect x="2" y="6" width="20" height="12" rx="2"/><path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M8 14h8"/>',
 'locate': '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3"/>',
 'flag': '<path d="M4 22V4a1 1 0 0 1 1-1h11l-2 4 2 4H5"/>',
 'chev': '<path d="m9 5 7 7-7 7"/>',
}
def ic(name, size=40, cls=''):
    return (f'<svg class="ic {cls}" width="{size}" height="{size}" viewBox="0 0 24 24" fill="none" '
            f'stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{ICONS[name]}</svg>')

# app icon (final 15) for the splash screen; falls back to the pin icon
APP_ICON = ''
_p = os.path.join(OUT_DIR, 'design', 'icons', 'final', 'app_icon.svg')
if os.path.exists(_p):
    _m = re.search(r'<rect[^>]*/>(.*)</svg>', open(_p, encoding='utf-8').read(), re.S)
    if _m:
        APP_ICON = (f'<svg width="240" height="240" viewBox="0 0 100 100" aria-hidden="true">'
                    f'<rect width="100" height="100" rx="22" fill="#E4572E"/>{_m.group(1)}</svg>')
BRAND_ICON = APP_ICON.replace('width="240" height="240"', 'width="56" height="56"') if APP_ICON else ''
if not APP_ICON:
    APP_ICON = f'<div style="width:240px;height:240px;border-radius:52px;background:#E4572E;display:flex;align-items:center;justify-content:center;color:#fff">{ic("pin",140)}</div>'
    BRAND_ICON = f'<div style="width:56px;height:56px;border-radius:14px;background:#E4572E;display:flex;align-items:center;justify-content:center;color:#fff">{ic("pin",32)}</div>'

# ---------------------------------------------------------------- wireframe CSS (1280x1080 canvas, 1dp = 1px)
# orange Liquid Glass style from the app icon (#E4572E). dark = default tokens, light = .frame.light (or .wl ancestor)
WIRE_CSS = r'''
.frame{--bg:#15100E;--fg:#FBF6F2;--muted:#B8A69B;--faint:#8C7B72;--line:rgba(255,255,255,.12);
  --glass:rgba(255,255,255,.09);--glass2:rgba(255,255,255,.14);--edge:rgba(255,255,255,.22);--spec:rgba(255,255,255,.45);
  --shadow:0 24px 60px rgba(0,0,0,.45);--shadow-s:0 10px 28px rgba(0,0,0,.32);--hi:linear-gradient(160deg,rgba(255,255,255,.20) 0%,rgba(255,255,255,0) 38%,rgba(255,255,255,0) 70%,rgba(255,255,255,.07) 100%);
  --accent:#E4572E;--acc-t:#FF8A62;--acc-soft:rgba(228,87,46,.20);--acc-ring:rgba(228,87,46,.35);
  --amber:#F2B544;--amber-bg:rgba(242,181,68,.16);--ok:#4CC38A;--ok-bg:rgba(76,195,138,.16);
  --danger:#FF6B6B;--danger-fill:#C62828;--danger-edge:rgba(255,107,107,.45);
  --overlay:rgba(12,7,5,.62);--field:rgba(0,0,0,.28);--off:rgba(255,255,255,.20);--map1:#1B1411;--map2:#201814;--kbd:rgba(0,0,0,.30);
  position:relative;width:1280px;height:1080px;background:var(--bg);color:var(--fg);overflow:hidden;
  font-family:"Pretendard","Apple SD Gothic Neo","Malgun Gothic","Noto Sans KR",system-ui,sans-serif;font-size:28px;line-height:1.3;word-break:keep-all}
.frame.light,.wl .frame{--bg:#F6EFEA;--fg:#1F1512;--muted:#6E5B53;--faint:#857168;--line:rgba(31,21,18,.10);
  --glass:rgba(255,255,255,.78);--glass2:rgba(255,255,255,.95);--edge:rgba(255,255,255,.9);--spec:rgba(255,255,255,.95);
  --shadow:0 24px 60px rgba(120,60,40,.18);--shadow-s:0 10px 26px rgba(120,60,40,.14);--hi:linear-gradient(160deg,rgba(255,255,255,.55) 0%,rgba(255,255,255,0) 40%);
  --acc-t:#B83A18;--acc-soft:rgba(228,87,46,.14);--acc-ring:rgba(228,87,46,.28);
  --amber:#8A5A00;--amber-bg:rgba(242,181,68,.28);--ok:#1B7F4D;--ok-bg:rgba(27,127,77,.13);
  --danger:#C62828;--danger-fill:#C62828;--danger-edge:rgba(198,40,40,.40);
  --overlay:rgba(70,40,28,.24);--field:rgba(255,255,255,.92);--off:rgba(31,21,18,.18);--map1:#EEE4DD;--map2:#E9DED6;--kbd:rgba(255,255,255,.7)}
.frame *{box-sizing:border-box}
.frame .ic{flex:none}
.frame .ic.acc{color:var(--acc-t)}.frame .ic.faint{color:var(--faint)}.frame .ic.muted{color:var(--muted)}
.top{position:absolute;left:0;top:0;right:0;height:96px;display:flex;align-items:center;gap:16px;padding:0 24px 0 16px}
.top .title{font-size:36px;font-weight:600;flex:1}
.top .iconbtn{width:80px;height:80px;display:flex;align-items:center;justify-content:center;border-radius:50%;color:var(--fg);background:var(--glass) var(--hi);border:1px solid var(--edge);box-shadow:inset 0 1px 0 var(--spec),var(--shadow-s);backdrop-filter:blur(28px) saturate(170%)}
.top .textbtn{height:76px;padding:0 28px;display:flex;align-items:center;gap:12px;border-radius:999px;border:1px solid var(--edge);background:var(--glass) var(--hi);box-shadow:inset 0 1px 0 var(--spec),var(--shadow-s);font-size:30px;font-weight:600;backdrop-filter:blur(28px) saturate(170%)}
.top.home{left:40px;right:40px;top:28px;height:88px;padding:0}
.top.home .iconbtn{width:88px;height:88px}
.gpill{height:88px;display:flex;align-items:center;gap:16px;border-radius:999px;background:var(--glass) var(--hi);border:1px solid var(--edge);box-shadow:inset 0 1px 0 var(--spec),var(--shadow-s);backdrop-filter:blur(28px) saturate(170%)}
.gpill.brand{padding:0 28px 0 16px;font-size:32px;font-weight:700}
.gpill.brand svg{border-radius:14px}
.gpill.loc{padding:0 30px;gap:14px;color:var(--muted);font-size:26px}
.body{position:absolute;left:0;right:0;bottom:0;top:96px;padding:48px}
.body.full{top:0}
.body.home{top:140px;padding:16px 48px 48px;display:grid;grid-template-columns:600px minmax(0,1fr);gap:48px}
.home .talk{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px}
.home .side{display:flex;flex-direction:column;justify-content:center;gap:24px;min-width:0}
.hrow{height:124px;padding:0 36px!important;display:flex;align-items:center;gap:24px}
.hrow .ib{width:68px;height:68px;border-radius:22px;background:var(--glass2);border:1px solid var(--edge);display:flex;align-items:center;justify-content:center;flex:none}
.hrow .tx{flex:1;min-width:0;display:flex;flex-direction:column;gap:4px}
.hrow .t{font-size:34px;font-weight:600}
.recent{display:flex;flex-direction:column;gap:16px;padding:28px 36px 32px!important}
.recent .meta{display:flex;align-items:center;justify-content:space-between}
.recent .nm{display:flex;align-items:center;gap:20px;font-size:38px;font-weight:700}
.btn{display:flex;align-items:center;justify-content:center;gap:16px;height:88px;padding:0 40px;border-radius:999px;border:1px solid var(--edge);background:var(--glass2) var(--hi);box-shadow:inset 0 1px 0 var(--spec),var(--shadow-s);font-size:32px;font-weight:600;color:var(--fg);backdrop-filter:blur(28px) saturate(170%)}
.btn.primary{background:linear-gradient(160deg,rgba(255,255,255,.22),rgba(255,255,255,0) 55%),var(--accent);border-color:rgba(255,255,255,.28);color:#fff;box-shadow:inset 0 1px 0 rgba(255,255,255,.5),0 12px 30px rgba(228,87,46,.35)}
.btn.big{height:112px;font-size:36px}
.btn.danger{background:var(--glass2);border:2px solid var(--danger-edge);color:var(--danger);box-shadow:none}
.btn.danger.fill{background:linear-gradient(160deg,rgba(255,255,255,.18),rgba(255,255,255,0) 55%),var(--danger-fill);border-color:var(--danger-fill);color:#fff;box-shadow:0 12px 30px rgba(198,40,40,.30)}
.btn.ghost{background:transparent;border-color:transparent;box-shadow:none;color:var(--muted);backdrop-filter:none}
.card{position:relative;background:var(--glass) var(--hi);border:1px solid var(--edge);border-radius:36px;padding:32px;box-shadow:inset 0 1px 0 var(--spec),var(--shadow);backdrop-filter:blur(28px) saturate(170%)}
.muted{color:var(--muted)}.small{font-size:24px}.h40{font-size:40px;font-weight:600}.h48{font-size:48px;font-weight:600}.h64{font-size:64px;font-weight:700;line-height:1.15}
.chip{display:inline-flex;align-items:center;height:48px;padding:0 20px;border-radius:24px;border:1px solid var(--edge);background:var(--glass2);font-size:24px;font-weight:600;color:var(--muted);white-space:nowrap}
.chip.on{background:var(--accent);border-color:var(--accent);color:#fff}
.chip.warn{background:var(--amber-bg);border-color:transparent;color:var(--amber)}
.chip.ok{background:var(--ok-bg);border-color:transparent;color:var(--ok)}
.chip.dim{background:transparent;border-color:var(--line);color:var(--faint)}
.row{display:flex;align-items:center;gap:24px;height:104px;padding:0 24px;border-bottom:1px solid var(--line)}
.row .name{font-size:32px;font-weight:600}
.row.sel{background:var(--acc-soft);border-radius:24px;border-bottom-color:transparent}
.toggle{width:96px;height:52px;border-radius:26px;background:var(--accent);position:relative;flex:none;box-shadow:inset 0 1px 0 rgba(255,255,255,.35)}
.toggle::after{content:"";position:absolute;top:6px;right:6px;width:40px;height:40px;border-radius:50%;background:#fff;box-shadow:0 2px 6px rgba(0,0,0,.25)}
.toggle.off{background:var(--off)}.toggle.off::after{right:auto;left:6px}
.seg{display:flex;border:1px solid var(--edge);border-radius:999px;overflow:hidden;height:80px;background:var(--glass)}
.seg div{padding:0 32px;display:flex;align-items:center;font-size:28px;color:var(--muted);border-radius:999px}
.seg .on{background:var(--accent);color:#fff}
.mapph{background:repeating-linear-gradient(45deg,var(--map1) 0 24px,var(--map2) 24px 48px);border:1px solid var(--line);border-radius:36px;position:relative;color:var(--muted)}
.mapph .lab{position:absolute;left:24px;top:20px;font-size:24px}
.mapph .lab.bot{top:auto;bottom:20px}
.mapph .pin{position:absolute;color:var(--acc-t)}
.mapph .pin.me{color:var(--fg)}
.overlay{position:absolute;inset:0;background:var(--overlay)}
.toast{position:absolute;left:140px;right:140px;height:120px;border-radius:36px;background:var(--glass2) var(--hi);border:1px solid var(--edge);box-shadow:inset 0 1px 0 var(--spec),var(--shadow);backdrop-filter:blur(28px) saturate(170%);display:flex;align-items:center;gap:24px;padding:0 40px;font-size:32px;font-weight:600}
.frame .ok,.toast .ok{color:var(--ok)}
.toast .act{margin-left:auto;height:76px;padding:0 28px;border-radius:999px;border:1px solid var(--edge);background:var(--glass2);display:flex;align-items:center;gap:12px;font-size:28px;color:var(--acc-t)}
[data-co]{position:relative}
[data-co]::after{content:attr(data-co);position:absolute;left:-18px;top:-18px;width:44px;height:44px;border-radius:50%;background:#7C3AED;color:#fff;box-shadow:0 0 0 3px rgba(255,255,255,.85);font-size:26px;font-weight:700;display:flex;align-items:center;justify-content:center;z-index:5;font-family:system-ui,sans-serif;line-height:1}
.clean [data-co]::after{display:none}
.top .iconbtn[data-co]::after{left:2px;top:2px}
.top .textbtn[data-co]::after{top:-2px}
.frame .toast,.frame .kbd{position:absolute}
.copad{padding:0 52px}
.wave{display:flex;align-items:center;gap:12px;height:120px}
.wave i{display:block;width:16px;border-radius:8px;background:var(--acc-t)}
.wave.off i{background:var(--off)}
.ring{width:72px;height:72px;border-radius:50%;border:8px solid var(--off);border-top-color:var(--acc-t);flex:none}
.mic{position:relative;width:360px;height:360px;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;
  background:radial-gradient(circle at 35% 30%,rgba(255,255,255,.35),rgba(255,255,255,0) 45%),linear-gradient(160deg,rgba(255,255,255,.18),rgba(255,255,255,0) 50%),var(--accent);
  box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -14px 30px rgba(120,30,10,.35),0 0 0 24px var(--acc-ring),0 0 0 56px rgba(228,87,46,.10),0 30px 70px rgba(228,87,46,.40)}
.mic::before{content:"";position:absolute;inset:14px;border-radius:50%;border:1px solid rgba(255,255,255,.35)}
.qr{width:280px;height:280px;border-radius:20px;background:
  repeating-linear-gradient(90deg,#1F1512 0 20px,transparent 20px 40px),repeating-linear-gradient(0deg,#1F1512 0 20px,#FFFFFF 20px 40px);border:12px solid #FFFFFF;box-shadow:var(--shadow-s)}
.input{height:88px;border:1px solid var(--edge);border-radius:24px;background:var(--field);display:flex;align-items:center;padding:0 24px;font-size:32px;gap:16px;box-shadow:inset 0 1px 3px rgba(0,0,0,.18)}
.input.focus{border:2px solid var(--accent);box-shadow:0 0 0 4px var(--acc-ring)}
.input .caret{width:3px;height:40px;background:var(--acc-t)}
.check{width:56px;height:56px;border-radius:16px;border:3px solid var(--faint);flex:none;display:flex;align-items:center;justify-content:center;color:#fff}
.check.on{background:var(--accent);border-color:var(--accent)}
.radio{width:56px;height:56px;border-radius:50%;border:3px solid var(--faint);flex:none;position:relative}
.radio.on{border-color:var(--accent)}.radio.on::before{content:"";position:absolute;inset:10px;border-radius:50%;background:var(--accent)}
.num{width:72px;height:72px;border-radius:50%;background:linear-gradient(160deg,rgba(255,255,255,.25),rgba(255,255,255,0) 55%),var(--accent);color:#fff;font-size:40px;font-weight:700;display:flex;align-items:center;justify-content:center;flex:none;box-shadow:inset 0 1px 0 rgba(255,255,255,.5)}
.slider{height:16px;border-radius:8px;background:var(--off);position:relative;flex:1}
.slider::before{content:"";position:absolute;left:0;top:0;bottom:0;width:60%;border-radius:8px;background:var(--accent)}
.slider::after{content:"";position:absolute;left:calc(60% - 28px);top:-20px;width:56px;height:56px;border-radius:50%;background:#fff;box-shadow:0 4px 12px rgba(0,0,0,.25)}
.hint{position:absolute;left:48px;right:48px;bottom:40px;text-align:center;color:var(--muted);font-size:28px}
.timebar{position:absolute;left:40px;right:40px;bottom:0;height:10px;background:var(--off);border-radius:5px 5px 0 0;overflow:hidden}
.timebar i{display:block;height:100%;width:62%;background:var(--accent)}
.dialog{position:absolute;left:240px;right:240px;top:300px;background:var(--glass2) var(--hi);border:1px solid var(--edge);border-radius:36px;padding:48px 56px;display:flex;flex-direction:column;gap:24px;box-shadow:inset 0 1px 0 var(--spec),var(--shadow);backdrop-filter:blur(28px) saturate(170%)}
.kbd{position:absolute;left:0;right:0;bottom:0;height:320px;border-top:1px dashed var(--edge);background:var(--kbd);backdrop-filter:blur(28px);display:flex;align-items:center;justify-content:center;gap:16px;color:var(--faint);font-size:28px}
.cd{width:72px;height:72px;flex:none}
.cd circle{fill:none;stroke-width:8}
.cd .bg{stroke:var(--off)}.cd .fg{stroke:var(--acc-t);stroke-linecap:round;stroke-dasharray:176;stroke-dashoffset:0;transform:rotate(-90deg);transform-origin:center}
.bars{display:flex;gap:10px;align-items:flex-end;height:48px}
.bars i{display:block;width:10px;border-radius:5px;background:var(--acc-t)}
.frame.port{width:640px}
.port .top.home{left:32px;right:32px;top:24px}
.port .gpill.loc{height:64px;padding:0 24px;font-size:24px}
.port .ploc{position:absolute;left:32px;top:132px}
.port .pcards{position:absolute;left:32px;right:32px;top:224px;display:flex;flex-direction:column;gap:20px}
.port .hrow{height:112px;padding:0 28px!important;gap:20px}
.port .hrow .ib{width:64px;height:64px}
.port .hrow .t{font-size:32px}
.port .recent{padding:24px 28px 28px!important;gap:12px}
.port .recent .nm{font-size:36px}
.port .ptalk{position:absolute;left:0;right:0;bottom:36px;display:flex;flex-direction:column;align-items:center;gap:44px}
.port .mic{width:240px;height:240px;box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -10px 24px rgba(120,30,10,.35),0 0 0 16px var(--acc-ring),0 0 0 36px rgba(228,87,46,.10),0 20px 50px rgba(228,87,46,.40)}
.port .pbottom{position:absolute;left:32px;right:32px;bottom:48px;display:flex;gap:32px;justify-content:center}
.port .toast{left:32px;right:32px}
.port .dialog{left:32px;right:32px}
'''

# animations run only inside .frame.play (doc: when scrolled into view / GIF pages); .frame.static shows the resting state
ANIM_CSS = r'''
@keyframes breathe{0%,100%{box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -14px 30px rgba(120,30,10,.35),0 0 0 24px var(--acc-ring),0 0 0 56px rgba(228,87,46,.10),0 30px 70px rgba(228,87,46,.40)}50%{box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -14px 30px rgba(120,30,10,.35),0 0 0 40px rgba(228,87,46,.16),0 0 0 72px rgba(228,87,46,.06),0 30px 70px rgba(228,87,46,.40)}}
@keyframes bar{0%,100%{transform:scaleY(.3)}50%{transform:scaleY(1)}}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes up{from{opacity:0;transform:translateY(24px)}}
@keyframes down{from{opacity:0;transform:translateY(-32px)}}
@keyframes pop{from{opacity:0;transform:scale(.96)}}
@keyframes fade{from{opacity:0}}
@keyframes drop{0%{opacity:0;transform:translateY(-48px)}60%{opacity:1;transform:translateY(8px)}100%{opacity:1;transform:none}}
@keyframes toastloop{0%{opacity:0;transform:translateY(24px)}5%{opacity:1;transform:none}88%{opacity:1;transform:none}93%{opacity:0;transform:translateY(8px)}100%{opacity:0;transform:translateY(8px)}}
@keyframes shrink{from{width:100%}to{width:0}}
@keyframes dash{to{stroke-dashoffset:176}}
@keyframes shake{0%,100%{transform:none}25%{transform:translateX(-8px)}75%{transform:translateX(8px)}}
@keyframes pulse{0%{transform:scale(1);opacity:.8}100%{transform:scale(2.4);opacity:0}}
@keyframes collapse{to{height:0;opacity:0;padding-top:0;padding-bottom:0;border-width:0;overflow:hidden}}
@keyframes hl{from{background:var(--acc-soft)}}
@keyframes slidex{from{opacity:0;transform:translateX(48px)}}
@keyframes grow{from{transform:scale(1)}to{transform:scale(1.04)}}
@keyframes dimout{to{opacity:.4}}
@keyframes bars{0%,100%{height:12px}50%{height:48px}}
.play .mic.live{animation:breathe 2s ease-in-out infinite}
@keyframes breathes{0%,100%{box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -10px 24px rgba(120,30,10,.35),0 0 0 16px var(--acc-ring),0 0 0 36px rgba(228,87,46,.10),0 20px 50px rgba(228,87,46,.40)}50%{box-shadow:inset 0 2px 0 rgba(255,255,255,.55),inset 0 -10px 24px rgba(120,30,10,.35),0 0 0 26px rgba(228,87,46,.16),0 0 0 48px rgba(228,87,46,.06),0 20px 50px rgba(228,87,46,.40)}}
.play.port .mic.live{animation:breathes 2s ease-in-out infinite}
.play .wave.live i{animation:bar .9s ease-in-out infinite;transform-origin:center;animation-delay:calc(var(--i,0)*60ms)}
.play .ring.live{animation:spin 1s linear infinite}
.play .a-up{animation:up .2s cubic-bezier(.2,0,0,1) both}
.play .a-up.d1{animation-delay:60ms}.play .a-up.d2{animation-delay:120ms}.play .a-up.d3{animation-delay:180ms}
.play .a-down{animation:down .24s cubic-bezier(.05,.7,.1,1) both}
.play .a-pop{animation:pop .2s cubic-bezier(.2,0,0,1) both}
.play .a-fade{animation:fade .16s linear both}
.play .a-drop{animation:drop .4s cubic-bezier(.05,.7,.1,1) both}
.play .a-slidex{animation:slidex .2s cubic-bezier(.2,0,0,1) both}
.play .a-shake{animation:shake .24s ease-in-out .4s 1}
.play .a-grow{animation:grow .2s cubic-bezier(.2,0,0,1) .3s both}
.play .a-dim{animation:dimout .2s linear .3s both}
.play .a-hl{animation:hl 1.2s ease-out .4s both}
.play .toast.live{animation:toastloop 4.6s cubic-bezier(.2,0,0,1) infinite}
.play .timebar.live i{animation:shrink var(--t,10s) linear infinite}
.play .cd.live .fg{animation:dash var(--t,5s) linear infinite}
.play .pulse:not(.pin){position:relative}.play .pulse::before{content:"";position:absolute;left:50%;top:50%;width:40px;height:40px;margin:-20px 0 0 -20px;border-radius:50%;background:var(--acc-t);animation:pulse 1.2s ease-out infinite}
.play .row.a-collapse{animation:collapse .2s cubic-bezier(.2,0,0,1) .8s forwards}
.play .bars.live i{animation:bars .8s ease-in-out infinite;animation-delay:calc(var(--i,0)*120ms)}
.play .a-text{animation:fade .08s linear both}
.play .a-text.d1{animation-delay:.3s}.play .a-text.d2{animation-delay:.6s}.play .a-text.d3{animation-delay:.9s}
.play .a-stag{animation:up .2s cubic-bezier(.2,0,0,1) both;animation-delay:calc(var(--i,0)*40ms)}
.static .a-collapse{display:none}
.static .a-dim{opacity:.4}
.static .a-grow{transform:scale(1.04)}
.static .a-hl{background:var(--acc-soft)}
'''
STD = 'cubic-bezier(.2,0,0,1)'

def top(title, back=True, right=''):
    b = f'<div class="iconbtn" data-co="b">{ic("back",48)}</div>' if back else '<div style="width:16px"></div>'
    return f'<div class="top">{b}<div class="title">{title}</div>{right}</div>'

def wave(live=True, off=False, n=17):
    hs = (28,56,96,120,80,44,104,72,36,88,112,60,32,76,100,52,24)
    cls = 'wave' + (' live' if live else '') + (' off' if off else '')
    return f'<div class="{cls}">' + ''.join(f'<i style="height:{h}px;--i:{i}"></i>' for i, h in enumerate(hs[:n])) + '</div>'

def countdown(sec=5):
    return (f'<svg class="cd live" viewBox="0 0 72 72" style="--t:{sec}s" aria-hidden="true">'
            f'<circle class="bg" cx="36" cy="36" r="28"/><circle class="fg" cx="36" cy="36" r="28"/></svg>')

def home(dim=1, recent='', count='12', sub1='근처 저장 가게 중 3곳 추천', sub2=None, co=None, live=False):
    """glass home (S-01). co maps 'mic','eat','mine','rec','gear','loc' to callout numbers"""
    co = co or {}
    c = lambda k: f' data-co="{co[k]}"' if k in co else ''
    sub2 = sub2 if sub2 is not None else f'{count}곳 · 미확인 3곳'
    op = f';opacity:{dim}' if dim != 1 else ''
    return f'''
 <div class="top home"{'' if dim == 1 else f' style="opacity:{dim}"'}>
   <div class="gpill brand">{BRAND_ICON}<span>다음에 가게</span></div>
   <div class="gpill loc"{c('loc')}>{ic("navi",26,"acc")}<span>강남대로 · 북쪽</span></div>
   <div style="flex:1"></div>
   <div class="iconbtn"{c('gear')}>{ic("gear",40)}</div>
 </div>
 <div class="body home" style="{op[1:]}">
   <div class="talk">
     <div class="mic{' live' if live else ''}"{c('mic')}>{ic("mic",168)}</div>
     <div style="text-align:center"><div class="h40">눌러서 말하기</div><div class="muted" style="margin-top:14px;line-height:1.5">“방금 지난 가게 저장해줘”<br>“뭐 먹을까”</div></div>
   </div>
   <div class="side">
     <div class="card hrow"{c('eat')}><span class="ib">{ic("fork",36,"acc")}</span><span class="tx"><span class="t">뭐 먹을까</span><span class="small muted">{sub1}</span></span>{ic("chev",32,"faint")}</div>
     <div class="card hrow"{c('mine')}><span class="ib">{ic("pin",36,"acc")}</span><span class="tx"><span class="t">내 맛집</span><span class="small muted">{sub2}</span></span>{ic("chev",32,"faint")}</div>
     {recent}
   </div>
 </div>'''

def home_bg(dim=.55, recent='', count='12'):
    return home(dim=dim, recent=recent or recent_card('서울갈비','<span class="chip warn">미확인</span>'), count=count,
                sub2=(f'{count}곳' if count == '0' else None))

def recent_card(name, chip, co=None, cls='', when='어제 18:42'):
    c = f' data-co="{co}"' if co else ''
    return f'''<div class="card recent {cls}"{c}>
       <div class="meta"><span class="small muted">최근 저장{(' · ' + when) if when else ''}</span>{chip}</div>
       <div class="nm">{ic("pin",40,"acc")}<span>{name}</span></div>
     </div>'''

def listen_card(status, middle, sentence, extra_cls='', btn_co='4', status_co='1'):
    return f'''
 {home_bg(dim=1)}
 <div class="overlay a-fade"></div>
 <div class="card a-pop {extra_cls}" style="position:absolute;left:160px;top:200px;width:960px;height:640px;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:48px">
   <div class="h48 copad" data-co="{status_co}">{status}</div>
   {middle}
   <div style="font-size:40px;text-align:center;min-height:120px">{sentence}</div>
   <div class="btn" data-co="{btn_co}" style="width:320px">{ic("close",40)} 취소</div>
 </div>'''

def dialog(title, desc, ok_label, co=('1','2','3','4'), ok_cls='danger fill', bg=''):
    return f'''
 {bg}
 <div class="overlay a-fade"></div>
 <div class="dialog a-pop">
   <div class="h40" data-co="{co[0]}">{title}</div>
   <div class="muted" data-co="{co[1]}" style="font-size:28px">{desc}</div>
   <div style="display:flex;gap:24px;justify-content:flex-end;margin-top:16px">
     <div class="btn" data-co="{co[3]}" style="width:240px">취소</div>
     <div class="btn {ok_cls}" data-co="{co[2]}" style="width:280px">{ok_label}</div>
   </div>
 </div>'''

# ---------------------------------------------------------------- screens
GROUPS = [
 ('A','시작과 권한','공통'),
 ('B','음성으로 저장','F-01 ~ F-06'),
 ('C','근처 리마인드','F-08, F-10'),
 ('D','추천','F-09, F-10'),
 ('E','방문 기록','F-11'),
 ('F','내 맛집','F-12, F-07'),
 ('G','맛집 지도 공유','F-13'),
 ('H','설정과 주행 제한','F-15, F-14'),
]
S = []
def screen(sid, group, name, when, entry, funcs, wire, spec, motion, note='', gif=False):
    S.append(dict(id=sid, group=group, name=name, when=when, entry=entry, funcs=funcs, wire=wire, spec=spec, motion=motion, note=note, gif=gif))

# ===== A. 시작과 권한 =====
screen('S-00','A','시작','주행 중 · 정차 중','앱 아이콘을 누름','공통',
 f'''
 <div class="body full" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px">
   <div class="a-drop" data-co="1">{APP_ICON}</div>
   <div class="a-fade" data-co="2" style="font-size:56px;font-weight:700;animation-delay:.25s">다음에 가게</div>
   <div class="muted a-fade" style="animation-delay:.4s">지나친 가게, 말로 저장하세요</div>
 </div>
 ''',
 [('1','앱 아이콘','확정 아이콘(15번) 240dp','0.8초 안에 S-01 홈으로. 처음 실행이면 S-13'),
  ('2','앱 이름','56dp','')],
 [('앱 아이콘','위에서 48dp 떨어지며 살짝 튕김','400ms · emphasized','화면 열릴 때'),
  ('앱 이름·문구','페이드 인','160ms · 250ms 지연','아이콘 뒤이어'),
  ('홈으로 전환','페이드','200ms','불러오기 끝나면(최대 0.8초)')],
 '불러올 것이 없으면 이 화면은 0.3초만 보인다. 로딩 막대는 두지 않는다.', gif=True)

screen('S-13','A','처음 실행 안내','정차 중 권장','S-00 뒤 첫 실행 1회','공통',
 f'''
 {home_bg(dim=.35, recent=recent_card('—','<span class="chip dim">없음</span>'), count='0')}
 <div class="overlay a-fade"></div>
 <div class="card a-pop" data-co="1" style="position:absolute;left:200px;right:200px;top:160px;padding:48px 56px;display:flex;flex-direction:column;gap:28px">
   <div class="h48">차 안에만 저장돼요</div>
   <div class="muted" style="font-size:28px">말한 내용과 위치는 이 차에만 남고 밖으로 보내지 않아요. 아래 세 가지를 허용하면 바로 쓸 수 있어요.</div>
   <div data-co="2" style="display:flex;flex-direction:column;gap:16px;padding-top:8px">
     <div style="display:flex;align-items:center;gap:20px;height:76px">{ic("mic",40)}<b>마이크</b><span class="muted">말한 내용으로 가게를 찾아요</span></div>
     <div style="display:flex;align-items:center;gap:20px;height:76px">{ic("locate",40)}<b>위치</b><span class="muted">지나친 가게를 추정하고 근처에서 알려요</span></div>
     <div style="display:flex;align-items:center;gap:20px;height:76px">{ic("navi",40)}<b>내비</b><span class="muted">고른 가게로 안내를 시작해요</span></div>
   </div>
   <div style="display:flex;gap:24px;justify-content:flex-end;margin-top:8px">
     <div class="btn" data-co="4" style="width:240px">나중에</div>
     <div class="btn primary big" data-co="3" style="width:360px">시작하기</div>
   </div>
 </div>
 ''',
 [('1','안내 카드','차 안에만 저장된다는 고지 1장',''),
  ('2','허용 항목 3줄','마이크 · 위치 · 내비, 각각 쓰는 이유 한 줄',''),
  ('3','시작하기','시스템 권한 창을 마이크 → 위치 → 내비 순서로 띄움','모두 끝나면 S-01'),
  ('4','나중에','권한 없이 홈으로. 말하기 버튼을 누르면 S-17','')],
 [('어두운 배경','페이드 인','160ms','카드 열릴 때'),
  ('안내 카드','96% → 100% 확대 + 페이드','200ms · standard','어두운 배경 뒤이어'),
  ('닫힘','카드 페이드 아웃','160ms','시작하기 · 나중에')],
 '')

screen('S-17','A','권한 없음','주행 중 · 정차 중','권한이 없는 상태에서 말하기 · 리마인드 · 안내 시작을 누름','공통',
 f'''
 {home_bg(dim=.35, recent=recent_card('서울갈비','<span class="chip warn">미확인</span>'))}
 <div class="overlay a-fade"></div>
 <div class="dialog a-pop" style="top:280px">
   <div style="display:flex;align-items:center;gap:20px" data-co="1">{ic("alert",56,"muted")}<div class="h40">마이크를 쓸 수 없어요</div></div>
   <div class="muted" style="font-size:28px">말한 내용으로 가게를 찾으려면 마이크 허용이 필요해요. 차량 설정에서 켤 수 있어요.</div>
   <div style="display:flex;gap:24px;justify-content:flex-end;margin-top:16px">
     <div class="btn" data-co="3" style="width:240px">닫기</div>
     <div class="btn primary" data-co="2" style="width:320px">{ic("gear",36)} 설정에서 허용</div>
   </div>
 </div>
 ''',
 [('1','안내','무엇을 못 쓰는지 한 줄 + 왜 필요한지 한 줄','위치면 “위치를 쓸 수 없어요 · 지나친 가게를 찾으려면 필요해요”, 음성 서비스 오류면 “음성 서비스를 쓸 수 없어요 · 잠시 뒤 다시 해 보세요”'),
  ('2','설정에서 허용','차량 앱 권한 설정 화면을 염','주행 중이면 버튼 대신 “멈춘 뒤 설정에서 허용해 주세요” 문구'),
  ('3','닫기','원래 화면으로','')],
 [('창 열림','어두운 배경 페이드 + 창 96% → 100%','160ms / 200ms','버튼을 눌렀을 때'),
  ('창 닫힘','페이드 아웃','160ms','닫기 · 설정으로 이동')],
 '')

# ===== B. 음성으로 저장 =====
screen('S-01','B','홈','주행 중 · 정차 중','앱 실행','F-01, F-09, F-12, F-15',
 home(recent=recent_card('서울갈비','<span class="chip warn">미확인</span>', co='4'), live=True,
      co=dict(mic='1', eat='2', mine='3', gear='5', loc='6')),
 [('1','말하기 버튼','360dp 원형, 화면의 유일한 1순위 동작','누르면 S-02a. 주행 중에도 사용'),
  ('2','뭐 먹을까','추천 바로가기','누르면 S-06'),
  ('3','내 맛집','저장 개수 표시','정차 중 S-07, 주행 중 S-12'),
  ('4','최근 저장','마지막으로 저장한 가게 1건, 상태 칩(미확인/저장됨)','누르면 S-08 (미확인이면 S-09). 0건이면 S-01b'),
  ('5','설정','','S-11'),
  ('6','현재 위치','지금 달리는 도로 이름과 방향. 누를 수 없음','위치 권한이 없으면 숨김')],
 [('말하기 버튼','바깥 그림자가 24 → 40dp로 숨 쉬듯 커졌다 작아짐','2s · ease-in-out 반복','항상. 대비가 낮아 주행 중에도 허용'),
  ('버튼 누름','96%로 줄었다 복귀','120ms · standard','모든 버튼 공통'),
  ('최근 저장 카드','내용이 바뀔 때 페이드 교체','200ms','저장 완료 뒤'),
  ('화면 전환','주행 중 화면끼리는 페이드만(이동 없음)','160ms','S-02a · S-06 · S-12로')],
 '주행 중 화면은 버튼 4개가 전부다. 글자 최소 28dp, 터치 최소 88dp.', gif=True)

screen('S-01b','B','홈 · 저장 전','주행 중 · 정차 중','저장한 가게가 0개일 때의 홈','F-01',
 home(count='0', sub1='저장한 가게가 생기면 추천해요', sub2='0곳', co=dict(mine='2'), live=True,
      recent='''<div class="card recent" data-co="1">
       <div class="meta"><span class="small muted">최근 저장</span><span class="chip dim">없음</span></div>
       <div style="font-size:32px;font-weight:600">아직 저장한 가게가 없어요</div>
       <div class="muted small">운전하다 “방금 지난 가게 저장해줘”라고 말해 보세요</div>
     </div>'''),
 [('1','안내 카드','최근 저장 자리에 안내 2줄','누를 수 없음'),
  ('2','내 맛집 0','개수 0 표시','누르면 S-07b')],
 [('말하기 버튼','S-01과 같은 숨쉬기','2s 반복',''),
  ('첫 저장 뒤','안내 카드가 최근 저장 카드로 페이드 교체','200ms','S-04a 뒤')],
 '')

screen('S-02a','B','듣는 중','주행 중 · 정차 중','S-01 말하기 / 음성 “뭐 먹을까” / S-12 말하기','F-01, F-03',
 listen_card('듣고 있어요', f'<div data-co="2">{wave()}</div>',
   '<span data-co="3" style="padding:0 20px">방금 지난 <span class="a-text d1">빨간 간판</span> <span class="a-text d2">고깃집</span><span class="muted a-text d3">…</span></span>'),
 [('1','상태 문구','“듣고 있어요”','말이 끝나면 S-02b'),
  ('2','소리 막대','마이크 입력 크기에 따라 17개 막대가 움직임',''),
  ('3','인식 중 문장','STT 부분 결과를 실시간 표시 (40dp)','말이 끝나고 1.5초 지나면 자동 종료'),
  ('4','취소','녹음 중단, 원래 화면으로','')],
 [('어두운 배경','페이드 인','160ms','말하기 버튼을 누른 직후'),
  ('카드','96% → 100% 확대 + 페이드','200ms · standard','배경과 함께'),
  ('소리 막대','높이 30% ↔ 100% 반복, 막대마다 60ms씩 어긋남','900ms · ease-in-out 반복','마이크 입력 크기에 비례'),
  ('인식 문장','단어가 들어올 때마다 페이드로 붙음','80ms','STT 부분 결과')],
 '어느 화면 위에도 겹쳐 뜨는 레이어. 뒤 화면은 흐리게만 하고 내용은 바꾸지 않는다.', gif=True)

screen('S-02b','B','알아듣는 중','주행 중 · 정차 중','S-02a에서 말이 끝난 뒤','F-03',
 listen_card('알아듣는 중', f'<div data-co="2" style="height:120px;display:flex;align-items:center"><div class="ring live"></div></div>',
   '<span data-co="3" style="padding:0 20px">방금 지난 빨간 간판 고깃집 저장해줘</span>'),
 [('1','상태 문구','“알아듣는 중”','규칙 분석은 0.3초 안에 끝남'),
  ('2','처리 표시','회전하는 링 72dp','1초를 넘기면 그대로 유지, 3초를 넘기면 S-02c'),
  ('3','확정 문장','최종 인식 문장','저장이면 S-14, 추천이면 S-06, 취소면 닫힘'),
  ('4','취소','분석 중단, 원래 화면으로','')],
 [('소리 막대 → 링','막대가 페이드 아웃되고 링이 페이드 인','160ms','말이 끝났을 때'),
  ('링','한 바퀴 회전 반복','1s · linear 반복','분석 끝날 때까지'),
  ('다음 화면으로','카드 페이드 아웃','160ms','의도 판정 뒤')],
 '', gif=True)

screen('S-02c','B','다시 말해 주세요','주행 중 · 정차 중','S-02b에서 의도를 못 찾았을 때 / 3초 넘게 응답 없을 때','F-03',
 listen_card('잘 못 들었어요', f'<div>{wave(live=False, off=True)}</div>',
   '<span data-co="2" style="padding:0 20px"><span class="muted">예를 들어</span><br>“방금 지난 카페 저장해줘”</span>', extra_cls='a-shake', btn_co='3', status_co='1'),
 [('1','상태 문구','“잘 못 들었어요” + TTS “다시 한 번 말해 주세요”','1초 뒤 자동으로 다시 듣기(S-02a). 재시도는 1회만'),
  ('2','예시 문구','쓸 수 있는 말 한 줄',''),
  ('3','취소','닫기','두 번째도 실패하면 자동으로 닫히고 토스트 “다음에 다시 말해 주세요”')],
 [('문구 교체','페이드','160ms','실패 판정 직후'),
  ('카드','좌우 8dp 흔들림 1회','240ms · ease-in-out','문구와 함께'),
  ('막대','주황 → 회색 페이드','160ms','')],
 '')

screen('S-14','B','가게 찾는 중','주행 중','S-02b에서 저장 의도가 확정된 뒤','F-02, F-04',
 f'''
 <div class="body full" style="display:flex;flex-direction:column;align-items:center;gap:40px;padding-top:72px">
   <div data-co="1" style="display:flex;align-items:center;gap:24px" class="copad"><div class="ring live"></div><div class="h48">주변 가게 찾는 중</div></div>
   <div class="mapph a-fade" data-co="2" style="width:1000px;height:520px">
     <div class="lab">조금 전 지나온 길</div>
     <div class="pin me pulse" style="left:620px;top:300px">{ic("navi",56)}</div>
     <div class="pin" style="left:430px;top:200px;color:var(--accent)">{ic("pin",64)}</div>
     <div class="lab bot">주황 핀 = 말한 시점에 있었던 곳(추정)</div>
   </div>
   <div class="muted" data-co="3" style="font-size:32px">“방금 지난 빨간 간판 고깃집 저장해줘”</div>
 </div>
 ''',
 [('1','문구 + 링','“주변 가게 찾는 중”','후보가 나오면 S-03, 2초를 넘기거나 인터넷이 없으면 S-04b'),
  ('2','추정 위치 지도','위치 버퍼로 계산한 지점(주황)과 현재 위치','지도가 없으면 이 자리는 비움'),
  ('3','말한 문장','확정 문장 그대로','')],
 [('링','회전 반복','1s · linear','검색 중'),
  ('현재 위치 핀','원이 퍼지며 사라지는 맥박','1.2s · ease-out 반복','검색 중'),
  ('지도','페이드 인','160ms','화면 열릴 때'),
  ('다음 화면으로','페이드','160ms','후보 확정')],
 '보통 1초 안에 지나가는 화면이라 글자는 두 줄뿐이다.', gif=True)

screen('S-03','B','확인','주행 중','S-14 뒤 후보가 있을 때','F-04, F-05',
 f'''
 <div class="body full" style="display:flex;flex-direction:column;align-items:center;gap:40px;padding-top:72px">
   <div class="h48 copad" data-co="1">이 가게 맞나요?</div>
   <div class="card a-up" data-co="2" style="width:1000px;height:400px;display:flex;flex-direction:column;justify-content:center;gap:20px;padding:48px 56px">
     <div class="h64">서울갈비</div>
     <div style="font-size:32px">고깃집 · 120m 뒤 · 오른쪽</div>
     <div class="muted">서울 강남구 강남대로 123</div>
   </div>
   <div style="display:flex;gap:48px">
     <div class="btn primary" data-co="3" style="width:480px;height:136px;font-size:40px">{ic("check",48)} 예</div>
     <div class="btn" data-co="4" style="width:480px;height:136px;font-size:40px">아니요</div>
   </div>
   <div class="hint" style="display:flex;align-items:center;justify-content:center;gap:64px">
     <span data-co="5" style="display:flex;align-items:center;gap:16px">{countdown(5)}5초 안에 대답이 없으면 위치만 저장해요</span>
     <span data-co="6" style="display:flex;align-items:center;gap:12px;color:var(--acc-t)">{ic("mic",32)}<span class="bars live"><i style="--i:0;height:20px"></i><i style="--i:1;height:40px"></i><i style="--i:2;height:28px"></i><i style="--i:3;height:44px"></i></span></span>
   </div>
 </div>
 ''',
 [('1','질문','TTS로 같은 문장을 읽음',''),
  ('2','후보 카드','가게 이름 64dp, 업종·거리·방향, 주소','1순위 후보 1개만'),
  ('3','예','저장 → S-04a','음성 “응·맞아”와 같음'),
  ('4','아니요','2순위가 있으면 S-03b, 없으면 S-04b','음성 “아니”와 같음'),
  ('5','대기 표시','5초 카운트다운 링','시간이 지나면 S-04b'),
  ('6','듣는 중 표시','대답을 듣는 동안 작은 막대가 움직임','TTS가 끝난 뒤부터')],
 [('후보 카드','아래에서 24dp 올라오며 페이드 인','200ms · standard','화면 열릴 때'),
  ('카운트다운 링','원 테두리가 5초 동안 줄어듦','5s · linear','TTS가 끝난 뒤 시작'),
  ('듣는 중 막대','4개 막대 높이 반복','800ms 반복','TTS 끝난 뒤'),
  ('예 누름','카드가 페이드 아웃되고 S-04a 토스트','160ms','버튼 · 음성 응'),
  ('아니요 누름','카드가 왼쪽으로 48dp 밀려 나가고 새 카드가 오른쪽에서 들어옴','200ms','S-03b로')],
 '', gif=True)

screen('S-03b','B','확인 · 두 번째 후보','주행 중','S-03에서 아니요 1회','F-04, F-05',
 f'''
 <div class="body full" style="display:flex;flex-direction:column;align-items:center;gap:40px;padding-top:72px">
   <div class="h48 copad" data-co="1">그럼 이 가게인가요?</div>
   <div class="card a-slidex" data-co="2" style="width:1000px;height:400px;display:flex;flex-direction:column;justify-content:center;gap:20px;padding:48px 56px">
     <div class="h64">명품갈비</div>
     <div style="font-size:32px">고깃집 · 140m 뒤 · 오른쪽</div>
     <div class="muted">서울 강남구 강남대로 131</div>
   </div>
   <div style="display:flex;gap:48px">
     <div class="btn primary" data-co="3" style="width:480px;height:136px;font-size:40px">{ic("check",48)} 예</div>
     <div class="btn" data-co="4" style="width:480px;height:136px;font-size:40px">아니요</div>
   </div>
   <div class="hint" style="display:flex;align-items:center;justify-content:center;gap:16px">{countdown(5)}5초 안에 대답이 없으면 위치만 저장해요</div>
 </div>
 ''',
 [('1','질문','“그럼 이 가게인가요?” TTS 동일',''),
  ('2','2순위 카드','S-03과 같은 구성','세 번째 후보는 묻지 않음'),
  ('3','예','저장 → S-04a',''),
  ('4','아니요','S-04b (위치만 저장)','')],
 [('카드 교체','이전 카드는 왼쪽으로 나가고 새 카드가 오른쪽 48dp에서 들어옴','200ms · standard','아니요 직후'),
  ('카운트다운 링','처음부터 다시 5초','5s · linear','새 TTS 끝난 뒤')],
 '')

screen('S-04a','B','저장 완료','주행 중 · 정차 중','S-03 · S-03b에서 예','F-06',
 home_bg(dim=.7, recent=recent_card('서울갈비','<span class="chip ok">저장됨</span>', co='2', cls='a-fade')) +
 f'''<div class="toast live" data-co="1" style="top:900px">{ic("check",48,"ok")}<span>저장했어요 · 서울갈비</span></div>''',
 [('1','저장 토스트','가게 이름 포함, 4초 뒤 사라짐','TTS “저장했어요”'),
  ('2','최근 저장 카드','방금 저장한 가게로 바뀜, 칩 “저장됨”','')],
 [('토스트','아래에서 24dp 올라오며 페이드 인, 4초 뒤 페이드 아웃','200ms 들어옴 · 200ms 나감','저장 직후'),
  ('체크 아이콘','선이 그려지듯 나타남','300ms','토스트와 함께'),
  ('최근 저장 카드','내용 페이드 교체','200ms','토스트와 동시')],
 '토스트가 떠 있는 동안에도 말하기 버튼은 바로 누를 수 있다.', gif=True)

screen('S-04b','B','위치만 저장','주행 중 · 정차 중','후보 없음 · 인터넷 없음 · 아니요 2회 · 5초 무응답','F-06',
 home_bg(dim=.7, recent=recent_card('이름 모름 · 강남대로','<span class="chip warn">미확인</span>', co='2', cls='a-fade')) +
 f'''
 <div class="toast live" data-co="1" style="top:880px;height:150px;align-items:flex-start;padding-top:28px;flex-direction:column;gap:8px">
   <div style="display:flex;gap:24px;align-items:center">{ic("pin",48,"muted")}<span>위치를 기억해 둘게요</span></div>
   <div class="muted small" style="font-weight:400;padding-left:72px">멈추면 어느 가게인지 확인해 주세요</div>
 </div>''',
 [('1','미확인 토스트','2줄, 4초','TTS “위치를 기억해 둘게요”'),
  ('2','최근 저장 카드','“이름 모름 · 도로명” + 칩 “미확인”','누르면 S-09 (정차 중)')],
 [('토스트','S-04a와 같은 들어옴·나감','200ms / 200ms',''),
  ('최근 저장 카드','페이드 교체','200ms','')],
 'S-04a와 동시에 뜨지 않는다. 둘 중 하나만.')

# ===== C. 근처 리마인드 =====
screen('S-05','C','리마인드','주행 중','저장 가게 500m 안 진입 (차량 알림)','F-08, F-10',
 f'''
 <div class="body full" style="padding:0">
   <div class="mapph" style="position:absolute;inset:0;border:0;border-radius:0"><div class="lab bot">현재 화면 (내비 등)</div></div>
   <div class="card a-down" data-co="1" style="position:absolute;left:48px;right:48px;top:48px;height:300px;display:flex;align-items:center;gap:32px;padding:36px 40px;background-color:var(--glass2)">
     {ic("pin",72)}
     <div style="flex:1">
       <div class="h40">지난주에 저장한 서울갈비</div>
       <div style="font-size:32px;margin-top:8px">300m 앞 · 오른쪽</div>
       <div class="muted small" style="margin-top:12px">“응”이라고 말해도 돼요</div>
     </div>
     <div class="btn primary big" data-co="2" style="width:300px">{ic("navi",40)} 안내 시작</div>
     <div class="btn big" data-co="3" style="width:200px">닫기</div>
     <div class="timebar live" data-co="4" style="--t:10s"><i></i></div>
   </div>
 </div>
 ''',
 [('1','알림 카드','가게 이름·저장 시기·거리·방향. TTS “…300m 앞이에요. 갈까요?”','10초 뒤 자동으로 닫힘'),
  ('2','안내 시작','내비 목적지 설정 (requestRoute) → S-05b','음성 “응”과 같음'),
  ('3','닫기','오늘은 다시 알리지 않음','음성 “아니”·무응답과 같음'),
  ('4','남은 시간 막대','카드 아래 가장자리, 10초 동안 줄어듦','')],
 [('알림 카드','위에서 32dp 내려오며 페이드 인','240ms · emphasized','500m 진입'),
  ('남은 시간 막대','가로 길이가 100 → 0%','10s · linear','TTS 끝난 뒤'),
  ('닫힘','위로 32dp 올라가며 페이드 아웃','200ms','닫기 · 시간 끝 · 아니')],
 '차량 알림(heads-up) 형태. 같은 가게는 하루 1회. 설정에서 식사 시간에만 받을 수 있다.', gif=True)

screen('S-05b','C','리마인드 · 안내 시작','주행 중','S-05에서 안내 시작 · 음성 “응”','F-10',
 f'''
 <div class="body full" style="padding:0">
   <div class="mapph" style="position:absolute;inset:0;border:0;border-radius:0"><div class="lab bot" data-co="2" style="left:48px">내비 화면으로 전환 중</div><div class="pin" style="left:760px;top:420px">{ic("pin",72)}</div><div class="pin me" style="left:520px;top:640px">{ic("navi",56)}</div></div>
   <div class="card a-pop" data-co="1" style="position:absolute;left:48px;right:48px;top:48px;height:140px;display:flex;align-items:center;gap:24px;padding:0 40px;background-color:var(--glass2)">
     {ic("check",48,"ok")}<div class="h40">서울갈비로 안내할게요</div><div class="muted" style="margin-left:auto;font-size:28px">내비로 넘어가요</div>
   </div>
 </div>
 ''',
 [('1','확인 문구','TTS로 같은 문장. 1.5초 뒤 사라짐',''),
  ('2','내비 화면','Pleos 내비가 목적지 안내를 시작함','도착하면 F-11 방문 기록 → S-15')],
 [('카드 줄어듦','높이 300 → 140dp, 버튼 사라짐','200ms · standard','응답 직후'),
  ('카드 닫힘','위로 올라가며 페이드','200ms','1.5초 뒤')],
 '')

# ===== D. 추천 =====
def rcard(n, name, meta, note, co1=False, co2=False, cls=''):
    return f'''<div class="card {cls}" style="--i:{n-1};width:368px;height:640px;display:flex;flex-direction:column;gap:20px;padding:36px 32px"{' data-co="1"' if co1 else ''}>
      <div class="num">{n}</div>
      <div class="h40">{name}</div>
      <div style="font-size:28px">{meta}</div>
      <div class="muted small" style="flex:1">{note}</div>
      <div class="btn primary"{' data-co="2"' if co2 else ''}>{ic("navi",36)} 안내 시작</div>
    </div>'''
screen('S-06','D','추천','주행 중 · 정차 중','S-01 뭐 먹을까 / 음성 “뭐 먹을까”','F-09, F-10',
 f'''
 {top('뭐 먹을까')}
 <div class="body" style="display:flex;flex-direction:column;gap:40px;align-items:center;padding-top:40px">
   <div style="display:flex;gap:40px">
     {rcard(1,'서울갈비','고깃집 · 1.2km','저장 11월 2일 · 안 가 봄', co1=True, co2=True, cls='a-up')}
     {rcard(2,'카페 노을','카페 · 2.0km','방문 1회 · “괜찮았어”', cls='a-up d1')}
     {rcard(3,'국밥집','국밥 · 3.4km','저장 10월 28일', cls='a-up d2')}
   </div>
   <div class="btn" data-co="3" style="width:400px">다른 곳 추천</div>
 </div>
 ''',
 [('1','추천 카드 3장','번호·이름·업종·거리·메모. 거리는 말한 곳 기준이고 화면이 열린 뒤 다시 계산하지 않음. TTS는 번호와 이름만 읽음','“2번” 또는 이름을 말하면 선택 → S-06c'),
  ('2','안내 시작','카드별 1개','누르면 S-06c'),
  ('3','다른 곳 추천','다음 순위 3곳으로 교체','최대 2회. 더 없으면 S-06b'),
  ('b','뒤로','홈','')],
 [('카드 3장','아래에서 24dp 올라오며 차례로 등장(60ms 간격)','200ms × 3 · standard','화면 열릴 때'),
  ('읽는 카드 강조','TTS가 번호를 읽는 동안 그 카드 테두리만 주황','120ms','카드마다 차례로'),
  ('다른 곳 추천','세 장이 왼쪽으로 나가고 새 세 장이 오른쪽에서 들어옴','200ms','버튼 · 음성 “다른 데”')],
 '', gif=True)

screen('S-06b','D','추천할 가게 없음','주행 중 · 정차 중','반경 안에 저장 가게가 없을 때','F-09',
 f'''
 {top('뭐 먹을까')}
 <div class="body" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px">
   <div class="a-fade" data-co="1" style="display:flex;flex-direction:column;align-items:center;gap:24px">{ic("pin",120,"muted")}<div class="h48">근처 10km 안에 저장한 가게가 없어요</div><div class="muted" style="font-size:32px">운전하다 마음에 드는 가게를 말해 두세요</div></div>
   <div class="btn primary" data-co="2" style="width:440px;height:112px;font-size:36px">30km까지 넓혀 보기</div>
 </div>
 ''',
 [('1','안내','한 줄 + TTS 동일 문장',''),
  ('2','30km까지 넓혀 보기','반경을 넓혀 S-06 다시','그래도 없으면 버튼이 사라지고 “저장한 가게가 없어요”'),
  ('b','뒤로','홈','')],
 [('안내','페이드 인','160ms','화면 열릴 때')],
 '')

screen('S-06c','D','추천 · 선택됨','주행 중 · 정차 중','S-06에서 카드 선택 (터치 · 번호 · 이름)','F-09, F-10',
 f'''
 {top('뭐 먹을까')}
 <div class="body" style="display:flex;flex-direction:column;gap:40px;align-items:center;padding-top:40px">
   <div style="display:flex;gap:40px">
     {rcard(1,'서울갈비','고깃집 · 1.2km','저장 11월 2일 · 안 가 봄', co1=True, cls='a-grow').replace('class="card a-grow"','class="card a-grow" data-sel="1"')}
     {rcard(2,'카페 노을','카페 · 2.0km','방문 1회 · “괜찮았어”', cls='a-dim')}
     {rcard(3,'국밥집','국밥 · 3.4km','저장 10월 28일', cls='a-dim')}
   </div>
   <div class="btn a-dim" style="width:400px">다른 곳 추천</div>
 </div>
 <div class="toast live" data-co="2" style="top:920px">{ic("navi",44,"ok")}<span>서울갈비로 안내할게요</span></div>
 <style>.frame [data-sel]{{border:2px solid var(--accent);box-shadow:0 0 0 6px var(--acc-ring),var(--shadow)}}.frame [data-sel] .a-dim{{opacity:1}}</style>
 ''',
 [('1','선택한 카드','테두리 주황 + 4% 확대, 나머지 카드는 흐려짐','1.5초 뒤 홈으로 돌아가고 내비가 안내 시작'),
  ('2','안내 토스트','“서울갈비로 안내할게요” + TTS 동일',''),
  ('b','뒤로','홈 (선택 취소)','')],
 [('선택 카드','100 → 104% 확대, 테두리 주황','200ms · standard','선택 직후'),
  ('나머지 카드','불투명도 100 → 40%','200ms','선택 카드와 동시'),
  ('토스트','아래에서 올라옴','200ms','선택 직후'),
  ('홈으로','페이드','160ms','1.5초 뒤')],
 '')

# ===== E. 방문 기록 =====
screen('S-15','E','방문 메모','주행 중 · 정차 중','내비 도착 + 10분 체류 뒤 다시 출발할 때 (차량 알림)','F-11',
 f'''
 <div class="body full" style="padding:0">
   <div class="mapph" style="position:absolute;inset:0;border:0;border-radius:0"><div class="lab bot">현재 화면 (내비 등)</div></div>
   <div class="card a-down" data-co="1" style="position:absolute;left:48px;right:48px;top:48px;height:300px;display:flex;align-items:center;gap:32px;padding:36px 40px;background-color:var(--glass2)">
     {ic("flag",72)}
     <div style="flex:1">
       <div class="h40">서울갈비 다녀오셨네요</div>
       <div style="font-size:32px;margin-top:8px">방문 1회로 기록했어요 · 한 줄 남길까요?</div>
       <div class="muted small" style="margin-top:12px">“괜찮았어”처럼 바로 말해도 돼요</div>
     </div>
     <div class="btn primary big" data-co="2" style="width:300px">{ic("mic",40)} 말하기</div>
     <div class="btn big" data-co="3" style="width:200px">다음에</div>
     <div class="timebar live" data-co="4" style="--t:10s"><i></i></div>
   </div>
 </div>
 ''',
 [('1','알림 카드','가게 이름, 방문 횟수 기록됨, 질문. TTS “…다녀오셨네요. 한 줄 남길까요?”','방문 기록 자체는 이미 저장됨'),
  ('2','말하기','S-02a로 메모 한 문장 받음 → S-15b','바로 말하면 버튼 없이 진행'),
  ('3','다음에','메모 없이 닫힘','무응답 10초와 같음'),
  ('4','남은 시간 막대','10초','')],
 [('알림 카드','위에서 32dp 내려오며 페이드 인','240ms · emphasized','출발 감지'),
  ('남은 시간 막대','100 → 0%','10s · linear','TTS 끝난 뒤'),
  ('닫힘','위로 올라가며 페이드 아웃','200ms','')],
 '리마인드(S-05)와 같은 틀을 쓴다. 주행 중이면 TTS와 음성만으로 끝낸다.', gif=True)

screen('S-15b','E','방문 메모 저장','주행 중 · 정차 중','S-15에서 말한 뒤','F-11',
 home_bg(dim=.7, recent=recent_card('서울갈비','<span class="chip ok">방문 1회</span>', co='2', cls='a-fade')) +
 f'''<div class="toast live" data-co="1" style="top:900px">{ic("check",48,"ok")}<span>메모 남겼어요 · “괜찮았어”</span></div>''',
 [('1','토스트','메모 문장 포함, 4초','TTS “남겼어요”'),
  ('2','최근 저장 카드','칩이 “방문 1회”로','상세(S-08)에 메모가 쌓임')],
 [('토스트','S-04a와 같음','200ms / 200ms',''),
  ('칩 교체','페이드','200ms','')],
 '')

# ===== F. 내 맛집 =====
def lrow(name, meta, chip, sel=False, co=None, cls='', i=0):
    c = f' data-co="{co}"' if co else ''
    return f'''<div class="row{' sel' if sel else ''} {cls}"{c} style="--i:{i}">{ic("pin",40,"muted")}<div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div>{chip}</div>'''
ROWS = [('서울갈비','고깃집 · 1.2km · 11월 2일','<span class="chip warn">미확인</span>'),
        ('카페 노을','카페 · 2.0km · 방문 1회','<span class="chip ok">방문</span>'),
        ('국밥집','국밥 · 3.4km · 10월 28일',''),
        ('빵집 (이름 없음)','4.1km · 10월 25일','<span class="chip warn">미확인</span>'),
        ('할머니 손칼국수','칼국수 · 6.8km · 방문 2회','<span class="chip ok">방문</span>'),
        ('파스타 집','양식 · 8.2km · 10월 12일','')]
def rows(skip=None, hl=None, sel0=True, collapse=None):
    out = []
    for i, (n, m, c) in enumerate(ROWS):
        if skip is not None and i == skip: continue
        cls = 'a-stag'
        if hl is not None and i in hl: cls = 'a-hl'
        if collapse is not None and i == collapse: cls = 'a-collapse'
        out.append(lrow(n, m, c, sel=(sel0 and i == 0), cls=cls, i=i))
    return ''.join(out)
def myplaces_map(co=None, pins=True):
    c = f' data-co="{co}"' if co else ''
    p = (f'<div class="pin" style="left:420px;top:300px">{ic("pin",64)}</div><div class="pin" style="left:180px;top:220px">{ic("pin",64)}</div>'
         f'<div class="pin" style="left:520px;top:560px">{ic("pin",64)}</div><div class="pin" style="left:140px;top:640px;color:var(--amber)">{ic("pin",64)}</div>') if pins else ''
    return f'''<div class="mapph"{c} style="height:100%"><div class="lab">지도</div><div class="pin me" style="left:320px;top:420px">{ic("navi",56)}</div>{p}</div>'''

screen('S-07','F','내 맛집','정차 중','S-01 내 맛집','F-12, F-07, F-13',
 f'''
 {top('내 맛집', right=f'<div class="textbtn" data-co="4">{ic("share",36)} 공유</div>')}
 <div class="body a-slidex" style="display:grid;grid-template-columns:560px 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:20px;min-height:0">
     <div style="display:flex;gap:12px" data-co="1"><span class="chip on">전체 12</span><span class="chip">미확인 3</span><span class="chip">방문 4</span><span class="chip">가까운 순</span></div>
     <div data-co="2" style="padding-top:8px"><div style="overflow:hidden">{rows()}</div></div>
   </div>
   {myplaces_map(co='3')}
 </div>
 ''',
 [('1','필터 칩','전체 / 미확인 / 방문, 정렬(가까운 순·최근 저장 순)','누르면 목록만 바뀜'),
  ('2','가게 목록','이름·업종·거리·저장일 또는 방문 횟수·상태 칩, 줄 높이 104dp','누르면 S-08, 미확인은 S-09. 0건이면 S-07b'),
  ('3','지도','저장 가게 핀(주황), 미확인 핀(노랑), 내 위치','핀을 누르면 목록에서 선택됨. 지도 SDK가 안 되면 비움'),
  ('4','공유','S-10',''),
  ('b','뒤로','홈','')],
 [('화면 전환','정차 화면끼리는 오른쪽 48dp에서 밀려 들어오며 페이드','200ms · standard','S-01 → S-07'),
  ('목록 줄','위에서부터 차례로 페이드 인(40ms 간격, 최대 8줄)','200ms','화면 열릴 때 · 필터 바꿀 때'),
  ('줄 선택','배경이 주황 톤으로','120ms','터치 · 지도 핀 터치'),
  ('지도 핀 선택','핀이 8dp 튀어 오름','200ms','목록 줄 터치')],
 '주행 중에 열면 S-12가 대신 뜬다.')

screen('S-07b','F','내 맛집 · 저장 전','정차 중','저장 가게 0개일 때 S-01b 내 맛집','F-12',
 f'''
 {top('내 맛집')}
 <div class="body" style="display:grid;grid-template-columns:560px 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:32px;text-align:center">
     <div data-co="1" style="display:flex;flex-direction:column;align-items:center;gap:20px">{ic("pin",96,"muted")}<div class="h40">아직 저장한 가게가 없어요</div><div class="muted">운전하다 말해 보세요<br>“방금 지난 가게 저장해줘”</div></div>
     <div class="btn primary" data-co="2" style="width:320px;height:112px;font-size:36px">{ic("mic",44)} 말하기</div>
   </div>
   {myplaces_map(co='3', pins=False)}
 </div>
 ''',
 [('1','안내','2줄 + 예시 문장',''),
  ('2','말하기','S-02a',''),
  ('3','지도','내 위치만',''),
  ('b','뒤로','홈','')],
 [('화면 전환','S-07과 같음','200ms','')],
 '')

screen('S-08','F','가게 상세','정차 중','S-07 목록 / 홈 최근 저장','F-10, F-11, F-12',
 f'''
 {top('서울갈비', right=f'<div class="iconbtn" data-co="4">{ic("edit",44)}</div>')}
 <div class="body a-slidex" style="display:grid;grid-template-columns:1fr 480px;grid-template-rows:1fr auto;gap:40px;padding:40px 48px">
   <div style="display:flex;flex-direction:column;gap:28px;padding:20px 0 0 24px" data-co="1">
     <div style="font-size:32px">고깃집 · 1.2km</div>
     <div class="muted">서울 강남구 강남대로 123</div>
     <div style="display:flex;gap:12px"><span class="chip">저장 11월 2일</span><span class="chip ok">방문 1회</span></div>
     <div class="card" style="padding:24px 28px"><div class="small muted">메모</div><div style="font-size:32px">줄 서 있던 집, 괜찮았어</div></div>
     <div class="muted small">저장할 때 한 말 · “방금 지난 빨간 간판 고깃집 저장해줘”</div>
   </div>
   <div class="mapph" data-co="2"><div class="lab">위치</div><div class="pin a-drop" style="left:200px;top:140px">{ic("pin",72)}</div></div>
   <div style="grid-column:1 / -1;display:flex;gap:24px;align-items:center">
     <div class="btn primary big" data-co="3" style="width:400px">{ic("navi",40)} 안내 시작</div>
     <div class="btn big" data-co="6" style="width:380px">{ic("share",40)} 공유에 담기</div>
     <div style="flex:1"></div>
     <div class="btn big danger" data-co="5" style="width:200px">{ic("trash",36)} 삭제</div>
   </div>
 </div>
 ''',
 [('1','정보','업종·거리·주소·저장일·방문 횟수·메모·저장할 때 한 말',''),
  ('2','위치 지도','핀 1개',''),
  ('3','안내 시작','내비 목적지 설정 → 홈 + 토스트 “안내할게요”',''),
  ('4','수정','S-08b',''),
  ('5','삭제','S-08c 확인 창','다른 버튼과 떨어뜨려 배치'),
  ('6','공유에 담기','S-10 목록에 추가. 누르면 “담았어요”로 바뀜',''),
  ('b','뒤로','S-07 (홈에서 왔으면 홈)','')],
 [('화면 전환','오른쪽 48dp에서 밀려 들어오며 페이드','200ms · standard','S-07 → S-08'),
  ('지도 핀','위에서 떨어지며 튕김','400ms · emphasized','화면 열릴 때'),
  ('공유에 담기','버튼 글자가 체크 + “담았어요”로 페이드 교체','160ms','누른 직후')],
 '')

screen('S-08b','F','가게 수정','정차 중','S-08 수정','F-12',
 f'''
 {top('수정')}
 <div class="body a-slidex" style="padding:32px 48px 0;display:grid;grid-template-columns:1fr 1fr;gap:32px 48px;align-content:start">
   <div><div class="small muted" style="margin-bottom:8px">이름</div><div class="input focus" data-co="1">서울갈비<span class="caret"></span></div></div>
   <div><div class="small muted" style="margin-bottom:8px">업종</div><div data-co="2" style="display:flex;gap:12px;flex-wrap:wrap"><span class="chip on" style="height:76px;padding:0 28px;font-size:28px">고깃집</span><span class="chip" style="height:76px;padding:0 28px;font-size:28px">카페</span><span class="chip" style="height:76px;padding:0 28px;font-size:28px">국밥</span><span class="chip" style="height:76px;padding:0 28px;font-size:28px">양식</span><span class="chip" style="height:76px;padding:0 28px;font-size:28px">기타</span></div></div>
   <div style="grid-column:1 / -1"><div class="small muted" style="margin-bottom:8px">메모</div><div class="input" data-co="3">줄 서 있던 집, 괜찮았어</div></div>
   <div style="grid-column:1 / -1;display:flex;gap:24px;align-items:center">
     <div class="btn" data-co="4" style="width:360px">{ic("search",36)} 후보 다시 고르기</div>
     <div style="flex:1"></div>
     <div class="btn" data-co="6" style="width:200px">취소</div>
     <div class="btn primary" data-co="5" style="width:260px">{ic("check",36)} 저장</div>
   </div>
 </div>
 <div class="kbd a-up"><span data-co="7" style="display:flex;align-items:center;gap:16px">{ic("keyboard",40)} 차량 키보드 (시스템)</span></div>
 ''',
 [('1','이름','글자 입력. 비우면 저장 불가',''),
  ('2','업종','칩에서 하나 고름',''),
  ('3','메모','한 줄',''),
  ('4','후보 다시 고르기','S-09로 가서 다른 가게로 바꿈',''),
  ('5','저장','S-08로 돌아감 + 토스트 “수정했어요”',''),
  ('6','취소','바뀐 내용 버림',''),
  ('7','키보드 자리','입력창을 누르면 시스템 키보드가 아래 320dp를 차지','주행 중이면 S-12'),
  ('b','뒤로','취소와 같음','')],
 [('화면 전환','오른쪽에서 밀려 들어옴','200ms','S-08 → S-08b'),
  ('입력창 포커스','테두리가 주황으로, 바깥 4dp 번짐','120ms','입력창 터치'),
  ('키보드','아래에서 올라옴 (시스템)','240ms','입력창 터치')],
 '')

screen('S-08c','F','삭제 확인','정차 중','S-08 삭제 / S-09 삭제','F-12',
 dialog('서울갈비를 삭제할까요?', '방문 기록과 메모도 함께 지워져요. 4초 안에는 되돌릴 수 있어요.', ic("trash",36)+' 삭제',
        bg=f'''{top('서울갈비', back=False, right=f'<div class="iconbtn">{ic("edit",44)}</div>')}<div class="body" style="opacity:.5"><div class="card" style="height:100%"></div></div>'''),
 [('1','질문','가게 이름을 넣은 한 줄',''),
  ('2','설명','무엇이 함께 지워지는지, 되돌리기 안내',''),
  ('3','삭제','빨강 채움 버튼. 삭제 → S-08d','빨강은 이 버튼과 설정의 모두 삭제에만'),
  ('4','취소','창 닫기','')],
 [('어두운 배경','페이드 인','160ms','삭제 누름'),
  ('창','96% → 100% + 페이드','200ms · standard','배경과 함께'),
  ('닫힘','페이드 아웃','160ms','')],
 '', gif=True)

screen('S-08d','F','삭제 뒤 되돌리기','정차 중','S-08c 삭제 뒤 (목록으로 돌아옴)','F-12',
 f'''
 {top('내 맛집', right=f'<div class="textbtn">{ic("share",36)} 공유</div>')}
 <div class="body" style="display:grid;grid-template-columns:560px 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:20px;min-height:0">
     <div style="display:flex;gap:12px"><span class="chip on">전체 11</span><span class="chip">미확인 2</span><span class="chip">방문 4</span><span class="chip">가까운 순</span></div>
     <div data-co="2" style="padding-top:8px"><div style="overflow:hidden">{rows(collapse=0, sel0=False)}</div></div>
   </div>
   {myplaces_map()}
 </div>
 <div class="toast live" data-co="1" style="top:900px">{ic("trash",44,"muted")}<span>삭제했어요 · 서울갈비</span><div class="act">{ic("undo",32)} 되돌리기</div></div>
 ''',
 [('1','토스트 + 되돌리기','4초. 되돌리기를 누르면 줄이 제자리에 다시 생김','되돌리기 버튼은 삭제 뒤에만 씀'),
  ('2','목록','지운 줄이 접히며 사라짐, 개수 칩 갱신',''),
  ('b','뒤로','홈','')],
 [('지운 줄','높이 104 → 0dp로 접히며 페이드','200ms · standard','삭제 직후'),
  ('토스트','아래에서 올라옴, 4초 뒤 사라짐','200ms / 200ms',''),
  ('되돌리기','줄이 다시 펼쳐짐','200ms','되돌리기 누름')],
 '', gif=True)

def crow(name, meta, on=False, co=None):
    c = f' data-co="{co}"' if co else ''
    return f'''<div class="row{' sel' if on else ''}"{c} style="height:112px"><div class="radio{' on' if on else ''}"></div><div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div></div>'''
screen('S-09','F','미확인 가게 정리','정차 중','S-07 미확인 항목 / S-08b 후보 다시 고르기 / 홈 최근 저장(미확인)','F-07',
 f'''
 {top('어느 가게였나요?')}
 <div class="body a-slidex" style="display:grid;grid-template-columns:1fr 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:24px">
     <div class="card" data-co="1" style="padding:28px 32px"><div class="small muted">11월 7일 18:42 · 강남대로 (북쪽으로 이동 중)</div><div style="font-size:32px;margin-top:8px">“방금 지난 빨간 간판 고깃집 저장해줘”</div></div>
     <div class="mapph" style="flex:1"><div class="lab">그때 위치</div><div class="pin me" style="left:260px;top:260px">{ic("navi",56)}</div><div class="pin a-grow" style="left:330px;top:200px">{ic("pin",64)}</div><div class="pin" style="left:380px;top:320px">{ic("pin",56)}</div><div class="pin" style="left:190px;top:340px">{ic("pin",56)}</div></div>
   </div>
   <div style="display:flex;flex-direction:column;gap:24px">
     <div data-co="2">
       {crow('서울갈비','고깃집 · 80m 오른쪽', on=True)}
       {crow('명품갈비','고깃집 · 140m 오른쪽')}
       {crow('왕돼지','고깃집 · 160m 왼쪽')}
       {crow('빨간모자 치킨','치킨 · 90m 오른쪽')}
       {crow('강남 정육식당','고깃집 · 210m 오른쪽')}
     </div>
     <div style="flex:1"></div>
     <div class="btn primary big" data-co="3">{ic("check",40)} 이걸로 저장</div>
     <div style="display:flex;gap:24px"><div class="btn" data-co="4" style="flex:1">{ic("search",36)} 직접 검색</div><div class="btn danger" data-co="5" style="width:200px">삭제</div></div>
   </div>
 </div>
 ''',
 [('1','그때 상황','저장 시각·위치·진행 방향·말한 문장','판단 근거'),
  ('2','후보 5곳','가까운 순, 업종·거리·좌우. 기본 선택은 1순위','하나 고르면 지도 핀도 강조'),
  ('3','이걸로 저장','선택한 후보로 확정 → S-08 + 토스트 “저장했어요”',''),
  ('4','직접 검색','S-09b',''),
  ('5','삭제','S-08c 확인 창',''),
  ('b','뒤로','S-07','')],
 [('화면 전환','오른쪽에서 밀려 들어옴','200ms',''),
  ('후보 선택','라디오 채움 + 줄 배경','120ms','줄 터치'),
  ('지도 핀 강조','선택한 후보의 핀이 120%로','200ms · standard','줄 터치와 동시')],
 '')

def srow(name, meta, on=False, dist=''):
    return f'''<div class="row" style="height:96px">{ic("pin",36,"muted")}<div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div><span class="muted small">{dist}</span>{'<div class="radio on"></div>' if on else '<div class="radio"></div>'}</div>'''
screen('S-09b','F','직접 검색','정차 중','S-09 직접 검색','F-07',
 f'''
 {top('가게 이름으로 찾기')}
 <div class="body" style="padding:24px 48px 0;display:flex;flex-direction:column;gap:24px">
   <div class="input focus" data-co="1">{ic("search",40,"muted")}갈비<span class="caret"></span><span class="muted" style="margin-left:auto;font-size:24px">그때 위치 2km 안</span></div>
   <div data-co="2" class="a-fade">
     {srow('서울갈비','고깃집 · 강남대로 123', on=True, dist='80m')}
     {srow('명품갈비','고깃집 · 강남대로 131', dist='140m')}
     {srow('갈비명가','고깃집 · 테헤란로 12', dist='620m')}
        </div>
   <div style="display:flex;gap:24px;align-items:center;margin-top:8px">
     <div style="flex:1"></div>
     <div class="btn primary" data-co="3" style="width:300px">{ic("check",36)} 이걸로 저장</div>
   </div>
 </div>
 <div class="kbd a-up"><span data-co="4" style="display:flex;align-items:center;gap:16px">{ic("keyboard",40)} 차량 키보드 (시스템)</span></div>
 ''',
 [('1','검색창','이름 일부로 검색. 그때 위치 2km 안을 먼저 보여 줌',''),
  ('2','결과 목록','이름·업종·주소·거리, 하나 선택','한 글자 칠 때마다 갱신'),
  ('3','이걸로 저장','S-08 + 토스트 “저장했어요”',''),
  ('4','키보드 자리','시스템 키보드 320dp',''),
  ('b','뒤로','S-09','')],
 [('결과 목록','갱신될 때 페이드 교체','160ms','글자 입력마다'),
  ('키보드','아래에서 올라옴','240ms','화면 열릴 때')],
 '')

# ===== G. 공유 =====
def chk(name, meta, on=True):
    return f'''<div class="row" style="height:96px"><div class="check{' on' if on else ''}">{ic("check",32) if on else ''}</div><div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div></div>'''
screen('S-10','G','맛집 지도 공유','정차 중','S-07 공유 / S-08 공유에 담기','F-13',
 f'''
 {top('맛집 지도 공유')}
 <div class="body a-slidex" style="display:grid;grid-template-columns:520px 1fr;gap:48px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:16px">
     <div style="display:flex;align-items:center;gap:16px;padding-left:4px" data-co="1"><span class="chip on">전체 선택</span><span class="muted small">7곳 선택됨 · 미확인 가게는 뺌</span></div>
     <div>
       {chk('서울갈비','고깃집')}{chk('카페 노을','카페 · 방문 1회')}{chk('국밥집','국밥')}{chk('할머니 손칼국수','칼국수 · 방문 2회')}{chk('파스타 집','양식')}{chk('두부마을','한식', on=False)}
     </div>
   </div>
   <div style="display:flex;flex-direction:column;gap:28px">
     <div><div class="small muted" style="margin-bottom:8px">제목</div><div class="input" data-co="2">재원의 강릉 맛집 7곳</div></div>
     <div style="display:flex;gap:32px;align-items:center">
       <div class="qr a-fade" data-co="3" style="width:240px;height:240px"></div>
       <div style="flex:1;display:flex;flex-direction:column;gap:20px">
         <div class="muted small" style="word-break:break-all">nextplace.app/s/8Kd2…</div>
         <div class="btn primary" data-co="4">링크 복사</div>
         <div class="btn">{ic("share",36)} 보내기</div>
       </div>
     </div>
     <div class="card" data-co="5" style="padding:24px 28px"><div class="small muted">받는 사람 화면</div><div style="font-size:28px;margin-top:6px">링크를 열면 가게 목록이 보이고, 앱이 있으면 <b>내 지도에 추가</b> 버튼이 뜬다 (S-16).</div></div>
   </div>
 </div>
 ''',
 [('1','가게 고르기','체크 목록, 전체 선택. 미확인 가게는 목록에 없음','고를 때마다 링크·QR이 다시 만들어짐'),
  ('2','제목','기본값 “이름의 맛집 N곳”, 정차 중 키보드로 수정',''),
  ('3','QR','동승자가 폰으로 바로 찍음',''),
  ('4','링크 복사 / 보내기','보내기는 차량의 다른 앱(문자 등)으로. 링크에 가게 이름·업종·주소·좌표·메모만 담김','방문 기록·차량 위치는 안 들어감'),
  ('5','받는 사람','웹 목록 → 앱 있으면 S-16 가져오기',''),
  ('b','뒤로','S-07','')],
 [('화면 전환','오른쪽에서 밀려 들어옴','200ms',''),
  ('체크 토글','채움 + 체크 표시','120ms','줄 터치'),
  ('QR','다시 만들어질 때 페이드 교체','200ms','선택·제목 바뀔 때'),
  ('링크 복사','버튼 글자가 체크 + “복사했어요”로 1.5초 바뀜','160ms','누른 직후')],
 '')

screen('S-16','G','받은 목록 가져오기','정차 중','공유 링크를 차량에서 열었을 때 / S-10 받는 사람','F-13',
 f'''
 {top('받은 맛집 목록')}
 <div class="body a-slidex" style="display:grid;grid-template-columns:1fr 480px;grid-template-rows:auto 1fr auto;gap:28px 40px;padding:32px 48px">
   <div data-co="1" style="grid-column:1 / -1"><div class="h40">재원의 강릉 맛집 7곳</div><div class="muted">보낸 사람 재원 · 11월 9일</div></div>
   <div data-co="2"><div style="overflow:hidden">
     {chk('서울갈비','고깃집 · 강릉')}{chk('카페 노을','카페')}{chk('국밥집','국밥')}{chk('할머니 손칼국수','칼국수')}{chk('파스타 집','양식').replace('<div style="flex:1">','<div style="flex:1">').replace('</div></div></div>','</div></div><span class="chip dim">이미 있음</span></div>')}
   </div></div>
   <div class="mapph" data-co="3"><div class="lab">위치</div><div class="pin" style="left:180px;top:160px">{ic("pin",56)}</div><div class="pin" style="left:300px;top:260px">{ic("pin",56)}</div><div class="pin" style="left:120px;top:380px">{ic("pin",56)}</div><div class="pin" style="left:340px;top:420px">{ic("pin",56)}</div></div>
   <div style="grid-column:1 / -1;display:flex;gap:24px;align-items:center">
     <div class="muted">6곳을 추가해요 · 이미 있는 1곳은 건너뜀</div>
     <div style="flex:1"></div>
     <div class="btn" data-co="5" style="width:200px">닫기</div>
     <div class="btn primary big" data-co="4" style="width:400px">{ic("download",40)} 내 지도에 추가</div>
   </div>
 </div>
 ''',
 [('1','제목 · 보낸 사람','링크에 담긴 제목과 날짜',''),
  ('2','목록','체크로 고름. 이미 있는 가게는 “이미 있음” 칩과 함께 빠짐',''),
  ('3','지도','받은 가게 핀',''),
  ('4','내 지도에 추가','선택한 가게를 저장 → S-16b','받은 가게는 방문 기록 없이 “저장됨”'),
  ('5','닫기','저장 안 함',''),
  ('b','뒤로','닫기와 같음','')],
 [('화면 전환','오른쪽에서 밀려 들어옴','200ms',''),
  ('추가 누름','줄마다 체크가 차례로 채워짐(40ms 간격)','120ms × N','추가 직후')],
 '차량에서 링크를 열 브라우저가 없으면 폰에서 보는 웹 목록만 쓴다(기획서 9장 결정 대기).')

screen('S-16b','G','가져오기 완료','정차 중','S-16 내 지도에 추가 뒤','F-13',
 f'''
 {top('내 맛집', right=f'<div class="textbtn">{ic("share",36)} 공유</div>')}
 <div class="body" style="display:grid;grid-template-columns:560px 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:20px;min-height:0">
     <div style="display:flex;gap:12px"><span class="chip on">전체 18</span><span class="chip">미확인 3</span><span class="chip">방문 4</span><span class="chip">최근 저장 순</span></div>
     <div data-co="2" style="padding-top:8px"><div style="overflow:hidden">
       {lrow('서울갈비 (강릉)','고깃집 · 방금 추가','<span class="chip">저장됨</span>', cls='a-hl')}
       {lrow('카페 노을','카페 · 방금 추가','<span class="chip">저장됨</span>', cls='a-hl')}
       {lrow('국밥집','국밥 · 방금 추가','<span class="chip">저장됨</span>', cls='a-hl')}
       {lrow('할머니 손칼국수','칼국수 · 방금 추가','<span class="chip">저장됨</span>', cls='a-hl')}
       {lrow('빵집 (이름 없음)','4.1km · 10월 25일','<span class="chip warn">미확인</span>')}
       {lrow('파스타 집','양식 · 8.2km · 10월 12일','')}
     </div></div>
   </div>
   {myplaces_map()}
 </div>
 <div class="toast live" data-co="1" style="top:900px">{ic("check",48,"ok")}<span>6곳을 내 지도에 추가했어요</span></div>
 ''',
 [('1','토스트','추가한 개수, 4초',''),
  ('2','새 줄 강조','추가된 줄 배경이 주황 톤에서 서서히 원래대로','정렬은 최근 저장 순으로 바뀜'),
  ('b','뒤로','홈','')],
 [('새 줄 배경','주황 톤 → 투명','1.2s · ease-out','목록 열릴 때'),
  ('토스트','아래에서 올라옴','200ms','')],
 '')

# ===== H. 설정과 주행 제한 =====
def setrow(label, ctrl, co=None, danger=False):
    c = f' data-co="{co}"' if co else ''
    return f'<div class="row"{c} style="height:112px;{"color:var(--danger)" if danger else ""}"><div class="name" style="flex:1">{label}</div>{ctrl}</div>'
screen('S-11','H','설정','정차 중','S-01 설정','F-15',
 f'''
 {top('설정')}
 <div class="body a-slidex" style="padding:24px 48px;display:flex;flex-direction:column">
   {setrow('리마인드','<div class="toggle"></div>', co='1')}
   {setrow('알림 거리','<div class="seg"><div>300m</div><div class="on">500m</div><div>1km</div></div>', co='2')}
   {setrow('식사 시간에만 알림','<div class="toggle"></div>', co='3')}
   {setrow('음성 안내 음량','<div style="display:flex;align-items:center;gap:24px;width:480px">'+ic("volume",40,"muted")+'<div class="slider"></div></div>', co='4')}
   {setrow('가게를 특정할 때 쓰는 시간','<div class="seg"><div>10초</div><div class="on">20초</div><div>30초</div></div>', co='6')}
   <div style="flex:1"></div>
   {setrow('저장한 가게 모두 삭제','<div class="btn danger" style="height:76px">삭제</div>', co='5', danger=True)}
   <div class="muted small" style="padding:16px 24px 0">버전 0.1 · 저장 가게와 방문 기록은 이 차 안에만 보관</div>
 </div>
 ''',
 [('1','리마인드','켜기/끄기',''),
  ('2','알림 거리','300m / 500m / 1km',''),
  ('3','식사 시간에만 알림','11~14시, 17~20시',''),
  ('4','음성 안내 음량','',''),
  ('5','모두 삭제','S-11b 확인 창. 다른 항목과 떨어뜨림',''),
  ('6','가게를 특정할 때 쓰는 시간','말한 시점에서 얼마나 거슬러 올라가 위치를 잡을지 (10 / 20 / 30초)',''),
  ('b','뒤로','홈','')],
 [('화면 전환','오른쪽에서 밀려 들어옴','200ms',''),
  ('토글','손잡이가 좌우로 이동 + 배경색','160ms · standard','터치'),
  ('구간 선택','주황 배경이 옆 칸으로 미끄러짐','160ms','터치'),
  ('음량','손잡이가 손가락을 따라 즉시 이동','0ms','드래그')],
 '')

screen('S-11b','H','모두 삭제 확인','정차 중','S-11 모두 삭제','F-15',
 dialog('저장한 가게 12곳을 모두 삭제할까요?', '방문 기록과 메모도 함께 지워지고, 되돌릴 수 없어요.', ic("trash",36)+' 모두 삭제',
        bg=f'''{top('설정', back=False)}<div class="body" style="opacity:.5"></div>'''),
 [('1','질문','개수를 넣은 한 줄',''),
  ('2','경고','되돌릴 수 없음을 분명히',''),
  ('3','모두 삭제','빨강 채움. 삭제 뒤 S-01b','되돌리기 토스트 없음'),
  ('4','취소','창 닫기','')],
 [('창 열림','S-08c와 같음','160ms / 200ms',''),
  ('삭제 뒤','설정 → 홈(S-01b)으로 페이드','200ms','')],
 '')

screen('S-12','H','주행 중 안내','주행 중','주행 중 S-07 ~ S-11, S-16 진입 시','F-14',
 f'''
 {top('내 맛집')}
 <div class="body a-fade" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px">
   <div data-co="1" style="display:flex;flex-direction:column;align-items:center;gap:24px">{ic("car",120,"muted")}<div class="h48">멈추면 볼 수 있어요</div><div class="muted" style="font-size:32px">주행 중에는 음성으로 이용해 주세요</div></div>
   <div class="btn primary" data-co="2" style="width:400px;height:112px;font-size:36px">{ic("mic",44)} 말하기</div>
 </div>
 ''',
 [('1','안내','정차(주차 브레이크 또는 속도 0)하면 원래 화면으로 자동 전환','제목은 열려던 화면 이름'),
  ('2','말하기','S-02a',''),
  ('b','뒤로','홈','')],
 [('대신 뜰 때','페이드만 (이동 없음)','160ms','주행 중 정차 화면 진입'),
  ('정차 감지','원래 화면으로 페이드','200ms','주차 브레이크 · 속도 0 2초 유지'),
  ('주행 다시 시작','열려 있던 정차 화면이 이 화면으로 페이드','160ms','속도 > 0')],
 '')

# ===== 세로 창 (1칸) =====
# 재원 결정 2026-11-09: 세로 1칸에서는 말하기 버튼을 하단 가운데, 카드는 그 위에 쌓는다. 크기 640×1080은 가정.
PS = []
def pscreen(sid, base, name, wire, spec, note=''):
    PS.append(dict(id=sid, base=base, name=name, wire=wire, spec=spec, note=note))

def phome(recent='', count='12', sub1='근처 3곳 추천', sub2=None, co=None, live=False, dim=1, mic=True):
    co = co or {}
    c = lambda k: f' data-co="{co[k]}"' if k in co else ''
    sub2 = sub2 if sub2 is not None else f'{count}곳 · 미확인 3곳'
    st = f' style="opacity:{dim}"' if dim != 1 else ''
    talk = ''
    if mic:
        talk = (f'<div class="ptalk"{st}>'
                f'<div class="mic{" live" if live else ""}"{c("mic")}>{ic("mic",112)}</div>'
                f'<div class="h40" style="font-size:32px">눌러서 말하기</div></div>')
    return f'''
 <div class="top home"{st}>
   <div class="gpill brand">{BRAND_ICON}<span>다음에 가게</span></div>
   <div style="flex:1"></div>
   <div class="iconbtn"{c('gear')}>{ic("gear",40)}</div>
 </div>
 <div class="ploc"{st}><div class="gpill loc"{c('loc')}>{ic("navi",24,"acc")}<span>강남대로 · 북쪽</span></div></div>
 <div class="pcards"{st}>
   {recent}
   <div class="card hrow"{c('eat')}><span class="ib">{ic("fork",34,"acc")}</span><span class="tx"><span class="t">뭐 먹을까</span><span class="small muted">{sub1}</span></span>{ic("chev",32,"faint")}</div>
   <div class="card hrow"{c('mine')}><span class="ib">{ic("pin",34,"acc")}</span><span class="tx"><span class="t">내 맛집</span><span class="small muted">{sub2}</span></span>{ic("chev",32,"faint")}</div>
 </div>
 {talk}'''

pscreen('P-01', 'S-01', '홈',
 phome(recent=recent_card('서울갈비', '<span class="chip warn">미확인</span>', co='2'), live=True,
       co=dict(mic='1', eat='3', mine='4', gear='5', loc='6')),
 [('1','말하기 버튼','240dp 원형, 하단 가운데 고정','S-01과 같음 (누르면 P-02a)'),
  ('2','최근 저장','카드 묶음 맨 위','S-01과 같음'),
  ('3','뭐 먹을까','카드 높이 112dp','S-01과 같음'),
  ('4','내 맛집','카드 높이 112dp','S-01과 같음'),
  ('5','설정','오른쪽 위','S-11'),
  ('6','현재 위치','앱 이름 아래 한 줄로 내림','누를 수 없음')],
 '말하기 버튼이 손이 닿기 쉬운 아래쪽 가운데에 오고, 카드 3장은 그 위에 쌓인다. 가로 화면의 왼쪽 버튼 · 오른쪽 카드를 위아래로 바꾼 배치다.')

pscreen('P-02a', 'S-02a', '듣는 중',
 phome(recent=recent_card('서울갈비', '<span class="chip warn">미확인</span>'), mic=False) + f'''
 <div class="overlay a-fade"></div>
 <div class="card a-pop" style="position:absolute;left:32px;right:32px;top:176px;height:560px;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:48px 32px">
   <div class="h48 copad" data-co="1">듣고 있어요</div>
   <div data-co="2">{wave()}</div>
   <div style="font-size:40px;text-align:center;min-height:120px"><span data-co="3" style="padding:0 32px">방금 지난 <span class="a-text d1">빨간 간판</span> <span class="a-text d2">고깃집</span><span class="muted a-text d3">…</span></span></div>
 </div>
 <div class="pbottom"><div class="btn big" data-co="4" style="width:320px">{ic("close",40)} 취소</div></div>''',
 [('1','상태 문구','S-02a와 같음',''),
  ('2','소리 막대','17개, 폭 464dp',''),
  ('3','인식 중 문장','40dp, 두 줄까지',''),
  ('4','취소','말하기 버튼이 있던 하단 가운데 자리','누르면 녹음 중단')],
 '카드는 위, 누를 것은 아래 가운데라는 규칙을 겹침 화면에도 그대로 쓴다. 취소가 말하기 버튼 자리에 오므로 같은 손 위치에서 바로 멈출 수 있다.')

pscreen('P-03', 'S-03', '확인',
 f'''
 <div class="body full" style="padding:64px 32px 0;bottom:216px;display:flex;flex-direction:column;justify-content:space-between">
   <div class="h48" data-co="1" style="text-align:center">이 가게 맞나요?</div>
   <div class="card a-up" data-co="2" style="display:flex;flex-direction:column;gap:20px;padding:56px 40px">
     <div class="h64">서울갈비</div>
     <div style="font-size:32px">고깃집 · 120m 뒤 · 오른쪽</div>
     <div class="muted">서울 강남구 강남대로 123</div>
   </div>
   <div style="display:flex;align-items:center;gap:20px;color:var(--muted)">
     <span data-co="5" style="display:flex">{countdown(5)}</span>
     <span style="flex:1">5초 안에 대답이 없으면<br>위치만 저장해요</span>
     <span data-co="6" style="display:flex;align-items:center;gap:12px;color:var(--acc-t)">{ic("mic",32)}<span class="bars live"><i style="--i:0;height:20px"></i><i style="--i:1;height:40px"></i><i style="--i:2;height:28px"></i><i style="--i:3;height:44px"></i></span></span>
   </div>
 </div>
 <div class="pbottom">
   <div class="btn" data-co="4" style="width:272px;height:136px;font-size:40px">아니요</div>
   <div class="btn primary" data-co="3" style="width:272px;height:136px;font-size:40px">{ic("check",48)} 예</div>
 </div>''',
 [('1','질문','S-03과 같음',''),
  ('2','후보 카드','폭 576dp, 이름 64dp',''),
  ('3','예','하단 오른쪽, 높이 136dp','S-03과 같음'),
  ('4','아니요','하단 왼쪽, 높이 136dp','S-03과 같음'),
  ('5','대기 표시','카드 아래 한 줄','5초 지나면 위치만 저장'),
  ('6','듣는 중 표시','대기 표시 오른쪽','')],
 '예 · 아니요가 아래 가운데에 나란히 놓인다. 예는 오른쪽(확인 창의 실행 위치와 같음).')

pscreen('P-04a', 'S-04a', '저장 완료',
 phome(recent=recent_card('서울갈비', '<span class="chip ok">저장됨</span>', co='2', cls='a-fade'), dim=.7) +
 f'''<div class="toast live" data-co="1" style="top:560px">{ic("check",48,"ok")}<span>저장했어요 · 서울갈비</span></div>''',
 [('1','저장 토스트','말하기 버튼 바로 위(위에서 560dp)','4초 뒤 사라짐. 버튼을 가리지 않음'),
  ('2','최근 저장 카드','방금 저장한 가게, 칩 “저장됨”','')],
 '세로 창에서 토스트는 말하기 버튼을 가리지 않도록 버튼 바로 위에 뜬다. 가로 화면(아래쪽 900dp)과 다른 유일한 위치 규칙이다.')

def prcard(n, name, meta, note, co1=False, co2=False, cls=''):
    return f'''<div class="card {cls}" style="--i:{n-1};display:flex;align-items:center;gap:24px;height:220px;padding:0 28px"{' data-co="1"' if co1 else ''}>
      <div class="num">{n}</div>
      <div style="flex:1;min-width:0;display:flex;flex-direction:column;gap:6px">
        <div class="h40" style="font-size:36px">{name}</div>
        <div style="font-size:26px">{meta}</div>
        <div class="muted small">{note}</div>
      </div>
      <div class="btn primary" style="width:96px;height:96px;padding:0;border-radius:50%"{' data-co="2"' if co2 else ''}>{ic("navi",40)}</div>
    </div>'''

pscreen('P-06', 'S-06', '추천',
 f'''
 {top('뭐 먹을까')}
 <div style="position:absolute;left:32px;right:32px;top:128px;display:flex;flex-direction:column;gap:20px">
   {prcard(1,'서울갈비','고깃집 · 1.2km','안 가 봄', co1=True, co2=True, cls='a-up')}
   {prcard(2,'카페 노을','카페 · 2.0km','방문 1회', cls='a-up d1')}
   {prcard(3,'국밥집','국밥 · 3.4km','10월 28일 저장', cls='a-up d2')}
 </div>
 <div class="pbottom"><div class="btn" data-co="3" style="width:400px">다른 곳 추천</div></div>''',
 [('1','추천 카드 3장','위에서 아래로 1 · 2 · 3','S-06과 같음 (번호 · 이름으로 선택)'),
  ('2','안내 시작','카드 오른쪽 둥근 버튼 96dp','누르면 S-06c'),
  ('3','다른 곳 추천','하단 가운데','S-06과 같음'),
  ('b','뒤로','홈','')],
 '가로 화면의 카드 3장 가로 배치를 세로로 쌓고, 안내 시작 버튼은 글자 없이 아이콘만 둔다(폭이 좁아서). TTS가 “1번 서울갈비 …”로 읽으므로 번호는 크게 유지한다.')

# order screens by group then by declared order
GORDER = {g[0]: i for i, g in enumerate(GROUPS)}
S.sort(key=lambda s: GORDER[s['group']])

# ---------------------------------------------------------------- flow (generated SVG, one lane per group)
FLOWS = {
 'A': dict(nodes={'S-00':(0,0),'S-13':(1,0),'S-01':(2,0),'S-17':(1,1)},
           edges=[('S-00','S-13','처음 1회'),('S-13','S-01','허용'),('S-00','S-01',''),('S-01','S-17','권한 없음')]),
 'B': dict(nodes={'S-01':(0,0),'S-02a':(1,0),'S-02b':(2,0),'S-14':(3,0),'S-03':(4,0),'S-04a':(5,0),
                  'S-02c':(2,1),'S-03b':(4,1),'S-04b':(5,1),'S-01b':(0,1)},
           edges=[('S-01','S-02a','말하기'),('S-02a','S-02b','말 끝남'),('S-02b','S-14','저장 의도'),('S-14','S-03','후보 있음'),('S-03','S-04a','예'),
                  ('S-02b','S-02c','못 알아들음'),('S-03','S-03b','아니요'),('S-03b','S-04a','예'),('S-03b','S-04b','아니요'),('S-14','S-04b','후보 없음'),('S-03','S-04b','5초 무응답'),
                  ('S-01b','S-02a','말하기')]),
 'C': dict(nodes={'S-05':(0,0),'S-05b':(1,0),'NAV':(2,0)},
           edges=[('S-05','S-05b','응'),('S-05b','NAV','')]),
 'D': dict(nodes={'S-01':(0,0),'S-06':(1,0),'S-06c':(2,0),'NAV':(3,0),'S-06b':(1,1)},
           edges=[('S-01','S-06','뭐 먹을까'),('S-06','S-06c','선택'),('S-06c','NAV',''),('S-06','S-06b','없음')]),
 'E': dict(nodes={'NAV':(0,0),'S-15':(1,0),'S-02a':(2,0),'S-15b':(3,0)},
           edges=[('NAV','S-15','도착 뒤 출발'),('S-15','S-02a','말하기'),('S-02a','S-15b','메모')]),
 'F': dict(nodes={'S-01':(0,0),'S-07':(1,0),'S-08':(2,0),'S-08b':(3,0),'S-08c':(4,0),'S-08d':(5,0),
                  'S-07b':(1,1),'S-09':(2,1),'S-09b':(3,1)},
           edges=[('S-01','S-07','내 맛집'),('S-07','S-08','가게'),('S-08','S-08b','수정'),('S-08','S-08c','삭제'),('S-08c','S-08d','삭제'),
                  ('S-07','S-09','미확인'),('S-09','S-09b','직접 검색'),('S-09','S-08','저장'),('S-01','S-07b','0개')]),
 'G': dict(nodes={'S-07':(0,0),'S-10':(1,0),'S-16':(2,0),'S-16b':(3,0)},
           edges=[('S-07','S-10','공유'),('S-10','S-16','링크 열기'),('S-16','S-16b','추가')]),
 'H': dict(nodes={'S-01':(0,0),'S-11':(1,0),'S-11b':(2,0),'S-07':(0,1),'S-12':(1,1)},
           edges=[('S-01','S-11','설정'),('S-11','S-11b','모두 삭제'),('S-07','S-12','주행 중')]),
}
NAMES = {s['id']: s['name'] for s in S}
NAMES['NAV'] = '내비 안내'
def flow_svg():
    NW, NH, CX, RY, LX, PAD = 140, 56, 236, 84, 150, 16
    y = PAD
    out = []; total_h = 0
    for gid, gname, gf in GROUPS:
        f = FLOWS[gid]
        rows = max(r for c, r in f['nodes'].values()) + 1
        lane_h = rows * RY + 28
        out.append(f'<g class="lane"><rect x="8" y="{y}" width="1484" height="{lane_h}" rx="12"/><text x="20" y="{y+22}" class="lanelab">{gid}. {gname}</text></g>')
        pos = {}
        for nid, (c, r) in f['nodes'].items():
            x = LX + c * CX; ny = y + 40 + r * RY
            pos[nid] = (x, ny)
        for a, b, label in f['edges']:
            ax, ay = pos[a]; bx, by = pos[b]
            if ay == by and bx > ax:
                x1, x2 = ax + NW, bx; ym = ay + NH / 2
                out.append(f'<path d="M{x1} {ym}H{x2}" marker-end="url(#fa)"/>')
                if label: out.append(f'<text x="{(x1+x2)/2}" y="{ym-6}" text-anchor="middle" class="q" font-size="12">{label}</text>')
            elif by > ay:   # down to the row below, then right/left into the target's side
                sx = ax + NW * 0.5 if bx >= ax else ax + 8
                ym = by + NH / 2
                if bx >= ax:
                    sx = ax + NW - 24 if bx == ax else ax + NW * 0.5
                    tx = bx if bx > ax else None
                    if bx == ax:
                        out.append(f'<path d="M{sx} {ay+NH}V{by}" marker-end="url(#fa)"/>')
                        if label: out.append(f'<text x="{sx+6}" y="{(ay+NH+by)/2+4}" class="q" font-size="12">{label}</text>')
                    else:
                        out.append(f'<path d="M{sx} {ay+NH}V{ym}H{bx}" marker-end="url(#fa)"/>')
                        if label: out.append(f'<text x="{sx+6}" y="{ym-6}" class="q" font-size="12">{label}</text>')
                else:
                    sx = ax + 24
                    out.append(f'<path d="M{sx} {ay+NH}V{ym}H{bx+NW}" marker-end="url(#fa)"/>')
                    if label: out.append(f'<text x="{bx+NW+6}" y="{ym-6}" class="q" font-size="12">{label}</text>')
            elif by < ay:   # up into the row above
                sx = ax + NW * 0.5; ym = by + NH / 2
                if bx > ax:
                    out.append(f'<path d="M{sx} {ay}V{ym}H{bx}" marker-end="url(#fa)"/>')
                    if label: out.append(f'<text x="{sx+6}" y="{ym+16}" class="q" font-size="12">{label}</text>')
                else:
                    out.append(f'<path d="M{sx} {ay}V{ym}H{bx+NW}" marker-end="url(#fa)"/>')
                    if label: out.append(f'<text x="{bx+NW+6}" y="{ym+16}" class="q" font-size="12">{label}</text>')
            else:  # same row, backwards
                x1, x2 = ax, bx + NW; ym = ay + NH + 10
                out.append(f'<path d="M{ax+NW/2} {ay+NH}V{ym}H{bx+NW/2}V{by+NH}" marker-end="url(#fa)" stroke-dasharray="4 4"/>')
        for nid, (x, ny) in pos.items():
            cls = 'ext' if nid == 'NAV' else ('main' if nid in ('S-01',) else '')
            label = NAMES[nid]
            if nid == 'NAV':
                out.append(f'<g class="node"><rect x="{x}" y="{ny}" width="{NW}" height="{NH}" rx="10" class="ext"/><text x="{x+NW/2}" y="{ny+34}" text-anchor="middle" font-weight="700">내비 안내</text></g>')
            else:
                out.append(f'<g class="node"><a href="#{nid}"><rect x="{x}" y="{ny}" width="{NW}" height="{NH}" rx="10" class="{cls}"/><text x="{x+NW/2}" y="{ny+23}" text-anchor="middle" font-weight="700" font-size="14">{nid}</text><text x="{x+NW/2}" y="{ny+44}" text-anchor="middle" class="q" font-size="13">{label}</text></a></g>')
        y += lane_h + 14
    total_h = y + 4
    return (f'<svg viewBox="0 0 1500 {total_h}" role="img" aria-label="화면 흐름" font-size="14" class="flow">'
            f'<defs><marker id="fa" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="ah"/></marker></defs>'
            f'<g class="edges">{"".join(o for o in out if o.startswith("<path"))}</g>'
            f'{"".join(o for o in out if not o.startswith("<path"))}</svg>')

# ---------------------------------------------------------------- tables shared by html / md
BASE_ROWS = [
 ['캔버스','1280 × 1080 (앱 영역). 전체 화면은 1920 × 1080 16:9를 좌우 1:2로 나눈 오른쪽','Pleos Connect 17인치 16:9, 좌우 1:2 분할. 해상도 값은 가정이며 에뮬레이터 확인 뒤 조정'],
 ['세로 창','Pleos 앱 영역 기본값인 세로 1칸에서는 말하기 버튼을 하단 가운데에 두고, 카드(최근 저장·추천·안내)는 그 위에 쌓는다. 가로 2칸은 이 문서의 그림대로','재원 결정 2026-11-09. 대표 화면 5장을 6장 끝 “세로 창” 묶음(P-01 ~ P-06)에 그렸다. 구현 때 창 비율을 보고 두 배치 중 하나를 고른다'],
 ['기준 위치','말하기 버튼(또는 뭐 먹을까 카드)을 누른 순간의 위치를 “말한 곳”으로 고정한다. 가게 찾기 · 추천의 거리와 순서는 모두 이 위치 기준이고, 결과가 나온 뒤 차가 움직여도 바뀌지 않는다','재원 결정 2026-11-09. 말하는 동안 최대 15초(시속 60km면 약 250m) 움직여도 검색 범위가 밀리지 않게 한다'],
 ['테마','다크·라이트 2가지 (차량 주/야간 설정을 따름). 다크: 배경 #15100E, 글자 #FBF6F2, 보조 #B8A69B. 라이트: 배경 #F6EFEA, 글자 #1F1512, 보조 #6E5B53. 배경은 그림 없는 단색','다크는 야간 눈부심 방지. 색은 앱 아이콘 주황 #E4572E에서 뽑음. 이 문서의 와이어프레임은 위 단추로 다크·라이트를 바꿔 볼 수 있다'],
 ['스타일','리퀴드 글라스: 카드·버튼·상단 버튼은 반투명 유리(다크 흰색 9~14%, 라이트 흰색 78~95%) + 흐림 28 + 밝은 윗선 + 1dp 테두리 + 그림자. 색이 있는 유리는 말하기 버튼 하나','유리는 콘텐츠 위 조작 층에만. 지도 화면에서는 지도가 콘텐츠 층. 투명도 줄이기를 켜면 불투명 카드 #1F1714 / #FFFFFF'],
 ['강조색','1순위 동작 주황 #E4572E 채움(흰 글자). 주황 글자·아이콘은 다크 #FF8A62 · 라이트 #B83A18. 삭제만 빨강 #C62828 채움 · 다크 #FF6B6B 글자. 미확인 노랑 #F2B544 / #8A5A00, 방문 초록 #4CC38A / #1B7F4D. 파랑은 쓰지 않음','빨강은 차량에서 경고색이라 삭제에만 사용. 글자 대비 4.5:1 이상 (흰 글자 위 주황은 32dp 굵은 글자만)'],
 ['글꼴','Pretendard (없으면 Noto Sans KR). 본문 28dp, 버튼 32dp, 카드 제목 40dp, 질문 48dp, 가게 이름 64dp, 최소 24dp','70cm 시청 거리 기준'],
 ['터치 크기','버튼 높이 88dp 기본, 1순위 112dp, 목록 줄 96~112dp, 최소 76dp, 간격 24dp 이상','Android Automotive 주행 중 최소 76dp'],
 ['한 화면 규칙','1순위 버튼은 1개(주황), 주행 중 글자 줄 수 3줄 이내, 목록 스크롤·키보드는 정차 중에만','기획서 8장 비기능 요구사항'],
 ['아이콘','Lucide 선 아이콘 1종, 이모지 금지. 앱 아이콘은 확정 15번(주황 핀)','design/icons/final/'],
 ['동작 시간','화면 전환 200ms 이내, 토스트 4초, 확인 대기 5초, 리마인드·방문 메모 카드 10초','5장 애니메이션 기준 참고'],
]
COMMON_ROWS = [
 ['상단 바','높이 96dp: 뒤로(80×80) · 제목 36dp · 오른쪽 동작 1개','홈에는 뒤로 없음. 제목은 화면 이름 그대로'],
 ['1순위 버튼','주황 채움 알약형, 높이 88~136dp','화면에 1개만'],
 ['2순위 버튼','유리 알약형, 높이 88dp',''],
 ['삭제 버튼','테두리 빨강(상세·정리) 또는 채움 빨강(확인 창)','다른 버튼과 떨어뜨림'],
 ['카드','유리, 모서리 36dp, 안쪽 여백 32dp, 1dp 밝은 테두리 + 그림자','알림 카드·창·토스트는 더 진한 유리(다크 14% · 라이트 95%)'],
 ['상태 칩','높이 48dp: 미확인(노랑) · 방문(초록) · 선택(주황) · 저장됨(회색)','글자와 색을 같이 써서 색만으로 구분하지 않음'],
 ['토스트','아래쪽(위에서 900dp), 좌우 140dp 여백, 높이 120dp, 4초','되돌리기 버튼은 삭제 뒤에만. 한 번에 1개'],
 ['확인 창','가운데, 폭 800dp, 질문 40dp + 설명 28dp + 버튼 2개','취소가 왼쪽, 실행이 오른쪽'],
 ['차량 알림 카드','위쪽 48dp, 높이 300dp, 아래 가장자리에 남은 시간 막대','리마인드(S-05) · 방문 메모(S-15)'],
 ['음성 겹침 화면','S-02a~c를 어느 화면 위에도 띄움','뒤 화면은 72% 어둡게'],
 ['키보드 자리','아래 320dp (시스템 키보드)','정차 중에만. 입력창은 키보드 위에 남도록 배치'],
]
MOTION_BASE = [
 ['빠름','120ms','standard (0.2, 0, 0, 1)','버튼 누름, 칩·체크·라디오·토글, 줄 선택'],
 ['기본','200ms','standard','화면 전환, 카드·토스트 들어옴, 목록 줄 등장, 확인 창'],
 ['강조','240 ~ 400ms','emphasized (0.05, 0.7, 0.1, 1)','차량 알림 카드 내려옴, 시작 아이콘·지도 핀 떨어짐'],
 ['반복','0.8 ~ 2s','ease-in-out / linear','소리 막대, 처리 링, 말하기 버튼 숨쉬기, 카운트다운'],
 ['진행','5s · 10s','linear','확인 대기 링, 알림 카드 남은 시간 막대'],
]
MOTION_RULES = [
 ['주행 중 화면','이동 거리 24dp 이하, 페이드 위주. 화면끼리는 페이드만(슬라이드 없음)','시선을 끌지 않기 위해'],
 ['정차 중 화면','오른쪽 48dp에서 밀려 들어오며 페이드 (뒤로 가기는 반대)','화면 깊이를 알게'],
 ['겹침 화면 (듣는 중·확인 창)','어두운 배경 160ms 페이드 + 창 96 → 100% 확대 200ms','어느 화면 위에서나 같음'],
 ['토스트','아래에서 24dp 올라오며 페이드 인 200ms, 4초 뒤 페이드 아웃 200ms','한 번에 1개'],
 ['차량 알림 카드','위에서 32dp 내려옴 240ms, 닫힐 때 위로 200ms','리마인드 · 방문 메모'],
 ['반복 애니메이션','듣는 중 막대, 처리 링, 말하기 숨쉬기, 카운트다운 네 곳만','그 밖에는 반복 없음'],
 ['동작 줄이기','차량 설정이 동작 줄이기면 전환은 페이드만, 반복은 소리 막대만 남김',''],
 ['소리','애니메이션에 효과음을 붙이지 않음. TTS만','차량 알림음과 겹치지 않게'],
]
STATE_ROWS = [
 ['저장 가게 0개','S-01b 홈 · 저장 전, S-07b 내 맛집 · 저장 전'],
 ['인식 실패','S-02c 다시 말해 주세요 (1회 재시도)'],
 ['인터넷 없음 · 후보 없음','S-14 → S-04b 위치만 저장'],
 ['추천할 가게 없음','S-06b'],
 ['삭제 뒤','S-08d 되돌리기 토스트'],
 ['처음 실행','S-13 처음 실행 안내'],
 ['권한 거부 · 음성 서비스 오류','S-17 권한 없음'],
 ['주행 중 정차 화면 진입','S-12 주행 중 안내'],
]
OPEN_ROWS = [
 '캔버스 1280×1080은 가정이다. 에뮬레이터 실제 해상도를 확인하면 비율만 다시 맞춘다. 세로 1칸은 640×1080으로 가정해 대표 화면 5장(P-01 ~ P-06)을 그렸다. 실제 창 크기를 확인하면 함께 맞춘다.',
 'S-07·S-14 지도는 카카오맵 SDK가 차량에서 뜨는지에 달려 있다. 안 뜨면 프로토타입은 목록만 쓰고 지도 자리는 비워 둔다.',
 'S-05 리마인드·S-15 방문 메모를 앱 밖(내비 화면 위)에 띄우려면 차량 알림 권한이 필요하다. 안 되면 앱이 켜져 있을 때만 띄운다.',
 'S-16 받은 목록 가져오기는 차량에서 링크를 열 수 있을 때만 의미가 있다. 안 되면 폰 웹 목록으로 끝낸다 (기획서 9장).',
 '스티어링 휠 음성 버튼 연동은 Pleos SDK에 없어 말하기 버튼은 화면에만 둔다.',
 '라이트 테마 와이어프레임은 홈(S-01·S-02a·S-04a)만 design/home/에 있다. 나머지는 색 토큰만 바꾸면 되므로 구현 때 맞춘다.',
]

# ---------------------------------------------------------------- page CSS
PAGE_CSS = r'''
:root{--bg:#fff;--fg:#1f2328;--muted:#59636e;--line:#d1d9e0;--head:#f6f8fa;--accent:#0969da;--accent-bg:#ddf4ff}
@media (prefers-color-scheme: dark){:root{--bg:#0d1117;--fg:#e6edf3;--muted:#9198a1;--line:#3d444d;--head:#161b22;--accent:#4493f8;--accent-bg:#121d2f}}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--fg);font-family:"Pretendard","Apple SD Gothic Neo","Malgun Gothic","Noto Sans KR",system-ui,sans-serif;font-size:16px;line-height:1.65;word-break:keep-all}
main{max-width:1180px;margin:0 auto;padding:40px 24px 80px}
h1{font-size:2em;border-bottom:1px solid var(--line);padding-bottom:.3em;margin:0 0 .4em}
h2{font-size:1.45em;border-bottom:1px solid var(--line);padding-bottom:.3em;margin-top:2.2em}
h3{font-size:1.25em;margin:2em 0 .4em;padding-top:.6em;border-top:1px dashed var(--line)}
h4{font-size:1.05em;margin:1.4em 0 .4em}
.meta{color:var(--muted)}
table{border-collapse:collapse;width:100%;font-size:.93em;margin:.6em 0 1em}
th,td{border:1px solid var(--line);padding:7px 11px;vertical-align:top;text-align:left}
th{background:var(--head);font-weight:600;white-space:nowrap}
.tbl{overflow-x:auto}
.screen{display:grid;grid-template-columns:640px 1fr;gap:28px;align-items:start}
@media (max-width:1000px){.screen{grid-template-columns:1fr}}
.shot{width:640px;height:540px;overflow:hidden;border-radius:12px;border:1px solid var(--line);background:#15100E;position:relative}
.wl .shot{background:#F6EFEA}
.shot .frame{transform:scale(.5);transform-origin:0 0}
.screen.port{grid-template-columns:320px 1fr}
.shot.port{width:320px}
.cap{color:var(--muted);font-size:.9em;margin-top:6px;display:flex;gap:12px;align-items:center}
.cap button{font:inherit;font-size:.9em;padding:2px 10px;border-radius:6px;border:1px solid var(--line);background:var(--head);color:var(--fg);cursor:pointer}
.spec table{margin-top:0}
.spec .co{display:inline-flex;width:22px;height:22px;border-radius:50%;background:#7C3AED;color:#fff;font-weight:700;font-size:13px;align-items:center;justify-content:center}
.note{color:var(--muted);font-size:.93em;margin:.3em 0 0}
.flow{width:100%;height:auto;display:block;margin:.6em 0}
.flow text{fill:var(--fg);font-family:inherit}.flow .q{fill:var(--muted)}
.flow .lane rect{fill:none;stroke:var(--line);stroke-dasharray:6 6}.flow .lanelab{fill:var(--muted);font-size:13px;font-weight:600}
.flow .node rect{fill:var(--bg);stroke:var(--muted);stroke-width:1.5}.flow .node rect.main{fill:var(--accent-bg);stroke:var(--accent);stroke-width:2}
.flow .node rect.ext{stroke-dasharray:5 5}
.flow .edges path{fill:none;stroke:var(--muted);stroke-width:1.5}.flow .ah{fill:var(--muted)}
.flow a{text-decoration:none}
.kv td:first-child{white-space:nowrap;color:var(--muted)}
.toc{columns:3;font-size:.95em}.toc a{color:var(--accent);text-decoration:none}
.grp{display:inline-block;padding:0 8px;border-radius:6px;background:var(--accent-bg);color:var(--accent);font-size:.8em;font-weight:600;vertical-align:middle;margin-left:6px}
.gif{color:var(--muted);font-size:.85em}
.themebar{position:sticky;top:0;z-index:20;display:flex;justify-content:flex-end;gap:8px;align-items:center;padding:8px 0;background:var(--bg);font-size:.9em;color:var(--muted)}
.themebar button{font:inherit;padding:3px 14px;border-radius:999px;border:1px solid var(--line);background:var(--head);color:var(--fg);cursor:pointer}
.themebar button[aria-pressed="true"]{background:#E4572E;border-color:#E4572E;color:#fff}
@media print{.themebar{display:none}.shot{break-inside:avoid}h2{break-before:page}h2:first-of-type{break-before:auto}.cap button{display:none}}
'''
PAGE_JS = r'''
(function(){
  var io=new IntersectionObserver(function(es){es.forEach(function(e){var f=e.target.querySelector('.frame');if(!f)return;
    if(e.isIntersecting){f.classList.remove('play');void f.offsetWidth;f.classList.add('play');}else{f.classList.remove('play');}});},{threshold:.35});
  document.querySelectorAll('.shot').forEach(function(s){io.observe(s);});
  var tb=document.querySelectorAll('.themebar button');
  function setT(t){document.body.classList.toggle('wl',t==='light');tb.forEach(function(b){b.setAttribute('aria-pressed',b.dataset.t===t?'true':'false');});}
  tb.forEach(function(b){b.addEventListener('click',function(){setT(b.dataset.t);});});
  setT(window.matchMedia&&window.matchMedia('(prefers-color-scheme: light)').matches?'light':'dark');
  document.querySelectorAll('.cap button').forEach(function(b){b.addEventListener('click',function(){var f=b.closest('.shotwrap').querySelector('.frame');f.classList.remove('play');void f.offsetWidth;f.classList.add('play');});});
})();
'''

# ---------------------------------------------------------------- build page
def tbl(headers, rows, cls=''):
    h = ''.join(f'<th>{x}</th>' for x in headers)
    b = ''.join('<tr>' + ''.join(f'<td>{c}</td>' for c in r) + '</tr>' for r in rows)
    return f'<div class="tbl"><table class="{cls}"><thead><tr>{h}</tr></thead><tbody>{b}</tbody></table></div>'

n_screens = len(S)
INTRO = (f'주행 중에 쓰는 화면은 말하기 버튼과 큰 카드 몇 장으로 끝내고, 목록·편집·지도는 정차 중에만 연다. '
         f'기능 묶음 {len(GROUPS)}개, 화면·상태 {n_screens}장이다. 같은 화면의 다른 상태는 S-02a·S-02b처럼 글자를 붙여 구분한다.')

PORTRAIT_INTRO = ('Pleos 앱 영역은 기본이 세로 1칸이다. 세로 창에서는 말하기 버튼(또는 그 화면의 결정 버튼)을 하단 가운데에 두고, 카드는 그 위에 쌓는다. '
                  '주행 중에 쓰는 대표 화면 ' + str(len(PS)) + '장만 그렸고, 나머지 화면도 같은 규칙으로 옮긴다. 모션은 가로 화면과 같다. 크기 640×1080은 가정이며 에뮬레이터에서 확인한다.')
parts = []
parts.append('<h1>다음에 가게 — 화면 설계서</h1>')
parts.append(f'<p class="meta">작성일 {DATE} · 작성자 재원 (easycd) · 기준 문서: <a href="01_개발기획서.md">01_개발기획서</a>의 기능 ID(F-xx)를 그대로 쓴다. 생성 스크립트 tools/gen_screens.py</p>')
parts.append('<div class="themebar">와이어프레임 테마 <button type="button" data-t="dark">다크</button><button type="button" data-t="light">라이트</button></div>')
parts.append(f'<p>{INTRO} 와이어프레임은 화면에 보일 때 애니메이션이 한 번 재생되고, 아래 “다시 재생”으로 다시 볼 수 있다. 오른쪽 위 단추로 다크·라이트를 바꿔 본다. 보라색 번호는 설명용 표시로, 실제 화면에는 없다.</p>')
parts.append('<p class="toc">' + ' · '.join(f'<a href="#{s["id"]}">{s["id"]} {s["name"]}</a>' for s in S + PS) + '</p>')

parts.append('<h2 id="base">1. 설계 기준</h2>')
parts.append(tbl(['항목','기준','근거'], BASE_ROWS))

parts.append('<h2 id="list">2. 화면 목록 (기능별)</h2>')
for gid, gname, gf in GROUPS:
    rows_ = [[f'<a href="#{s["id"]}">{s["id"]}</a>', s['name'], s['when'], s['entry'], s['funcs']] for s in S if s['group'] == gid]
    parts.append(f'<h4>{gid}. {gname} <span class="meta" style="font-weight:400">· {gf} · {len(rows_)}장</span></h4>')
    parts.append(tbl(['ID','화면','쓰는 상황','들어오는 길','기능'], rows_))

parts.append('<h2 id="flow">3. 화면 흐름</h2>')
parts.append(flow_svg())
parts.append('<p class="note">묶음마다 한 줄씩. 위 줄이 기본 흐름, 아래 줄이 갈림길이다. 홈(S-01)은 여러 묶음에 다시 나온다. 내비 안내는 Pleos 내비 앱으로 넘어가는 바깥 화면이다. 정차 화면(F·G·H)을 주행 중에 열면 S-12가 대신 뜬다. 상자를 누르면 그 화면으로 간다.</p>')

parts.append('<h2 id="common">4. 공통 요소</h2>')
parts.append(tbl(['요소','크기','규칙'], COMMON_ROWS))

parts.append('<h2 id="motion">5. 애니메이션 기준</h2>')
parts.append(tbl(['이름','시간','이징','쓰는 곳'], MOTION_BASE))
parts.append(tbl(['규칙','내용','이유'], MOTION_RULES))
parts.append('<p class="note">화면별 모션은 6장 각 화면의 “모션” 표에 있다. 움직이는 미리보기(GIF)는 screens/anim/ 에 있고, 이 HTML에서는 와이어프레임이 직접 움직인다.</p>')

parts.append('<h2 id="screens">6. 화면별 설계</h2>')
cur = None
for s in S:
    if s['group'] != cur:
        cur = s['group']; g = next(g for g in GROUPS if g[0] == cur)
        parts.append(f'<h3 id="g{cur}" style="border-top:2px solid var(--line);font-size:1.35em">{g[0]}. {g[1]} <span class="meta" style="font-weight:400;font-size:.75em">· {g[2]}</span></h3>')
    spec_rows = []
    for co, el, what, act in s['spec']:
        badge = '<span class="co">←</span>' if co == 'b' else f'<span class="co">{co}</span>'
        spec_rows.append([badge, el, what, act])
    gif_note = f' <span class="gif">· <a href="screens/anim/{s["id"]}.gif">GIF</a></span>' if s['gif'] else ''
    parts.append(f'<h3 id="{s["id"]}">{s["id"]} {s["name"]} <span class="meta" style="font-weight:400;font-size:.8em">· {s["when"]} · {s["funcs"]}</span></h3>')
    parts.append(f'<p class="meta" style="margin:0 0 .6em">들어오는 길: {s["entry"]}</p>')
    parts.append('<div class="screen">')
    parts.append(f'<div class="shotwrap"><div class="shot"><div class="frame">{s["wire"]}</div></div><div class="cap"><span>1280×1080 기준, 50% 축소</span><button type="button">다시 재생</button>{gif_note}</div></div>')
    parts.append('<div class="spec">' + tbl(['','요소','내용','동작'], spec_rows) + (f'<p class="note">{s["note"]}</p>' if s['note'] else '')
                 + '<p style="margin:.9em 0 .2em;font-weight:600">모션</p>' + tbl(['요소','움직임','시간 · 이징','계기'], s['motion']) + '</div>')
    parts.append('</div>')

parts.append('<h3 id="gP" style="border-top:2px solid var(--line);font-size:1.35em">세로 창 (1칸) <span class="meta" style="font-weight:400;font-size:.75em">· 640×1080 가정 · ' + str(len(PS)) + '장</span></h3>')
parts.append('<p>' + PORTRAIT_INTRO + '</p>')
for s in PS:
    spec_rows = [[('<span class="co">←</span>' if co == 'b' else f'<span class="co">{co}</span>'), el, what, act] for co, el, what, act in s['spec']]
    parts.append(f'<h3 id="{s["id"]}">{s["id"]} {s["name"]} <span class="meta" style="font-weight:400;font-size:.8em">· 가로 화면 <a href="#{s["base"]}">{s["base"]}</a>의 세로 배치</span></h3>')
    parts.append('<div class="screen port">')
    parts.append(f'<div class="shotwrap"><div class="shot port"><div class="frame port">{s["wire"]}</div></div><div class="cap"><span>640×1080, 50% 축소</span><button type="button">다시 재생</button></div></div>')
    parts.append('<div class="spec">' + tbl(['','요소','내용','동작'], spec_rows) + (f'<p class="note">{s["note"]}</p>' if s['note'] else '') + '</div>')
    parts.append('</div>')

parts.append('<h2 id="states">7. 상태와 예외 → 화면</h2>')
parts.append(tbl(['상황','화면'], STATE_ROWS))

parts.append('<h2 id="next">8. 확인이 필요한 사항</h2>')
parts.append('<ul>' + ''.join(f'<li>{x}</li>' for x in OPEN_ROWS) + '</ul>')

doc = f'''<!DOCTYPE html>
<html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>다음에 가게 — 화면 설계서</title>
<style>{PAGE_CSS}{WIRE_CSS}{ANIM_CSS}</style></head>
<body><main>
{''.join(parts)}
</main><script>{PAGE_JS}</script></body></html>'''
with open(os.path.join(OUT_DIR, '02_화면설계서.html'), 'w', encoding='utf-8') as f:
    f.write(doc)

# standalone wire pages: static (PNG, dark + light) and live (GIF, dark); canvas boards without callouts
LIGHT_DIR = os.path.join(WIRE_DIR, 'light'); BOARD_DIR = os.path.join(WIRE_DIR, 'boards')
os.makedirs(LIGHT_DIR, exist_ok=True); os.makedirs(BOARD_DIR, exist_ok=True)
def wire_page(s_, cls, bg, title=''):
    t = f'<title>{title}</title>' if title else ''
    return f'''<!DOCTYPE html><html lang="ko"><head><meta charset="utf-8">{t}<style>body{{margin:0;background:{bg}}}{WIRE_CSS}{ANIM_CSS}</style></head>
<body><div class="frame {cls}" id="f" data-id="{s_["id"]}">{s_["wire"]}</div></body></html>'''
for s in S:
    for d, cls, bg in ((WIRE_DIR, 'static', '#15100E'), (LIVE_DIR, 'play', '#15100E'), (LIGHT_DIR, 'static light', '#F6EFEA')):
        with open(os.path.join(d, f'{s["id"]}.html'), 'w', encoding='utf-8') as f:
            f.write(wire_page(s, cls, bg))
    for theme, cls, bg in (('dark', 'static clean', '#15100E'), ('light', 'static clean light', '#F6EFEA')):
        with open(os.path.join(BOARD_DIR, f'{s["id"]}_{theme}.dc.html'), 'w', encoding='utf-8') as f:
            f.write(wire_page(s, cls, bg, f'{s["id"]} {s["name"]} · {"다크" if theme == "dark" else "라이트"}'))

PORT_DIR = os.path.join(WIRE_DIR, 'portrait'); PORT_LIGHT = os.path.join(PORT_DIR, 'light'); PORT_LIVE = os.path.join(PORT_DIR, 'live')
for d in (PORT_DIR, PORT_LIGHT, PORT_LIVE): os.makedirs(d, exist_ok=True)
for s in PS:
    for d, cls, bg in ((PORT_DIR, 'static port', '#15100E'), (PORT_LIVE, 'play port', '#15100E'), (PORT_LIGHT, 'static port light', '#F6EFEA')):
        with open(os.path.join(d, f'{s["id"]}.html'), 'w', encoding='utf-8') as f:
            f.write(wire_page(s, cls, bg))
    for theme, cls, bg in (('dark', 'static clean port', '#15100E'), ('light', 'static clean port light', '#F6EFEA')):
        with open(os.path.join(BOARD_DIR, f'{s["id"]}_{theme}.dc.html'), 'w', encoding='utf-8') as f:
            f.write(wire_page(s, cls, bg, f'{s["id"]} {s["name"]} · 세로 · {"다크" if theme == "dark" else "라이트"}'))

# data for the checker
with open(os.path.join(WIRE_DIR, 'screens.json'), 'w', encoding='utf-8') as f:
    json.dump(dict(groups=GROUPS, flows={k: dict(nodes=list(v['nodes'].keys()), edges=v['edges']) for k, v in FLOWS.items()},
                   screens=[dict(id=s['id'], group=s['group'], name=s['name'], when=s['when'], entry=s['entry'], funcs=s['funcs'],
                                 spec=s['spec'], motion=s['motion'], note=s['note'], gif=s['gif']) for s in S]),
              f, ensure_ascii=False, indent=1)

# ---------------------------------------------------------------- markdown
def mdtbl(headers, rows):
    out = ['| ' + ' | '.join(headers) + ' |', '| ' + ' | '.join('---' for _ in headers) + ' |']
    for r in rows:
        out.append('| ' + ' | '.join(strip(str(c)).replace('|', '／').replace('\n', ' ') for c in r) + ' |')
    return '\n'.join(out)
def strip(s):
    s = re.sub(r'<span class="co">(.*?)</span>', r'\1', s)
    s = re.sub(r'<[^>]+>', '', s)
    return html.unescape(s)

md = ['# 다음에 가게 — 화면 설계서', '',
      f'작성일 {DATE} · 작성자 재원 (easycd) · 기준 문서: [01_개발기획서](01_개발기획서.md)의 기능 ID(F-xx)를 그대로 쓴다. 생성 스크립트 tools/gen_screens.py', '',
      INTRO + ' 와이어프레임이 움직이는 원본은 [02_화면설계서.html](02_화면설계서.html)이고, 움직이는 미리보기는 screens/anim/ 의 GIF다. 화면마다 다크(screens/)와 라이트(screens/light/) 그림이 있다. 보라색 번호는 설명용 표시다.', '']
md += ['## 1. 설계 기준', '', mdtbl(['항목','기준','근거'], BASE_ROWS), '']
md += ['## 2. 화면 목록 (기능별)', '']
for gid, gname, gf in GROUPS:
    rows_ = [[s['id'], s['name'], s['when'], s['entry'], s['funcs']] for s in S if s['group'] == gid]
    md += [f'### {gid}. {gname} · {gf} · {len(rows_)}장', '', mdtbl(['ID','화면','쓰는 상황','들어오는 길','기능'], rows_), '']
md += ['## 3. 화면 흐름', '', '```mermaid', 'flowchart LR']
for gid, gname, gf in GROUPS:
    f_ = FLOWS[gid]
    md.append(f'  subgraph {gid}[{gid}. {gname}]')
    seen = set()
    for a, b, label in f_['edges']:
        def nid(x): return f'{gid}_{x.replace("-", "")}'
        def lab(x): return (f'{nid(x)}([{NAMES[x]}])' if x == 'NAV' else f'{nid(x)}[{x} {NAMES[x]}]') if x not in seen and not seen.add(x) else nid(x)
        l = f'|{label}|' if label else ''
        md.append(f'    {lab(a)} -->{l} {lab(b)}')
    md.append('  end')
md += ['```', '', '묶음마다 한 줄씩. 홈(S-01)은 여러 묶음에 다시 나온다. 내비 안내는 Pleos 내비 앱으로 넘어가는 바깥 화면이다. 정차 화면(F·G·H)을 주행 중에 열면 S-12가 대신 뜬다.', '']
md += ['## 4. 공통 요소', '', mdtbl(['요소','크기','규칙'], COMMON_ROWS), '']
md += ['## 5. 애니메이션 기준', '', mdtbl(['이름','시간','이징','쓰는 곳'], MOTION_BASE), '', mdtbl(['규칙','내용','이유'], MOTION_RULES), '',
       '화면별 모션은 6장 각 화면의 “모션” 표에 있다.', '']
md += ['## 6. 화면별 설계', '']
cur = None
for s in S:
    if s['group'] != cur:
        cur = s['group']; g = next(g for g in GROUPS if g[0] == cur)
        md += [f'### {g[0]}. {g[1]} · {g[2]}', '']
    md += [f'#### {s["id"]} {s["name"]}', '', f'{s["when"]} · 들어오는 길: {s["entry"]} · {s["funcs"]}', '',
           '| 다크 | 라이트 |', '| --- | --- |',
           f'| ![{s["id"]} {s["name"]} 다크](screens/{s["id"]}.png) | ![{s["id"]} {s["name"]} 라이트](screens/light/{s["id"]}.png) |', '']
    if s['gif']:
        md += [f'움직이는 미리보기: ![{s["id"]} 모션](screens/anim/{s["id"]}.gif)', '']
    md += [mdtbl(['번호','요소','내용','동작'], [[('뒤로' if co == 'b' else co), el, what, act] for co, el, what, act in s['spec']]), '']
    if s['note']:
        md += [s['note'], '']
    md += ['모션', '', mdtbl(['요소','움직임','시간 · 이징','계기'], s['motion']), '']
md += [f'### 세로 창 (1칸) · 640×1080 가정 · {len(PS)}장', '', PORTRAIT_INTRO, '']
for s in PS:
    md += [f'#### {s["id"]} {s["name"]}', '', f'가로 화면 {s["base"]}의 세로 배치', '',
           '| 다크 | 라이트 |', '| --- | --- |',
           f'| ![{s["id"]} {s["name"]} 다크](screens/portrait/{s["id"]}.png) | ![{s["id"]} {s["name"]} 라이트](screens/portrait/light/{s["id"]}.png) |', '',
           mdtbl(['번호','요소','내용','동작'], [[('뒤로' if co == 'b' else co), el, what, act] for co, el, what, act in s['spec']]), '']
    if s['note']:
        md += [s['note'], '']
md += ['## 7. 상태와 예외 → 화면', '', mdtbl(['상황','화면'], STATE_ROWS), '']
md += ['## 8. 확인이 필요한 사항', ''] + [f'- {x}' for x in OPEN_ROWS] + ['']
with open(os.path.join(OUT_DIR, '02_화면설계서.md'), 'w', encoding='utf-8') as f:
    f.write('\n'.join(md))
print('ok', n_screens, 'screens')
