# -*- coding: utf-8 -*-
"""Generate docs/02_화면설계서.html (+ standalone wireframe pages for PNG export) for 다음에 가게."""
import os, sys, html

OUT_DIR = sys.argv[1] if len(sys.argv) > 1 else '.'
WIRE_DIR = sys.argv[2] if len(sys.argv) > 2 else os.path.join(OUT_DIR, 'wires')
os.makedirs(WIRE_DIR, exist_ok=True)

# ---------------------------------------------------------------- icons
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
}
def ic(name, size=40, cls=''):
    return (f'<svg class="ic {cls}" width="{size}" height="{size}" viewBox="0 0 24 24" fill="none" '
            f'stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{ICONS[name]}</svg>')

# ---------------------------------------------------------------- wireframe CSS (1280x1080 canvas, 1dp = 1px)
WIRE_CSS = r'''
.frame{position:relative;width:1280px;height:1080px;background:#0B0F17;color:#F3F5F8;overflow:hidden;
  font-family:"Pretendard","Apple SD Gothic Neo","Malgun Gothic","Noto Sans KR",system-ui,sans-serif;font-size:28px;line-height:1.3;word-break:keep-all}
.frame *{box-sizing:border-box}
.frame .ic{flex:none}
.top{position:absolute;left:0;top:0;right:0;height:96px;display:flex;align-items:center;gap:16px;padding:0 24px 0 16px;border-bottom:2px solid #2C3646}
.top .title{font-size:36px;font-weight:600;flex:1}
.top .iconbtn{width:80px;height:80px;display:flex;align-items:center;justify-content:center;border-radius:20px;color:#F3F5F8}
.top .textbtn{height:72px;padding:0 28px;display:flex;align-items:center;gap:12px;border-radius:20px;border:2px solid #2C3646;font-size:30px;font-weight:600}
.body{position:absolute;left:0;right:0;bottom:0;top:96px;padding:48px}
.body.full{top:0}
.btn{display:flex;align-items:center;justify-content:center;gap:16px;height:88px;padding:0 40px;border-radius:24px;border:2px solid #3A4557;background:#1E2635;font-size:32px;font-weight:600;color:#F3F5F8}
.btn.primary{background:#2563EB;border-color:#2563EB;color:#fff}
.btn.big{height:112px;font-size:36px}
.btn.danger{background:transparent;border-color:#7F1D1D;color:#F87171}
.btn.ghost{background:transparent;border-color:transparent;color:#A6B0C0}
.card{background:#151B26;border:2px solid #2C3646;border-radius:28px;padding:32px}
.muted{color:#A6B0C0}.small{font-size:24px}.h40{font-size:40px;font-weight:600}.h48{font-size:48px;font-weight:600}.h64{font-size:64px;font-weight:700;line-height:1.15}
.chip{display:inline-flex;align-items:center;height:48px;padding:0 20px;border-radius:24px;border:2px solid #3A4557;font-size:24px;color:#C8D0DC;white-space:nowrap}
.chip.on{background:#2563EB;border-color:#2563EB;color:#fff}
.chip.warn{border-color:#B45309;color:#FCD34D}
.chip.ok{border-color:#15803D;color:#86EFAC}
.row{display:flex;align-items:center;gap:24px;height:104px;padding:0 24px;border-bottom:2px solid #1E2635}
.row .name{font-size:32px;font-weight:600}
.row.sel{background:#1B2A45;border-radius:20px;border-bottom-color:transparent}
.toggle{width:96px;height:52px;border-radius:26px;background:#2563EB;position:relative;flex:none}
.toggle::after{content:"";position:absolute;top:6px;right:6px;width:40px;height:40px;border-radius:50%;background:#fff}
.toggle.off{background:#3A4557}.toggle.off::after{right:auto;left:6px}
.seg{display:flex;border:2px solid #3A4557;border-radius:20px;overflow:hidden;height:72px}
.seg div{padding:0 32px;display:flex;align-items:center;font-size:28px;color:#C8D0DC}
.seg .on{background:#2563EB;color:#fff}
.mapph{background:repeating-linear-gradient(45deg,#121926 0 24px,#161E2C 24px 48px);border:2px solid #2C3646;border-radius:28px;position:relative;color:#A6B0C0}
.mapph .lab{position:absolute;left:24px;top:20px;font-size:24px}
.mapph .pin{position:absolute;color:#4F8DFF}
.mapph .pin.me{color:#F3F5F8}
.overlay{position:absolute;inset:0;background:rgba(5,8,14,.72)}
.toast{position:absolute;left:140px;right:140px;height:120px;border-radius:28px;background:#151B26;border:2px solid #2C3646;display:flex;align-items:center;gap:24px;padding:0 40px;font-size:32px;font-weight:600}
.toast .ok{color:#4ADE80}
[data-co]{position:relative}
[data-co]::after{content:attr(data-co);position:absolute;left:-18px;top:-18px;width:44px;height:44px;border-radius:50%;background:#F59E0B;color:#111;font-size:26px;font-weight:700;display:flex;align-items:center;justify-content:center;z-index:5;font-family:system-ui,sans-serif;line-height:1}
.wave{display:flex;align-items:center;gap:12px;height:120px}
.wave i{display:block;width:16px;border-radius:8px;background:#4F8DFF}
.ring{width:72px;height:72px;border-radius:50%;border:8px solid #3A4557;border-top-color:#4F8DFF;flex:none}
.mic{width:360px;height:360px;border-radius:50%;background:#2563EB;display:flex;align-items:center;justify-content:center;color:#fff;box-shadow:0 0 0 24px rgba(37,99,235,.18)}
.qr{width:280px;height:280px;border-radius:20px;background:
  repeating-linear-gradient(90deg,#F3F5F8 0 20px,transparent 20px 40px),repeating-linear-gradient(0deg,#F3F5F8 0 20px,#0B0F17 20px 40px);border:12px solid #F3F5F8}
.input{height:88px;border:2px solid #3A4557;border-radius:20px;background:#0F141D;display:flex;align-items:center;padding:0 24px;font-size:32px}
.check{width:56px;height:56px;border-radius:14px;border:3px solid #6B7686;flex:none;display:flex;align-items:center;justify-content:center;color:#fff}
.check.on{background:#2563EB;border-color:#2563EB}
.radio{width:56px;height:56px;border-radius:50%;border:3px solid #6B7686;flex:none;position:relative}
.radio.on{border-color:#2563EB}.radio.on::after{content:"";position:absolute;inset:10px;border-radius:50%;background:#2563EB}
.num{width:72px;height:72px;border-radius:20px;background:#2563EB;color:#fff;font-size:40px;font-weight:700;display:flex;align-items:center;justify-content:center;flex:none}
.slider{height:16px;border-radius:8px;background:#3A4557;position:relative;flex:1}
.slider::before{content:"";position:absolute;left:0;top:0;bottom:0;width:60%;border-radius:8px;background:#2563EB}
.slider::after{content:"";position:absolute;left:calc(60% - 28px);top:-20px;width:56px;height:56px;border-radius:50%;background:#fff}
.hint{position:absolute;left:48px;right:48px;bottom:40px;text-align:center;color:#A6B0C0;font-size:28px}
.progress{position:absolute;left:0;right:0;bottom:0;height:12px;background:#1E2635}
.progress i{display:block;height:100%;width:62%;background:#2563EB}
.top .iconbtn[data-co]::after{left:2px;top:2px}
.mapph .lab.bot{top:auto;bottom:20px}
.copad{padding:0 52px}
'''

def top(title, back=True, right=''):
    b = f'<div class="iconbtn" data-co="b">{ic("back",48)}</div>' if back else '<div style="width:16px"></div>'
    return f'<div class="top">{b}<div class="title">{title}</div>{right}</div>'

# ---------------------------------------------------------------- screens
S = []
def screen(sid, name, when, entry, funcs, wire, spec, note=''):
    S.append(dict(id=sid, name=name, when=when, entry=entry, funcs=funcs, wire=wire, spec=spec, note=note))

# S-01 home
screen('S-01', '홈', '주행 중 · 정차 중', '앱 실행', 'F-01, F-09, F-12, F-15',
 f'''
 <div class="top" style="border-bottom:0">
   <div style="width:16px"></div><div class="title">다음에 가게</div>
   <div class="iconbtn" data-co="5">{ic("gear",48)}</div>
 </div>
 <div class="body" style="display:grid;grid-template-columns:600px 1fr;gap:48px;padding-top:24px">
   <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:36px">
     <div class="mic" data-co="1">{ic("mic",160)}</div>
     <div style="font-size:36px;font-weight:600">눌러서 말하기</div>
     <div class="muted" style="text-align:center">“방금 지난 가게 저장해줘”<br>“뭐 먹을까”</div>
   </div>
   <div style="display:flex;flex-direction:column;gap:32px;justify-content:center">
     <div class="btn big" data-co="2">{ic("fork",44)} 뭐 먹을까</div>
     <div class="btn big" data-co="3">{ic("list",44)} 내 맛집 <span class="muted" style="font-weight:400;font-size:28px">12</span></div>
     <div class="card" data-co="4" style="display:flex;align-items:center;gap:24px;padding:28px 32px">
       {ic("pin",44,"muted")}
       <div style="flex:1"><div class="small muted">최근 저장</div><div style="font-size:32px;font-weight:600">서울갈비</div></div>
       <span class="chip warn">미확인</span>
     </div>
   </div>
 </div>
 ''',
 [('1','말하기 버튼','360dp 원형, 화면의 유일한 1순위 동작','누르면 S-02. 주행 중에도 사용'),
  ('2','뭐 먹을까','추천 바로가기','누르면 S-06'),
  ('3','내 맛집','저장 개수 표시','정차 중 S-07, 주행 중 S-12'),
  ('4','최근 저장','마지막으로 저장한 가게 1건, 상태 칩(미확인/저장됨)','누르면 S-08 (미확인이면 S-09)'),
  ('5','설정','','S-11')],
 '주행 중 화면은 버튼 4개가 전부다. 글자 최소 28dp, 터치 최소 88dp.')

# S-02 listening
screen('S-02', '듣는 중', '주행 중', 'S-01 말하기 / 리마인드·추천 뒤 대답 대기', 'F-01, F-03',
 f'''
 <div class="top" style="border-bottom:0"><div style="width:16px"></div><div class="title">다음에 가게</div></div>
 <div class="overlay"></div>
 <div class="card" style="position:absolute;left:160px;top:200px;width:960px;height:640px;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:48px">
   <div class="h48 copad" data-co="1">듣고 있어요</div>
   <div class="wave" data-co="2">{"".join(f'<i style="height:{h}px"></i>' for h in (28,56,96,120,80,44,104,72,36,88,112,60,32,76,100,52,24))}</div>
   <div style="font-size:40px;text-align:center;min-height:120px" data-co="3">방금 지난 빨간 간판 고깃집<span class="muted">…</span></div>
   <div class="btn" data-co="4" style="width:320px">{ic("close",40)} 취소</div>
 </div>
 ''',
 [('1','상태 문구','듣고 있어요 → 알아듣는 중… 순서로 바뀜',''),
  ('2','소리 막대','마이크 입력에 따라 움직임',''),
  ('3','인식 중 문장','STT 부분 결과를 실시간 표시 (40dp)','말이 끝나면 1.5초 뒤 자동 종료'),
  ('4','취소','녹음 중단, 홈으로','')],
 '홈 위에 겹치는 레이어. 대답(예/아니요)을 기다릴 때도 같은 레이어를 쓴다.')

# S-03 confirm
screen('S-03', '확인', '주행 중', 'S-02 저장 발화 뒤 후보가 있을 때', 'F-04, F-05',
 f'''
 <div class="body full" style="display:flex;flex-direction:column;align-items:center;gap:40px;padding-top:72px">
   <div class="h48 copad" data-co="1">이 가게 맞나요?</div>
   <div class="card" data-co="2" style="width:1000px;height:400px;display:flex;flex-direction:column;justify-content:center;gap:20px;padding:48px 56px">
     <div class="h64">서울갈비</div>
     <div style="font-size:32px">고깃집 · 120m 뒤 · 오른쪽</div>
     <div class="muted">서울 강남구 강남대로 123</div>
   </div>
   <div style="display:flex;gap:48px">
     <div class="btn primary" data-co="3" style="width:480px;height:136px;font-size:40px">{ic("check",48)} 예</div>
     <div class="btn" data-co="4" style="width:480px;height:136px;font-size:40px">아니요</div>
   </div>
   <div class="hint" data-co="5" style="display:flex;align-items:center;justify-content:center;gap:20px"><span class="ring"></span>5초 안에 대답이 없으면 위치만 저장해요</div>
 </div>
 ''',
 [('1','질문','TTS로 같은 문장을 읽음',''),
  ('2','후보 카드','가게 이름 64dp, 업종·거리·방향, 주소','1순위 후보 1개만'),
  ('3','예','저장 → S-04 (저장했어요)','음성 “응·맞아”와 같음'),
  ('4','아니요','2순위가 있으면 카드만 바뀜(1회), 없으면 S-04 (미확인)','음성 “아니”와 같음'),
  ('5','대기 표시','5초 카운트다운','시간 지나면 미확인 저장')],
 '')

# S-04 saved toast
screen('S-04', '저장 완료', '주행 중', 'S-03 뒤 / 후보 없음·오프라인', 'F-06',
 f'''
 <div class="top" style="border-bottom:0"><div style="width:16px"></div><div class="title">다음에 가게</div><div class="iconbtn">{ic("gear",48)}</div></div>
 <div class="body" style="display:grid;grid-template-columns:600px 1fr;gap:48px;padding-top:24px;opacity:.55">
   <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:36px">
     <div class="mic">{ic("mic",160)}</div><div style="font-size:36px;font-weight:600">눌러서 말하기</div><div class="muted">&nbsp;</div>
   </div>
   <div style="display:flex;flex-direction:column;gap:32px;justify-content:center">
     <div class="btn big">{ic("fork",44)} 뭐 먹을까</div><div class="btn big">{ic("list",44)} 내 맛집</div>
     <div class="card" style="height:120px"></div>
   </div>
 </div>
 <div class="toast" data-co="1" style="top:300px">{ic("check",48,"ok")}<span>저장했어요 · 서울갈비</span></div>
 <div class="toast" data-co="2" style="top:460px;height:150px;align-items:flex-start;padding-top:28px;flex-direction:column;gap:8px">
   <div style="display:flex;gap:24px;align-items:center">{ic("pin",48,"muted")}<span>위치를 기억해 둘게요</span></div>
   <div class="muted small" style="font-weight:400;padding-left:72px">멈추면 어느 가게인지 확인해 주세요</div>
 </div>
 ''',
 [('1','저장 토스트','가게 이름 포함, 4초 뒤 사라짐','TTS “저장했어요”'),
  ('2','미확인 토스트','후보 없음·오프라인·두 번 아니요일 때','TTS “위치를 기억해 둘게요”')],
 '두 토스트는 동시에 뜨지 않는다. 그림은 두 가지 모양을 한 번에 보여 준 것.')

# S-05 reminder
screen('S-05', '리마인드', '주행 중', '저장 가게 500m 안 진입 (시스템 알림)', 'F-08, F-10',
 f'''
 <div class="body full" style="padding:0">
   <div class="mapph" style="position:absolute;inset:0;border:0;border-radius:0"><div class="lab bot">현재 화면 (내비 등)</div></div>
   <div class="card" data-co="1" style="position:absolute;left:48px;right:48px;top:48px;height:300px;display:flex;align-items:center;gap:32px;padding:36px 40px;background:#151B26">
     {ic("pin",72)}
     <div style="flex:1">
       <div class="h40">지난주에 저장한 서울갈비</div>
       <div style="font-size:32px;margin-top:8px">300m 앞 · 오른쪽</div>
       <div class="muted small" style="margin-top:12px">“응”이라고 말해도 돼요</div>
     </div>
     <div class="btn primary big" data-co="2" style="width:300px">{ic("navi",40)} 안내 시작</div>
     <div class="btn big" data-co="3" style="width:200px">닫기</div>
   </div>
 </div>
 ''',
 [('1','알림 카드','가게 이름·저장 시기·거리·방향. TTS로 “…300m 앞이에요. 갈까요?”','10초 뒤 자동으로 닫힘'),
  ('2','안내 시작','내비 목적지 설정 (requestRoute)','음성 “응”과 같음'),
  ('3','닫기','오늘은 다시 알리지 않음','음성 “아니”·무응답과 같음')],
 '차량 알림(heads-up) 형태. 같은 가게는 하루 1회.')

# S-06 recommend
def rcard(n, name, meta, note, sel=False):
    co = ' data-co="1"' if n == 1 else ''
    return f'''<div class="card"{co} style="width:368px;height:640px;display:flex;flex-direction:column;gap:20px;padding:36px 32px">
      <div class="num">{n}</div>
      <div class="h40">{name}</div>
      <div style="font-size:28px">{meta}</div>
      <div class="muted small" style="flex:1">{note}</div>
      <div class="btn primary"{' data-co="2"' if n==1 else ''}>{ic("navi",36)} 안내 시작</div>
    </div>'''
screen('S-06', '추천', '주행 중 · 정차 중', 'S-01 뭐 먹을까 / 음성 “뭐 먹을까”', 'F-09, F-10',
 f'''
 {top('뭐 먹을까')}
 <div class="body" style="display:flex;flex-direction:column;gap:40px;align-items:center;padding-top:40px">
   <div style="display:flex;gap:40px">
     {rcard(1,'서울갈비','고깃집 · 1.2km','저장 11월 2일 · 안 가 봄')}
     {rcard(2,'카페 노을','카페 · 2.0km','방문 1회 · “괜찮았어”')}
     {rcard(3,'국밥집','국밥 · 3.4km','저장 10월 28일')}
   </div>
   <div class="btn" data-co="3" style="width:400px">다른 곳 추천</div>
 </div>
 ''',
 [('1','추천 카드 3장','번호·이름·업종·거리·메모. TTS는 번호와 이름만 읽음','“2번” 또는 이름을 말하면 선택'),
  ('2','안내 시작','카드별 1개, 내비 목적지 설정','선택 뒤 홈으로'),
  ('3','다른 곳 추천','다음 순위 3곳으로 교체','최대 2회'),
  ('b','뒤로','홈','')],
 '')

# S-07 my places
def lrow(name, meta, chip, sel=False, co=None):
    c = f' data-co="{co}"' if co else ''
    return f'''<div class="row{' sel' if sel else ''}"{c}>{ic("pin",40,"muted")}<div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div>{chip}</div>'''
screen('S-07', '내 맛집', '정차 중', 'S-01 내 맛집', 'F-12, F-07, F-13',
 f'''
 {top('내 맛집', right=f'<div class="textbtn" data-co="4">{ic("share",36)} 공유</div>')}
 <div class="body" style="display:grid;grid-template-columns:560px 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:20px;min-height:0">
     <div style="display:flex;gap:12px" data-co="1"><span class="chip on">전체 12</span><span class="chip">미확인 3</span><span class="chip">방문 4</span><span class="chip">가까운 순</span></div>
     <div data-co="2" style="padding-top:8px"><div style="overflow:hidden">
       {lrow('서울갈비','고깃집 · 1.2km · 11월 2일','<span class="chip warn">미확인</span>', sel=True)}
       {lrow('카페 노을','카페 · 2.0km · 방문 1회','<span class="chip ok">방문</span>')}
       {lrow('국밥집','국밥 · 3.4km · 10월 28일','')}
       {lrow('빵집 (이름 없음)','4.1km · 10월 25일','<span class="chip warn">미확인</span>')}
       {lrow('할머니 손칼국수','칼국수 · 6.8km · 방문 2회','<span class="chip ok">방문</span>')}
       {lrow('파스타 집','양식 · 8.2km · 10월 12일','')}
     </div></div>
   </div>
   <div class="mapph" data-co="3" style="height:100%">
     <div class="lab">지도</div>
     <div class="pin me" style="left:320px;top:420px">{ic("navi",56)}</div>
     <div class="pin" style="left:420px;top:300px">{ic("pin",64)}</div>
     <div class="pin" style="left:180px;top:220px">{ic("pin",64)}</div>
     <div class="pin" style="left:520px;top:560px">{ic("pin",64)}</div>
     <div class="pin" style="left:140px;top:640px;color:#F59E0B">{ic("pin",64)}</div>
   </div>
 </div>
 ''',
 [('1','필터 칩','전체 / 미확인 / 방문, 정렬(가까운 순·최근 저장 순)',''),
  ('2','가게 목록','이름·업종·거리·저장일 또는 방문 횟수·상태 칩, 줄 높이 104dp','누르면 S-08, 미확인은 S-09'),
  ('3','지도','저장 가게 핀(파랑), 미확인 핀(주황), 내 위치','핀을 누르면 목록에서 선택됨. 프로토타입은 이 자리가 비어 있을 수 있음'),
  ('4','공유','S-10',''),
  ('b','뒤로','홈','')],
 '주행 중에 열면 S-12가 대신 뜬다.')

# S-08 detail
screen('S-08', '가게 상세', '정차 중', 'S-07 목록 / 홈 최근 저장', 'F-10, F-11, F-12',
 f'''
 {top('서울갈비', right=f'<div class="iconbtn" data-co="4">{ic("edit",44)}</div>')}
 <div class="body" style="display:grid;grid-template-columns:1fr 480px;grid-template-rows:1fr auto;gap:40px;padding:40px 48px">
   <div style="display:flex;flex-direction:column;gap:28px;padding:20px 0 0 24px" data-co="1">
     <div style="font-size:32px">고깃집 · 1.2km</div>
     <div class="muted">서울 강남구 강남대로 123</div>
     <div style="display:flex;gap:12px"><span class="chip">저장 11월 2일</span><span class="chip ok">방문 1회</span></div>
     <div class="card" style="padding:24px 28px"><div class="small muted">메모</div><div style="font-size:32px">줄 서 있던 집, 괜찮았어</div></div>
     <div class="muted small">저장할 때 한 말 · “방금 지난 빨간 간판 고깃집 저장해줘”</div>
   </div>
   <div class="mapph" data-co="2"><div class="lab">위치</div><div class="pin" style="left:200px;top:140px">{ic("pin",72)}</div></div>
   <div style="grid-column:1 / -1;display:flex;gap:24px;align-items:center">
     <div class="btn primary big" data-co="3" style="width:400px">{ic("navi",40)} 안내 시작</div>
     <div class="btn big" style="width:380px">{ic("share",40)} 공유에 담기</div>
     <div style="flex:1"></div>
     <div class="btn big danger" data-co="5" style="width:200px">{ic("trash",36)} 삭제</div>
   </div>
 </div>
 ''',
 [('1','정보','업종·거리·주소·저장일·방문 횟수·메모·저장할 때 한 말',''),
  ('2','위치 지도','핀 1개',''),
  ('3','안내 시작','내비 목적지 설정','공유에 담기: S-10 목록에 추가'),
  ('4','수정','이름·업종·메모 수정, 후보 다시 고르기(S-09)',''),
  ('5','삭제','확인 창 1번 뒤 삭제, 4초 안에 되돌리기 토스트','다른 버튼과 떨어뜨려 배치')],
 '')

# S-09 unconfirmed
def crow(name, meta, on=False, co=None):
    c = f' data-co="{co}"' if co else ''
    return f'''<div class="row{' sel' if on else ''}"{c} style="height:112px"><div class="radio{' on' if on else ''}"></div><div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div></div>'''
screen('S-09', '미확인 가게 정리', '정차 중', 'S-07 미확인 항목 / S-08 수정', 'F-07',
 f'''
 {top('어느 가게였나요?')}
 <div class="body" style="display:grid;grid-template-columns:1fr 1fr;gap:40px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:24px">
     <div class="card" data-co="1" style="padding:28px 32px"><div class="small muted">11월 7일 18:42 · 강남대로 (북쪽으로 이동 중)</div><div style="font-size:32px;margin-top:8px">“방금 지난 빨간 간판 고깃집 저장해줘”</div></div>
     <div class="mapph" style="flex:1"><div class="lab">그때 위치</div><div class="pin me" style="left:260px;top:260px">{ic("navi",56)}</div><div class="pin" style="left:330px;top:200px">{ic("pin",56)}</div><div class="pin" style="left:380px;top:320px">{ic("pin",56)}</div><div class="pin" style="left:190px;top:340px">{ic("pin",56)}</div></div>
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
  ('3','이걸로 저장','선택한 후보로 확정 → S-08',''),
  ('4','직접 검색','이름으로 검색해 고르기 (정차 중 키보드)',''),
  ('5','삭제','기록 삭제, 되돌리기 토스트','')],
 '')

# S-10 share
def srow(name, meta, on=True):
    return f'''<div class="row" style="height:96px"><div class="check{' on' if on else ''}">{ic("check",32) if on else ''}</div><div style="flex:1"><div class="name">{name}</div><div class="muted small">{meta}</div></div></div>'''
screen('S-10', '맛집 지도 공유', '정차 중', 'S-07 공유 / S-08 공유에 담기', 'F-13',
 f'''
 {top('맛집 지도 공유')}
 <div class="body" style="display:grid;grid-template-columns:520px 1fr;gap:48px;padding:32px 48px">
   <div style="display:flex;flex-direction:column;gap:16px">
     <div style="display:flex;align-items:center;gap:16px;padding-left:4px" data-co="1"><span class="chip on">전체 선택</span><span class="muted small">7곳 선택됨 · 미확인 가게는 뺌</span></div>
     <div>
       {srow('서울갈비','고깃집')}{srow('카페 노을','카페 · 방문 1회')}{srow('국밥집','국밥')}{srow('할머니 손칼국수','칼국수 · 방문 2회')}{srow('파스타 집','양식')}{srow('두부마을','한식', on=False)}
     </div>
   </div>
   <div style="display:flex;flex-direction:column;gap:28px">
     <div><div class="small muted" style="margin-bottom:8px">제목</div><div class="input" data-co="2">재원의 강릉 맛집 7곳</div></div>
     <div style="display:flex;gap:32px;align-items:center">
       <div class="qr" data-co="3" style="width:240px;height:240px"></div>
       <div style="flex:1;display:flex;flex-direction:column;gap:20px">
         <div class="muted small" style="word-break:break-all">nextplace.app/s/8Kd2…</div>
         <div class="btn primary" data-co="4">링크 복사</div>
         <div class="btn">{ic("share",36)} 보내기</div>
       </div>
     </div>
     <div class="card" data-co="5" style="padding:24px 28px"><div class="small muted">받는 사람 화면</div><div style="font-size:28px;margin-top:6px">링크를 열면 가게 목록이 보이고, 앱이 있으면 <b>내 지도에 추가</b> 버튼이 뜬다.</div></div>
   </div>
 </div>
 ''',
 [('1','가게 고르기','체크 목록, 전체 선택. 미확인 가게는 목록에 없음',''),
  ('2','제목','기본값 “이름의 맛집 N곳”, 정차 중 키보드로 수정',''),
  ('3','QR','동승자가 폰으로 바로 찍음',''),
  ('4','링크 복사 / 보내기','보내기는 차량의 다른 앱(문자 등)으로 전달. 링크에 가게 이름·업종·주소·좌표·메모만 담김','방문 기록·차량 위치는 안 들어감'),
  ('5','받는 사람','웹 목록 → 앱 있으면 내 지도에 추가','')],
 '')

# S-11 settings
def setrow(label, ctrl, co=None, danger=False):
    c = f' data-co="{co}"' if co else ''
    col = ' style="color:#F87171"' if danger else ''
    return f'<div class="row"{c} style="height:112px;{"color:#F87171" if danger else ""}"><div class="name" style="flex:1"{col}>{label}</div>{ctrl}</div>'
screen('S-11', '설정', '정차 중', 'S-01 설정', 'F-15',
 f'''
 {top('설정')}
 <div class="body" style="padding:24px 48px;display:flex;flex-direction:column">
   {setrow('리마인드','<div class="toggle"></div>', co='1')}
   {setrow('알림 거리','<div class="seg"><div>300m</div><div class="on">500m</div><div>1km</div></div>', co='2')}
   {setrow('식사 시간에만 알림','<div class="toggle"></div>', co='3')}
   {setrow('음성 안내 음량','<div style="display:flex;align-items:center;gap:24px;width:480px">'+ic("volume",40,"muted")+'<div class="slider"></div></div>', co='4')}
   {setrow('가게를 특정할 때 쓰는 시간','<div class="seg"><div>10초</div><div class="on">20초</div><div>30초</div></div>')}
   <div style="flex:1"></div>
   {setrow('저장한 가게 모두 삭제','<div class="btn danger" style="height:72px">삭제</div>', co='5', danger=True)}
   <div class="muted small" style="padding:16px 24px 0">버전 0.1 · 저장 가게와 방문 기록은 이 차 안에만 보관</div>
 </div>
 ''',
 [('1','리마인드','켜기/끄기',''),
  ('2','알림 거리','300m / 500m / 1km',''),
  ('3','식사 시간에만 알림','11~14시, 17~20시',''),
  ('4','음성 안내 음량','',''),
  ('5','모두 삭제','확인 창 뒤 삭제. 다른 항목과 떨어뜨림','')],
 '')

# S-12 driving lock
screen('S-12', '주행 중 안내', '주행 중', '주행 중 S-07~S-11 진입 시', 'F-14',
 f'''
 {top('내 맛집')}
 <div class="body" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:40px">
   <div data-co="1" style="display:flex;flex-direction:column;align-items:center;gap:24px">{ic("car",120,"muted")}<div class="h48">멈추면 볼 수 있어요</div><div class="muted" style="font-size:32px">주행 중에는 음성으로 이용해 주세요</div></div>
   <div class="btn primary" data-co="2" style="width:400px;height:112px;font-size:36px">{ic("mic",44)} 말하기</div>
 </div>
 ''',
 [('1','안내','정차(주차 브레이크 또는 속도 0)하면 원래 화면으로 자동 전환',''),
  ('2','말하기','S-02','')],
 '')

# ---------------------------------------------------------------- flow svg
FLOW_SVG = '''<svg viewBox="0 0 1100 575" role="img" aria-label="화면 흐름" font-size="18" class="flow">
<defs><marker id="fa" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="ah"/></marker></defs>
<g class="lane"><rect x="20" y="20" width="1060" height="250" rx="16"/><text x="40" y="52" class="lanelab">주행 중 · 음성 위주</text></g>
<g class="lane"><rect x="20" y="300" width="1060" height="255" rx="16"/><text x="40" y="332" class="lanelab">정차 중 · 터치 가능</text></g>
<g class="edges">
 <path d="M300 130H360" marker-end="url(#fa)"/><path d="M540 130H600" marker-end="url(#fa)"/><path d="M780 130H840" marker-end="url(#fa)"/>
 <path d="M930 162V200H220V162" marker-end="url(#fa)"/>
 <path d="M210 162V230H360" marker-end="url(#fa)"/>
 <path d="M540 230H640" marker-end="url(#fa)"/>
 <path d="M840 230H790" marker-start="url(#fa)" />
 <path d="M210 162V415H290" marker-end="url(#fa)"/>
 <path d="M470 415H530" marker-end="url(#fa)"/><path d="M470 400V360H560V382" marker-end="url(#fa)"/><path d="M470 430V480H560V448" marker-end="url(#fa)"/>
 <path d="M140 162V513H940" marker-end="url(#fa)"/>
</g>
<g class="node"><rect x="120" y="98" width="180" height="64" rx="12" class="main"/><text x="210" y="124" text-anchor="middle" font-weight="700">S-01 홈</text><text x="210" y="148" text-anchor="middle" class="q">말하기 · 뭐 먹을까</text></g>
<g class="node"><rect x="360" y="98" width="180" height="64" rx="12"/><text x="450" y="124" text-anchor="middle" font-weight="700">S-02 듣는 중</text><text x="450" y="148" text-anchor="middle" class="q">STT</text></g>
<g class="node"><rect x="600" y="98" width="180" height="64" rx="12"/><text x="690" y="124" text-anchor="middle" font-weight="700">S-03 확인</text><text x="690" y="148" text-anchor="middle" class="q">예 / 아니요</text></g>
<g class="node"><rect x="840" y="98" width="180" height="64" rx="12"/><text x="930" y="124" text-anchor="middle" font-weight="700">S-04 저장 완료</text><text x="930" y="148" text-anchor="middle" class="q">토스트 → 홈</text></g>
<g class="node"><rect x="360" y="198" width="180" height="64" rx="12"/><text x="450" y="224" text-anchor="middle" font-weight="700">S-06 추천</text><text x="450" y="248" text-anchor="middle" class="q">카드 3장</text></g>
<g class="node"><rect x="640" y="198" width="150" height="64" rx="12" class="ext"/><text x="715" y="236" text-anchor="middle" font-weight="700">내비 안내</text></g>
<g class="node"><rect x="840" y="198" width="180" height="64" rx="12"/><text x="930" y="224" text-anchor="middle" font-weight="700">S-05 리마인드</text><text x="930" y="248" text-anchor="middle" class="q">500m 안 · 알림</text></g>
<g class="node"><rect x="290" y="383" width="180" height="64" rx="12"/><text x="380" y="409" text-anchor="middle" font-weight="700">S-07 내 맛집</text><text x="380" y="433" text-anchor="middle" class="q">목록 + 지도</text></g>
<g class="node"><rect x="560" y="330" width="180" height="52" rx="12"/><text x="650" y="362" text-anchor="middle" font-weight="700">S-08 가게 상세</text></g>
<g class="node"><rect x="530" y="389" width="180" height="52" rx="12"/><text x="620" y="421" text-anchor="middle" font-weight="700">S-09 미확인 정리</text></g>
<g class="node"><rect x="560" y="448" width="180" height="52" rx="12"/><text x="650" y="480" text-anchor="middle" font-weight="700">S-10 공유</text></g>
<g class="node"><rect x="940" y="487" width="140" height="52" rx="12"/><text x="1010" y="519" text-anchor="middle" font-weight="700">S-11 설정</text></g>
<g class="node"><rect x="770" y="383" width="250" height="64" rx="12" class="warn"/><text x="895" y="409" text-anchor="middle" font-weight="700">S-12 주행 중 안내</text><text x="895" y="433" text-anchor="middle" class="q">아래 화면을 주행 중에 열면 대신 표시</text></g>
<text x="330" y="118" class="q" font-size="15">누름</text><text x="560" y="118" class="q" font-size="15">저장 발화</text><text x="790" y="118" class="q" font-size="15">대답</text>
<text x="548" y="222" class="q" font-size="15">선택</text><text x="800" y="222" class="q" font-size="15">응</text>
<text x="150" y="300" class="q" font-size="15">정차 중</text>
</svg>'''

# ---------------------------------------------------------------- page CSS
PAGE_CSS = r'''
:root{--bg:#fff;--fg:#1f2328;--muted:#59636e;--line:#d1d9e0;--head:#f6f8fa;--accent:#0969da;--accent-bg:#ddf4ff}
@media (prefers-color-scheme: dark){:root{--bg:#0d1117;--fg:#e6edf3;--muted:#9198a1;--line:#3d444d;--head:#161b22;--accent:#4493f8;--accent-bg:#121d2f}}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--fg);font-family:"Pretendard","Apple SD Gothic Neo","Malgun Gothic","Noto Sans KR",system-ui,sans-serif;font-size:16px;line-height:1.65;word-break:keep-all}
main{max-width:1180px;margin:0 auto;padding:40px 24px 80px}
h1{font-size:2em;border-bottom:1px solid var(--line);padding-bottom:.3em;margin:0 0 .4em}
h2{font-size:1.45em;border-bottom:1px solid var(--line);padding-bottom:.3em;margin-top:2.2em}
h3{font-size:1.2em;margin:1.8em 0 .6em}
.meta{color:var(--muted)}
table{border-collapse:collapse;width:100%;font-size:.93em;margin:.6em 0 1em}
th,td{border:1px solid var(--line);padding:7px 11px;vertical-align:top;text-align:left}
th{background:var(--head);font-weight:600;white-space:nowrap}
.tbl{overflow-x:auto}
.sw{display:inline-block;width:14px;height:14px;border-radius:4px;vertical-align:-2px;margin-right:6px;border:1px solid rgba(128,128,128,.4)}
.screen{display:grid;grid-template-columns:640px 1fr;gap:28px;align-items:start}
@media (max-width:1000px){.screen{grid-template-columns:1fr}}
.shot{width:640px;height:540px;overflow:hidden;border-radius:12px;border:1px solid var(--line);background:#0B0F17;position:relative}
.shot .frame{transform:scale(.5);transform-origin:0 0}
.cap{color:var(--muted);font-size:.9em;margin-top:6px}
.spec table{margin-top:0}
.spec .co{display:inline-flex;width:22px;height:22px;border-radius:50%;background:#F59E0B;color:#111;font-weight:700;font-size:13px;align-items:center;justify-content:center}
.note{color:var(--muted);font-size:.93em;margin:.3em 0 0}
.flow{width:100%;height:auto;display:block;margin:.6em 0}
.flow text{fill:var(--fg);font-family:inherit}.flow .q{fill:var(--muted)}
.flow .lane rect{fill:none;stroke:var(--line);stroke-dasharray:6 6}.flow .lanelab{fill:var(--muted);font-size:15px}
.flow .node rect{fill:var(--bg);stroke:var(--muted);stroke-width:1.5}.flow .node rect.main{fill:var(--accent-bg);stroke:var(--accent);stroke-width:2}
.flow .node rect.ext{stroke-dasharray:5 5}.flow .node rect.warn{stroke:#B45309}
.flow .edges path{fill:none;stroke:var(--muted);stroke-width:1.5}.flow .ah{fill:var(--muted)}
.kv td:first-child{white-space:nowrap;color:var(--muted)}
.toc{columns:2;font-size:.95em}.toc a{color:var(--accent);text-decoration:none}
@media print{.shot{break-inside:avoid}h2{break-before:page}h2:first-of-type{break-before:auto}}
'''

# ---------------------------------------------------------------- build page
def tbl(headers, rows):
    h = ''.join(f'<th>{x}</th>' for x in headers)
    b = ''.join('<tr>' + ''.join(f'<td>{c}</td>' for c in r) + '</tr>' for r in rows)
    return f'<div class="tbl"><table><thead><tr>{h}</tr></thead><tbody>{b}</tbody></table></div>'

parts = []
parts.append('<h1>다음에 가게 — 화면 설계서</h1>')
parts.append('<p class="meta">작성일 2026-11-09 · 작성자 재원 (easycd) · 기준 문서: <a href="01_개발기획서.md">01_개발기획서</a>의 기능 ID(F-xx)를 그대로 쓴다.</p>')
parts.append('<p>주행 중에 쓰는 화면은 말하기 버튼과 큰 카드 몇 장으로 끝내고, 목록·편집·지도는 정차 중에만 연다. 화면 12개, 흐름 2개(주행 중·정차 중)다.</p>')

parts.append('<h2 id="base">1. 설계 기준</h2>')
parts.append(tbl(['항목','기준','근거'],[
 ['캔버스','1280 × 1080 (앱 영역). 전체 화면은 1920 × 1080 16:9를 좌우 1:2로 나눈 오른쪽','Pleos Connect 17인치 16:9, 좌우 1:2 분할(현대차그룹 시승기). 해상도 값은 가정이며 에뮬레이터 확인 뒤 조정'],
 ['테마','다크 고정. 배경 #0B0F17, 카드 #151B26, 글자 #F3F5F8, 보조 글자 #A6B0C0','야간 운전 눈부심 방지. UI/UX Pro Max: Dark Mode(OLED) 스타일, Minimalism 스타일'],
 ['강조색','1순위 동작 파랑 #2563EB(흰 글자, 대비 5.2:1). 삭제만 빨강 #DC2626. 미확인 주황, 방문 초록','UI/UX Pro Max 추천 팔레트. 빨강은 차량에서 경고색이라 삭제에만 사용'],
 ['글꼴','Pretendard (없으면 Noto Sans KR). 본문 28dp, 버튼 32dp, 카드 제목 40dp, 질문 48dp, 가게 이름 64dp, 최소 24dp','UI/UX Pro Max 글꼴 추천 Noto Sans KR. 70cm 시청 거리 기준'],
 ['터치 크기','버튼 높이 88dp 기본, 1순위 112dp, 목록 줄 104dp, 최소 76dp, 간격 24dp 이상','Android Automotive 주행 중 최소 76dp. UI/UX Pro Max: 최소 48dp·간격 8dp 이상'],
 ['한 화면 규칙','1순위 버튼은 1개(파랑), 주행 중 글자 줄 수 3줄 이내, 목록 스크롤·키보드는 정차 중에만','기획서 8장 비기능 요구사항'],
 ['아이콘','Lucide 선 아이콘 1종, 이모지 금지','UI/UX Pro Max 체크리스트'],
 ['동작','화면 전환 200ms 이내, 토스트 4초, 확인 대기 5초, 리마인드 카드 10초','']]))

parts.append('<h2 id="list">2. 화면 목록</h2>')
parts.append(tbl(['ID','화면','쓰는 상황','들어오는 길','기능'],
  [[f'<a href="#{s["id"]}">{s["id"]}</a>', s['name'], s['when'], s['entry'], s['funcs']] for s in S]))

parts.append('<h2 id="flow">3. 화면 흐름</h2>')
parts.append(FLOW_SVG)
parts.append('<p class="note">위 줄은 주행 중 음성으로 도는 흐름, 아래 줄은 정차 중 터치 화면이다. 정차 화면을 주행 중에 열면 S-12가 대신 뜬다. 내비 안내는 Pleos 내비 앱으로 넘어가는 바깥 화면이다.</p>')

parts.append('<h2 id="common">4. 공통 요소</h2>')
parts.append(tbl(['요소','크기','규칙'],[
 ['상단 바','높이 96dp: 뒤로(80×80) · 제목 36dp · 오른쪽 동작 1개','홈에는 뒤로 없음. 제목은 화면 이름 그대로'],
 ['1순위 버튼','파랑, 높이 88~136dp','화면에 1개만'],
 ['2순위 버튼','테두리형, 높이 88dp',''],
 ['카드','모서리 28dp, 안쪽 여백 32dp, 테두리 #2C3646',''],
 ['상태 칩','높이 48dp: 미확인(주황) · 방문(초록) · 선택(파랑)','글자와 색을 같이 써서 색만으로 구분하지 않음'],
 ['토스트','하단 중앙, 높이 120dp, 4초','되돌리기 버튼은 삭제 뒤에만'],
 ['음성 겹침 화면','S-02를 어느 화면 위에도 띄움','']]))

parts.append('<h2 id="screens">5. 화면별 설계</h2>')
for s in S:
    spec_rows = []
    for co, el, what, act in s['spec']:
        badge = '<span class="co">←</span>' if co == 'b' else f'<span class="co">{co}</span>'
        spec_rows.append([badge, el, what, act])
    parts.append(f'<h3 id="{s["id"]}">{s["id"]} {s["name"]} <span class="meta" style="font-weight:400;font-size:.8em">· {s["when"]} · {s["funcs"]}</span></h3>')
    parts.append('<div class="screen">')
    parts.append(f'<div><div class="shot"><div class="frame">{s["wire"]}</div></div><div class="cap">1280×1080 기준, 50% 축소</div></div>')
    parts.append('<div class="spec">' + tbl(['','요소','내용','동작'], spec_rows) + (f'<p class="note">{s["note"]}</p>' if s['note'] else '') + '</div>')
    parts.append('</div>')

parts.append('<h2 id="states">6. 상태와 예외 화면</h2>')
parts.append(tbl(['상황','보이는 것','문구'],[
 ['저장 가게 0개','S-07 목록 자리에 안내 + 말하기 버튼','“아직 저장한 가게가 없어요. 운전하다 말해 보세요.”'],
 ['인식 실패','S-02 문구 교체 후 1회 재시도','“다시 한 번 말해 주세요”'],
 ['인터넷 없음','S-03 없이 S-04 미확인 토스트','“위치를 기억해 둘게요”'],
 ['추천할 가게 없음','S-06 카드 대신 안내 1줄','“근처 10km 안에 저장한 가게가 없어요”'],
 ['삭제 뒤','토스트 + 되돌리기 버튼 4초','“삭제했어요 · 되돌리기”'],
 ['처음 실행','S-01 위에 안내 카드 1장 (위치·음성 사용 고지)','“차 안에만 저장돼요” + 확인 버튼']]))

parts.append('<h2 id="next">7. 확인이 필요한 사항</h2>')
parts.append('<ul>'
 '<li>캔버스 1280×1080은 가정이다. 에뮬레이터 실제 해상도를 확인하면 비율만 다시 맞춘다.</li>'
 '<li>S-07 지도는 카카오맵 SDK가 차량에서 뜨는지에 달려 있다. 안 뜨면 프로토타입은 목록만 쓰고 지도 자리는 비워 둔다.</li>'
 '<li>S-05 리마인드를 앱 밖(내비 화면 위)에 띄우려면 차량 알림 권한이 필요하다. 안 되면 앱이 켜져 있을 때만 띄운다.</li>'
 '<li>스티어링 휠 음성 버튼 연동은 Pleos SDK에 없어 말하기 버튼은 화면에만 둔다.</li>'
 '</ul>')

doc = f'''<!DOCTYPE html>
<html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>다음에 가게 — 화면 설계서</title>
<style>{PAGE_CSS}{WIRE_CSS}</style></head>
<body><main>
{''.join(parts)}
</main></body></html>'''
with open(os.path.join(OUT_DIR, '02_화면설계서.html'), 'w', encoding='utf-8') as f:
    f.write(doc)

# standalone wire pages for PNG export (scale 1)
for s in S:
    page = f'''<!DOCTYPE html><html lang="ko"><head><meta charset="utf-8"><style>body{{margin:0;background:#0B0F17}}{WIRE_CSS}</style></head>
<body><div class="frame" id="f">{s["wire"]}</div></body></html>'''
    with open(os.path.join(WIRE_DIR, f'{s["id"]}.html'), 'w', encoding='utf-8') as f:
        f.write(page)

# markdown version (images referenced from docs/screens/)
md = ['# 다음에 가게 — 화면 설계서', '', '작성일 2026-11-09 · 작성자 재원 (easycd) · 기준 문서: [01_개발기획서](01_개발기획서.md)의 기능 ID(F-xx)를 그대로 쓴다.', '',
      '주행 중에 쓰는 화면은 말하기 버튼과 큰 카드 몇 장으로 끝내고, 목록·편집·지도는 정차 중에만 연다. 화면 12개, 흐름 2개(주행 중·정차 중)다. 와이어프레임이 들어간 원본은 [02_화면설계서.html](02_화면설계서.html)이다.', '']
def mdtbl(headers, rows):
    out = ['| ' + ' | '.join(headers) + ' |', '| ' + ' | '.join('---' for _ in headers) + ' |']
    for r in rows:
        out.append('| ' + ' | '.join(str(c).replace('|', '／').replace('\n', ' ') for c in r) + ' |')
    return '\n'.join(out)
import re
def strip(s):
    s = re.sub(r'<span class="co">(.*?)</span>', r'\1', s)
    s = re.sub(r'<[^>]+>', '', s)
    return html.unescape(s)
md += ['## 1. 설계 기준', '', mdtbl(['항목','기준','근거'],[
 ['캔버스','1280 × 1080 (앱 영역). 전체 화면은 1920 × 1080 16:9를 좌우 1:2로 나눈 오른쪽','Pleos Connect 17인치 16:9, 좌우 1:2 분할. 해상도 값은 가정이며 에뮬레이터 확인 뒤 조정'],
 ['테마','다크 고정. 배경 #0B0F17, 카드 #151B26, 글자 #F3F5F8, 보조 글자 #A6B0C0','야간 눈부심 방지. UI/UX Pro Max: Dark Mode(OLED), Minimalism'],
 ['강조색','1순위 파랑 #2563EB(흰 글자). 삭제만 빨강 #DC2626. 미확인 주황, 방문 초록','빨강은 차량 경고색이라 삭제에만'],
 ['글꼴','Pretendard (없으면 Noto Sans KR). 본문 28dp, 버튼 32dp, 카드 제목 40dp, 질문 48dp, 가게 이름 64dp, 최소 24dp','70cm 시청 거리'],
 ['터치 크기','버튼 88dp 기본, 1순위 112dp, 목록 줄 104dp, 최소 76dp, 간격 24dp 이상','Android Automotive 주행 중 최소 76dp'],
 ['한 화면 규칙','1순위 버튼 1개, 주행 중 글자 3줄 이내, 스크롤·키보드는 정차 중에만','기획서 8장'],
 ['아이콘','Lucide 선 아이콘 1종, 이모지 금지',''],
 ['동작','전환 200ms 이내, 토스트 4초, 확인 대기 5초, 리마인드 10초','']]), '']
md += ['## 2. 화면 목록', '', mdtbl(['ID','화면','쓰는 상황','들어오는 길','기능'], [[s['id'], s['name'], s['when'], s['entry'], s['funcs']] for s in S]), '']
md += ['## 3. 화면 흐름', '', '```mermaid', 'flowchart LR',
 '  subgraph D[주행 중 · 음성 위주]', '    S01[S-01 홈] -->|누름| S02[S-02 듣는 중] -->|저장 발화| S03[S-03 확인] -->|대답| S04[S-04 저장 완료] --> S01',
 '    S01 --> S06[S-06 추천] -->|선택| NAV([내비 안내])', '    S05[S-05 리마인드] -->|응| NAV', '  end',
 '  subgraph P[정차 중 · 터치 가능]', '    S01 -->|정차 중| S07[S-07 내 맛집] --> S08[S-08 가게 상세]', '    S07 --> S09[S-09 미확인 정리]', '    S07 --> S10[S-10 공유]', '    S01 --> S11[S-11 설정]', '  end',
 '  S07 -. 주행 중 .-> S12[S-12 주행 중 안내]', '```', '',
 '위 줄은 주행 중 음성으로 도는 흐름, 아래 줄은 정차 중 터치 화면이다. 정차 화면을 주행 중에 열면 S-12가 대신 뜬다.', '']
md += ['## 4. 공통 요소', '', mdtbl(['요소','크기','규칙'],[
 ['상단 바','높이 96dp: 뒤로(80×80) · 제목 36dp · 오른쪽 동작 1개','홈에는 뒤로 없음'],
 ['1순위 버튼','파랑, 높이 88~136dp','화면에 1개만'],
 ['2순위 버튼','테두리형, 높이 88dp',''],
 ['카드','모서리 28dp, 안쪽 여백 32dp',''],
 ['상태 칩','높이 48dp: 미확인(주황) · 방문(초록) · 선택(파랑)','글자와 색을 같이 씀'],
 ['토스트','하단 중앙, 높이 120dp, 4초','되돌리기는 삭제 뒤에만'],
 ['음성 겹침 화면','S-02를 어느 화면 위에도 띄움','']]), '']
md += ['## 5. 화면별 설계', '']
for s in S:
    md += [f'### {s["id"]} {s["name"]}', '', f'{s["when"]} · 들어오는 길: {s["entry"]} · {s["funcs"]}', '',
           f'![{s["id"]} {s["name"]}](screens/{s["id"]}.png)', '',
           mdtbl(['번호','요소','내용','동작'], [[('뒤로' if co=='b' else co), el, what, act] for co, el, what, act in s['spec']]), '']
    if s['note']:
        md += [s['note'], '']
md += ['## 6. 상태와 예외 화면', '', mdtbl(['상황','보이는 것','문구'],[
 ['저장 가게 0개','S-07 목록 자리에 안내 + 말하기 버튼','“아직 저장한 가게가 없어요. 운전하다 말해 보세요.”'],
 ['인식 실패','S-02 문구 교체 후 1회 재시도','“다시 한 번 말해 주세요”'],
 ['인터넷 없음','S-03 없이 S-04 미확인 토스트','“위치를 기억해 둘게요”'],
 ['추천할 가게 없음','S-06 카드 대신 안내 1줄','“근처 10km 안에 저장한 가게가 없어요”'],
 ['삭제 뒤','토스트 + 되돌리기 4초','“삭제했어요 · 되돌리기”'],
 ['처음 실행','S-01 위에 안내 카드 1장','“차 안에만 저장돼요” + 확인']]), '']
md += ['## 7. 확인이 필요한 사항', '',
 '- 캔버스 1280×1080은 가정이다. 에뮬레이터 실제 해상도를 확인하면 비율만 다시 맞춘다.',
 '- S-07 지도는 카카오맵 SDK가 차량에서 뜨는지에 달려 있다. 안 뜨면 프로토타입은 목록만 쓰고 지도 자리는 비워 둔다.',
 '- S-05 리마인드를 앱 밖(내비 화면 위)에 띄우려면 차량 알림 권한이 필요하다. 안 되면 앱이 켜져 있을 때만 띄운다.',
 '- 스티어링 휠 음성 버튼 연동은 Pleos SDK에 없어 말하기 버튼은 화면에만 둔다.', '']
with open(os.path.join(OUT_DIR, '02_화면설계서.md'), 'w', encoding='utf-8') as f:
    f.write('\n'.join(md))
print('ok', len(S), 'screens')
