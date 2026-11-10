# 다음에 가게

운전하다 지나친 가게를 **말 한마디로 저장**하고, 나중에 근처를 지나면 알려 주고, "뭐 먹을까"라고 물으면 저장한 가게 중에서 골라 주는 Pleos Connect(AAOS) 앱입니다.

- 기획서: [docs/01_개발기획서.md](docs/01_개발기획서.md)
- 화면 설계서: [docs/02_화면설계서.md](docs/02_화면설계서.md)
- 기술서: [docs/03_기술서.md](docs/03_기술서.md)

## 빌드

준비물: Android Studio(Ladybug 이상) 또는 JDK 17 + Android SDK 35.

```bash
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest         # 단위·구조 테스트
```

### local.properties (저장소에 올리지 않음)

```properties
sdk.dir=...                     # Android Studio가 자동으로 채움
KAKAO_REST_KEY=...              # 카카오 로컬 API REST 키 (가게 후보 검색). 없으면 후보 없이 '미확인'으로 저장됨
PLEOS_CLIENT_ID=...             # Pleos Playground 자격 증명 (STT·TTS 서버 모드)
PLEOS_CLIENT_SECRET=...
```

### 실행 옵션 (gradle.properties 또는 `-P`)

| 옵션 | 기본 | 뜻 |
| --- | --- | --- |
| `nextplace.fakeSdk` | false | true면 Pleos SDK 대신 가짜 SDK로 실행 (일반 에뮬레이터·개발용) |
| `nextplace.fakeLocation` | false | true면 `docs/tools/route_gangnam_60s.json` 경로를 가짜 위치로 재생 |
| `nextplace.screens` | false | true면 화면 렌더 검사 모듈(`tools/screenshots`)을 포함 |

예: `./gradlew :app:assembleDebug -Pnextplace.fakeSdk=true -Pnextplace.fakeLocation=true`

## 구조 (기술서 3·4장)

```
app/                    앱 셸: 시작 화면, 처음 실행 안내, 권한 안내, 내비게이션, 기능 등록
core/domain             모델·계약·파이프라인 단계 (Android 의존 없음)
core/data               Room·DataStore 저장소
core/pleos              Pleos SDK 어댑터 (STT·TTS·위치·내비·주행 제한) + 가짜 SDK
core/poi                카카오 로컬 API 가게 검색 + 캐시
core/ui                 리퀴드 글라스 디자인 시스템, 공통 컴포넌트, 주행 중 잠금
feature/voicesave       음성 저장 (홈 · 듣는 중 · 확인 · 저장 완료)
feature/remind          근처 리마인드
feature/recommend       추천 ("뭐 먹을까")
feature/visit           방문 기록 · 방문 메모
feature/myplaces        내 맛집 지도 · 상세 · 수정 · 미확인 정리 · 직접 검색
feature/share           맛집 지도 공유 (링크 · QR) · 받은 목록 가져오기
feature/settings        설정
tools/screenshots       화면 렌더 검사 (Paparazzi, 에뮬레이터 없이)
web/nextplace/s         공유 링크를 폰에서 열 때 보이는 웹 목록 (정적 페이지)
```

기능끼리는 서로 import하지 않고 `core/domain`의 계약으로만 연결합니다. 이 규칙은 `app/src/test/.../ArchitectureTest.kt`(Konsist)가 검사합니다.

## 검사

| 검사 | 명령 |
| --- | --- |
| 단위 · 구조 · 리소스 이름 테스트 | `./gradlew testDebugUnitTest` |
| 화면 렌더 (다크 31장 + 라이트 7장) | `./gradlew -Pnextplace.screens=true :tools:screenshots:recordPaparazziDebug` → `tools/screenshots/src/test/snapshots/images/` |
| Lint | `./gradlew :app:lintDebug` |

## 공유 링크 웹 페이지

공유 링크는 `https://easycd.github.io/nextplace/s#<데이터>` 형식이고, 목록은 링크 안에 압축해 담습니다(서버 없음).
폰에서 열리는 페이지는 `web/nextplace/s/index.html`입니다. GitHub에 `nextplace` 저장소를 만들고 이 폴더의 `s/`를 올린 뒤 Pages를 켜면 링크가 동작합니다.
