# 다음에 가게 · 앱 아이콘 (확정 15번)

- 디자인: 주황 배경 #E4572E, 흰 핀, 핀 머리 안 주황 원(배지), 흰 숟가락(면)·젓가락(선) 교차, 젓가락이 앞
- 출처 시안: docs/design/icons/주황_시안10_반전.png 의 15번 (orange-ten-mirror/orange-15-A5m.svg)

| 파일 | 용도 |
|---|---|
| app_icon.svg | 마스터 (둥근 사각 배경 포함, 100×100 좌표계) |
| app_icon_48/72/96/144/192/256/512.png | 크기별 PNG (둥근 사각) |
| app_icon_square.svg, app_icon_square_512/1024.png | 모서리 없는 정사각 (스토어·자체 마스크용) |
| ic_launcher_foreground.svg / _432.png | 적응형 아이콘 전경 (108dp 캔버스, 핀은 66dp 안전 영역 안) |
| ic_launcher_background.svg / _432.png | 적응형 아이콘 배경 (단색 #E4572E) |
| ic_launcher_monochrome.svg / _432.png | 단색(테마) 레이어 — 흰 실루엣, 원과 도안은 투명 |
| 확정_아이콘_15.html / .png | 미리보기 시트 |

Android 적용: `res/mipmap-anydpi-v26/ic_launcher.xml` 에서 background 는 `@color/ic_launcher_background`(#E4572E), foreground·monochrome 은 위 SVG 를 Vector Drawable 로 변환해 사용.
