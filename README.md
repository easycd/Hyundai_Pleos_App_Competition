# 다음에 가게

운전하다 지나친 가게를 **말 한마디로 저장**하고, 나중에 근처를 지나면 먼저 알려 주고, "뭐 먹을까"라고 물으면 저장한 가게 중에서 골라 주는 Pleos Connect(Android Automotive) 앱입니다. 2026 현대자동차 Pleos 앱 개발 공모전 출품작입니다.

- 기획서: [docs/01_개발기획서.md](docs/01_개발기획서.md)
- 화면 설계서: [docs/02_화면설계서.md](docs/02_화면설계서.md) (화면 그림: `docs/screens/`)
- 기술서: [docs/03_기술서.md](docs/03_기술서.md)

## 주요 기능

| 기능 | 내용 |
| --- | --- |
| 말 한마디로 저장 (F-01~F-06) | 말하기 버튼 → "방금 지난 카페 저장해줘". 버튼을 누른 순간의 위치·진행 방향으로 그때 지점 반경 150m 가게를 찾아 "○○ 맞나요?"로 되묻고 "응"이면 저장. 못 찾으면 위치만 '미확인'으로 남겨 정차 후 정리 |
| 근처 리마인드 (F-08) | 저장한 가게 500m 안에 들어가면 차량 알림 + 음성 "…앞이에요. 갈까요?" → "응"이면 Pleos 내비 안내. 같은 가게 하루 1번, 식사 시간에만(설정) |
| 추천 (F-09, F-10) | "뭐 먹을까" → 말한 곳 기준 거리·메모·방문 기록으로 3곳을 카드와 음성으로 → 번호·이름·터치로 고르면 내비 안내 |
| 방문 기록 (F-11) | 앱이 안내한 가게에 10분 넘게 머물면 방문으로 기록, 떠날 때 "한 줄 남길까요?" |
| 내 맛집 · 정리 (F-07, F-12) | 목록과 지도, 상세·수정·삭제(4초 되돌리기), 미확인 가게 후보 고르기·직접 검색 |
| 공유 (F-13) | 고른 가게를 링크·QR로 (서버 없이 링크 안에 목록을 담음), 받은 목록 가져오기 |
| 주행 제한 (F-14) | CarUxRestrictions 로 정차 전용 화면을 막고, 주행 중에는 음성과 버튼 한 번으로만 |

사용하는 Pleos Connect SDK: SpeechToText 3.3.1, TextToSpeech 3.3.1, FusedLocation 1.1.3, NaviHelper 3.0.5, Vehicle 2.3.2(진단 로그). 가게 검색과 지도는 카카오 로컬 API · 카카오맵 SDK.

## 빌드

준비물: Android Studio(Ladybug 이상) 또는 JDK 17 + Android SDK 35.

```bash
./gradlew :app:assembleDebug                      # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest :core:domain:test     # 단위·파이프라인·구조 테스트
```

### local.properties (저장소에 올리지 않음)

```properties
sdk.dir=...                     # Android Studio가 자동으로 채움
KAKAO_REST_KEY=...              # 카카오 로컬 API REST 키 (가게 후보 검색·도로명). 없으면 개발용 샘플 가게 목록으로 검색
KAKAO_NATIVE_KEY=...            # 카카오맵 네이티브 앱 키 (내 맛집 지도 배경). 없으면 배경 없는 핀 지도
PLEOS_CLIENT_ID=...             # Pleos Playground 자격 증명 (STT·TTS 서버 모드). 없으면 온디바이스 모드
PLEOS_CLIENT_SECRET=...
```

카카오맵은 카카오 개발자 콘솔의 앱 > 플랫폼 > Android에 패키지명 `com.easycd.nextplace`와 **빌드하는 PC의** 디버그 키 해시를 등록해야 뜹니다(키 해시는 앱 실행 시 logcat `KakaoMaps` 태그에 찍힘).

### 빌드 옵션 (gradle.properties 또는 `-P`)

| 옵션 | 기본 | 뜻 |
| --- | --- | --- |
| `nextplace.fakeSdk` | false | true면 Pleos SDK 대신 가짜 SDK (일반 에뮬레이터용. 주행 제한은 '정차', 말은 화면 입력 칸으로) |
| `nextplace.fakeLocation` | false | true면 `docs/tools/route_gangnam_60s.json` 경로를 가짜 위치로 재생 |
| `nextplace.abi` | (전체) | 넣을 CPU 종류. 예: `arm64-v8a` (x86_64 휴대폰 에뮬레이터에서 카카오맵 확인용) |
| `nextplace.demoVoice` | false | 시연 녹화용: STT 만 가짜로 두고 adb 방송으로 문장을 넣음 (TTS·위치·내비는 실제 SDK) |
| `nextplace.screens` | false | true면 화면 렌더 검사 모듈(`tools/screenshots`)을 포함 |

## 실행

**Pleos Connect Emulator (2.1.0, x86_64)** — 실제 SDK로 확인하는 기본 환경

```bash
./gradlew :app:installDebug
adb emu geo fix 127.0995 37.4123     # 에뮬레이터 기본 GPS(미국)를 한국으로 (예: 판교)
adb forward tcp:8088 tcp:8088        # Virtual Console: http://127.0.0.1:8088/ (기어·도어 등)
```

- 카카오맵 엔진은 ARM 전용이라 Pleos 에뮬레이터(x86_64)에서는 지도 대신 핀 지도로 그립니다. 실제 차량(ARM)에서는 카카오맵이 뜹니다.
- 말하기를 실제로 해 보려면 에뮬레이터 확장 컨트롤 → Microphone → "Virtual microphone uses host audio input"을 켭니다.

**휴대폰 에뮬레이터 (x86_64)** — 휴대폰에는 주행 제한 상태가 없어 정차 화면이 잠기므로 가짜 SDK로 실행합니다.

```bash
./gradlew :app:installDebug -Pnextplace.fakeSdk=true -Pnextplace.abi=arm64-v8a
```

**시연 녹화** — `-Pnextplace.demoVoice=true`로 설치한 뒤 말하기 버튼을 누르고 문장을 넣습니다.

```bash
adb shell am broadcast -a com.easycd.nextplace.DEMO_SAY --es text "'방금 지난 카페 저장해줘'"
```

## 구조 (기술서 3·4장)

```
app/                    앱 셸: 시작 화면, 처음 실행 안내, 권한 안내, 내비게이션, 기능 등록
core/domain             모델·계약·파이프라인 단계·음성 세션 (Android 의존 없음)
core/data               Room·DataStore 저장소
core/pleos              Pleos SDK 어댑터 (STT·TTS·위치·내비·Vehicle·주행 제한·오디오 포커스) + 가짜 SDK
core/poi                카카오 로컬 API 가게 검색 + 캐시
core/map                카카오맵 지도 (키·엔진이 없으면 쓰지 않음)
core/ui                 리퀴드 글라스 디자인 시스템, 공통 컴포넌트, 주행 중 잠금, 차량 알림(heads-up)
feature/voicesave       음성 저장 (홈 · 듣는 중 · 확인 · 저장 완료)
feature/remind          근처 리마인드
feature/recommend       추천 ("뭐 먹을까")
feature/visit           방문 기록 · 방문 메모
feature/myplaces        내 맛집 지도 · 상세 · 수정 · 미확인 정리 · 직접 검색
feature/share           맛집 지도 공유 (링크 · QR) · 받은 목록 가져오기
feature/settings        설정
tools/screenshots       화면 렌더 검사 (Paparazzi, 에뮬레이터 없이)
web/nextplace/s         공유 링크를 폰에서 열 때 보이는 웹 목록 (정적 페이지)
docs/                   기획서 · 화면 설계서 · 기술서, 화면 그림, 앱 아이콘(design/icons/final), 생성 도구
```

기능끼리는 서로 import하지 않고 `core/domain`의 계약으로만 연결합니다. 이 규칙은 `app/src/test/.../ArchitectureTest.kt`(Konsist)가 검사합니다.

## 검사

| 검사 | 명령 |
| --- | --- |
| 단위 · 파이프라인 · 구조 · 리소스 이름 테스트 | `./gradlew testDebugUnitTest :core:domain:test` |
| 화면 렌더 (다크 · 라이트 · 좁은 가로 창) | `./gradlew -Pnextplace.screens=true :tools:screenshots:recordPaparazziDebug` → `tools/screenshots/src/test/snapshots/images/` |
| Lint | `./gradlew :app:lintDebug` |

## 공유 링크 웹 페이지

공유 링크는 `https://easycd.github.io/nextplace/s#<데이터>` 형식이고, 목록은 링크의 `#` 뒤에 압축해 담습니다(서버로 전송되지 않음). 폰에서 열리는 페이지는 공개 저장소 [easycd/nextplace](https://github.com/easycd/nextplace)의 GitHub Pages로 배포되어 있습니다. 원본은 이 저장소의 `web/nextplace/s/index.html`이므로, 고치면 두 곳을 함께 맞춥니다. Pleos 차량에서 링크를 열면 앱이 받아 '받은 목록 가져오기'로 이어집니다.

## 알려진 한계

- **카카오맵**: 엔진이 ARM 전용이라 x86_64 에뮬레이터에서는 핀 지도로 대신 그립니다. 휴대폰 에뮬레이터는 `-Pnextplace.abi=arm64-v8a`로 빌드하면 ARM 변환으로 지도가 뜹니다(Pleos 에뮬레이터는 ARM 변환이 없음).
- **가게 검색**: `KAKAO_REST_KEY`가 없으면 개발용 샘플 가게 목록으로 대신 검색합니다(실제 가게가 아님). 네트워크가 없으면 위치만 미확인으로 저장하고, 연결되면 다시 찾아 홈에 '후보 찾음'으로 알립니다.
- **Pleos 에뮬레이터 위치**: 차가 멈춰 있으면 위치 표본이 오지 않아, 근처 리마인드·방문 판정은 위치가 바뀌거나 앱을 다시 켤 때 확인됩니다. 실제 차량에서는 위치가 계속 들어옵니다.
- **좁은 창**: 화면 설계서 기준(가로 1280dp, 세로 640dp)보다 좁은 창에서는 일부 버튼 글자가 줄어듭니다.
