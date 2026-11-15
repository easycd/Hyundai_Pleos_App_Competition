# 다음에 가게

> 운전 중 스쳐 지나간 가게를 말 한마디로 저장하면, 다시 지나갈 때 Pleos가 대신 기억해 알려 주는 차량용 맛집 메모·추천 앱

말하기 버튼을 누르고 "방금 지난 카페 저장해줘"라고 말하면, 버튼을 누른 순간의 위치·진행 방향으로 지나친 가게를 찾아 "○○ 맞나요?"로 되묻고 저장합니다. 저장한 가게 근처를 지나면 먼저 알려 주고("…앞이에요. 갈까요?"), "뭐 먹을까"라고 물으면 저장한 가게 중 3곳을 골라 Pleos 내비로 바로 안내합니다.

| 기능 | 내용 |
| --- | --- |
| 말 한마디로 저장 | 말하기 → 가게 후보 검색(그때 지점 반경 150m) → 음성으로 되묻기 → "응"이면 저장. 못 찾으면 위치만 '미확인'으로 남겨 정차 후 정리 |
| 근처 리마인드 | 저장한 가게 500m 안에 들어가면 차량 알림 + 음성 → "응"이면 Pleos 내비 안내 |
| 추천 | "뭐 먹을까" → 거리·메모·방문 기록으로 3곳을 카드와 음성으로 → 번호·이름·터치로 선택 → 내비 안내 |
| 방문 기록 | 앱이 안내한 가게에 10분 넘게 머물면 방문으로 기록, 떠날 때 한 줄 메모 |
| 내 맛집 · 공유 | 목록·지도·상세·수정·삭제, 미확인 가게 정리, 고른 가게를 링크·QR로 공유 |
| 주행 제한 | CarUxRestrictions로 정차 전용 화면을 막고, 주행 중에는 음성과 버튼 한 번으로만 |

문서: [개발 기획서](docs/01_개발기획서.md) · [화면 설계서](docs/02_화면설계서.md) · [기술서](docs/03_기술서.md) · 시연 영상 [`다음에가게_시연영상.mp4`](다음에가게_시연영상.mp4)

## APK 파일 위치

- **[`apk/김재원_다음에가게_release.apk`](apk/김재원_다음에가게_release.apk)**
- 릴리스 빌드, 실제 Pleos Connect SDK 사용, 디버그 키 서명, 지원 CPU: arm64-v8a · armeabi-v7a · x86_64 · x86

## 실행 방법

### APK로 바로 실행

1. Pleos Connect 에뮬레이터를 켭니다 (아래 '테스트 환경' 참고, Cold boot).
2. APK를 설치하고 실행합니다.

   ```bash
   adb install -r apk/김재원_다음에가게_release.apk
   adb shell monkey -p com.easycd.nextplace -c android.intent.category.LAUNCHER 1
   ```

3. 처음 실행 안내에서 **시작하기**를 눌러 마이크·위치·내비·알림 권한을 허용합니다.
4. 에뮬레이터의 기본 GPS는 미국으로 잡혀 있으므로 한국 좌표로 옮깁니다 (예: 판교 제2테크노밸리).

   ```bash
   adb emu geo fix 127.0995 37.4123
   ```

5. 홈의 **말하기** 버튼을 누르고 "방금 지난 카페 저장해줘", "뭐 먹을까" 등을 말합니다. PC 마이크로 말하려면 에뮬레이터 확장 컨트롤(…) → Microphone → *Virtual microphone uses host audio input*을 켭니다.

### 소스에서 빌드

```bash
./gradlew :app:assembleRelease    # app/build/outputs/apk/release/app-release.apk
./gradlew :app:installDebug       # 연결된 에뮬레이터에 디버그 빌드 설치
./gradlew testDebugUnitTest :core:domain:test   # 단위·파이프라인·구조 테스트
```

## 테스트 환경 및 필요한 설정

| 항목 | 값 |
| --- | --- |
| 개발 PC | Windows 11, Android Studio (Ladybug 이상), JDK 17 (Android Studio 내장 JBR) |
| 빌드 | Android Gradle Plugin 8.x, Kotlin 2.x, compileSdk / targetSdk 35, minSdk 28 |
| 실행 환경 | Pleos Connect Emulator 2.1.0 (Android 14, API 34) — 실제 Pleos SDK로 검증 |
| 보조 확인 | Android 에뮬레이터 Pixel 8 (카카오맵 화면 확인용, 아래 참고) |
| Pleos SDK | SpeechToText 3.3.1 · TextToSpeech 3.3.1 · FusedLocation 1.1.3 · NaviHelper 3.0.5 · Vehicle 2.3.2 |
| 외부 API | 카카오 로컬 REST API(가게 검색·도로명), 카카오맵 Android SDK 2.15.2(지도) |
| 네트워크 | 가게 검색에 인터넷 필요 (없으면 위치만 '미확인'으로 저장하고 연결되면 다시 찾음) |

### 빌드에 필요한 설정 — `local.properties` (저장소에 올리지 않음)

```properties
sdk.dir=...                 # Android Studio가 자동으로 채움
KAKAO_REST_KEY=...          # 카카오 로컬 API REST 키 (가게 검색). 없으면 개발용 샘플 가게 목록으로 검색
KAKAO_NATIVE_KEY=...        # 카카오맵 네이티브 앱 키. 없으면 배경 없는 핀 지도
PLEOS_CLIENT_ID=...         # Pleos Playground 자격 증명 (STT·TTS 서버 모드). 없으면 온디바이스 모드
PLEOS_CLIENT_SECRET=...
```

카카오맵은 카카오 개발자 콘솔의 앱 > 플랫폼 > Android에 패키지명 `com.easycd.nextplace`와 빌드한 PC의 키 해시를 등록해야 표시됩니다.

### 빌드 옵션 (`-P`)

| 옵션 | 뜻 |
| --- | --- |
| `nextplace.fakeSdk=true` | Pleos SDK 대신 가짜 SDK (일반 휴대폰 에뮬레이터용) |
| `nextplace.fakeLocation=true` | `docs/tools/route_gangnam_60s.json` 경로를 가짜 위치로 재생 |
| `nextplace.abi=arm64-v8a` | ARM만 넣어 빌드 (x86_64 휴대폰 에뮬레이터에서 카카오맵 확인용) |
| `nextplace.demoVoice=true` | 시연 녹화용: 말 대신 `adb shell am broadcast -a com.easycd.nextplace.DEMO_SAY --es text "'문장'"`으로 입력 |

### 참고 사항

- 카카오맵 엔진은 ARM 전용이라 **Pleos 에뮬레이터(x86_64)에서는 지도 대신 핀 지도**로 표시됩니다. 실제 차량(ARM)에서는 카카오맵이 뜹니다. 카카오맵 화면은 Pixel 에뮬레이터에서 `-Pnextplace.fakeSdk=true -Pnextplace.abi=arm64-v8a`로 확인했습니다.
- Pleos 에뮬레이터는 차가 멈춰 있으면 위치 갱신이 오지 않아, 근처 리마인드는 위치가 바뀌거나 앱을 다시 켤 때 확인됩니다.
- 리마인드는 기본으로 식사 시간(11~14시, 17~20시)에만 울립니다. 다른 시간에 확인하려면 설정에서 '식사 시간에만 알림'을 끕니다.

## 개발에 사용한 Pleos Connect 에뮬레이터 버전과 화면 규격

| 항목 | 값 |
| --- | --- |
| 에뮬레이터 | Pleos Connect Emulator **v2.1.0** (`system-images;android-34;pleos_car_emulator;x86_64`, 빌드 `release_connect_v2.1.0`) |
| AVD 설정 | Automotive, RAM 4GB, Graphics Hardware, **Cold boot** (Quick boot 시 VHAL 오류) |
| 전체 화면 | **2560 × 1440 px**, 160 dpi (1dp = 1px) |
| 앱 영역 | **1682 × 1210 dp** (화면 오른쪽 앱 칸, 왼쪽은 차량 정보 패널, 아래는 공조·앱 바) |
| 화면 설계 기준 | 가로 1280 × 1080 dp, 세로 1칸 640 × 1080 dp (창 비율을 보고 가로·세로 배치를 자동 선택) |

## 구조

```
app/                 앱 셸: 시작·처음 실행 안내·권한 안내·내비게이션·기능 등록
core/domain          모델·계약·파이프라인 단계·음성 세션 (Android 의존 없음)
core/data            Room·DataStore 저장소
core/pleos           Pleos SDK 어댑터 (STT·TTS·위치·내비·Vehicle·주행 제한·오디오 포커스) + 가짜 SDK
core/poi             카카오 로컬 API 가게 검색 + 캐시
core/map             카카오맵 지도
core/ui              리퀴드 글라스 디자인 시스템, 공통 컴포넌트, 주행 중 잠금, 차량 알림
feature/*            voicesave · remind · recommend · visit · myplaces · share · settings
web/nextplace/s      공유 링크를 폰에서 열 때 보이는 웹 목록 (https://easycd.github.io/nextplace/s, 저장소 easycd/nextplace)
docs/                기획서 · 화면 설계서 · 기술서 · 화면 그림 · 앱 아이콘
```

기능 모듈끼리는 서로 import하지 않고 `core/domain`의 계약으로만 연결하며, 이 규칙은 `ArchitectureTest`(Konsist)가 검사합니다.
