package com.easycd.nextplace.core.domain

/**
 * 임계값은 이 한 곳에만 둔다 (기술서 4장 · R8). 단위는 이름 끝에 붙인다.
 * 값의 근거: 기획서 4·6·8장, 기술서 7장.
 */
object Tunables {
    // 위치 버퍼 (F-02)
    const val BUFFER_CAPACITY = 120
    const val BUFFER_WINDOW_MS = 60_000L
    const val BUFFER_GAP_MS = 5_000L
    const val LOCATION_STALE_MS = 3_000L
    const val LOCATE_TOLERANCE_MS = 5_000L

    // 시점 (기획서 6장)
    const val LOOKBACK_DEFAULT_SEC = 20
    val LOOKBACK_CHOICES_SEC = listOf(10, 20, 30)
    const val EARLIER_SEC = 45
    const val GLEO_EXTRA_WINDOW_MS = 5_000L

    // 듣기 (S-02)
    const val LISTEN_SILENCE_MS = 1_500L
    const val LISTEN_MAX_MS = 15_000L
    const val LISTEN_RETRY_DELAY_MS = 1_000L
    const val UNDERSTAND_TIMEOUT_MS = 3_000L

    // 가게 찾기 (F-04)
    const val POI_RADIUS_M = 150
    const val POI_MAX_CANDIDATES = 15
    const val POI_TIMEOUT_MS = 1_200L
    const val KEEP_CANDIDATES = 5
    const val SCORE_CATEGORY = 40.0
    const val SCORE_NAME = 30.0
    const val SCORE_DISTANCE = 20.0
    const val SCORE_SIDE = 10.0
    const val SCORE_SIDE_UNKNOWN = 5.0
    const val SCORE_MIN_TOP = 40.0
    const val SCORE_CERTAIN_GAP = 30.0
    const val PREFETCH_DEBOUNCE_MS = 300L

    // 확인 대화 (F-05)
    const val CONFIRM_WAIT_MS = 5_000L
    const val CONFIRM_MAX_ASKS = 2
    const val TTS_NAME_MAX = 12

    // 저장 (F-06)
    const val DB_RETRY_DELAYS_MS = 200L
    val DB_RETRY_BACKOFF_MS = listOf(200L, 500L, 1_000L)
    const val DRAFT_STALE_MS = 60_000L
    const val UNDO_DELETE_MS = 4_000L
    const val TOAST_MS = 4_000L

    // 리마인드 (F-08)
    val REMIND_RADIUS_CHOICES_M = listOf(300, 500, 1_000)
    const val REMIND_RADIUS_DEFAULT_M = 500
    const val REMIND_EXIT_MARGIN_M = 100
    const val REMIND_RECHECK_MS = 60_000L
    const val REMIND_GLOBAL_GAP_MS = 10 * 60_000L
    const val REMIND_VOICE_WAIT_MS = 5_000L
    const val REMIND_BUTTON_WAIT_MS = 10_000L
    const val REMIND_QUEUE_MAX_WAIT_MS = 30_000L
    const val AHEAD_HALF_ANGLE_DEG = 45.0
    val MEAL_HOURS = listOf(11 until 14, 17 until 20)

    // 추천 (F-09)
    const val RECOMMEND_RADIUS_M = 10_000
    const val RECOMMEND_WIDE_RADIUS_M = 30_000
    const val RECOMMEND_PAGE_SIZE = 3
    const val RECOMMEND_MAX_PAGES = 3
    const val RECOMMEND_IDLE_CLOSE_MS = 120_000L
    const val RECOMMEND_SEND_DELAY_MS = 1_500L
    const val RECOMMEND_W_DISTANCE = 50.0
    const val RECOMMEND_W_MEMO = 25.0
    const val RECOMMEND_W_LAST_VISIT = 25.0
    const val RECOMMEND_VISIT_FULL_DAYS = 30.0

    // 방문 (F-11)
    const val VISIT_ROUTING_EXPIRE_MS = 3 * 60 * 60_000L
    const val VISIT_ARRIVE_RADIUS_M = 50
    const val VISIT_STAY_RADIUS_M = 100
    const val VISIT_STAY_MS = 10 * 60_000L
    const val VISIT_GAP_TOLERANCE_MS = 2 * 60_000L
    const val VISIT_OUT_RESET_MS = 30_000L
    const val VISIT_LEFT_MS = 60_000L
    const val VISIT_SAME_WINDOW_MS = 2 * 60 * 60_000L
    const val NOTE_RECENT_WINDOW_MS = 3 * 60 * 60_000L

    // 홈 도로명 (S-01)
    const val ROAD_NAME_REFRESH_MS = 30_000L

    // 공유 (F-13)
    const val SHARE_MAX_PLACES = 30
    const val SHARE_TEXT_MAX = 40
    const val SHARE_MAX_BYTES = 8 * 1024
    const val SHARE_QR_MAX_CHARS = 2_000
    const val SAME_PLACE_DISTANCE_M = 50.0

    // 직접 검색 (S-09b)
    const val SEARCH_DEBOUNCE_MS = 300L
    const val SEARCH_RADIUS_M = 2_000

    // 격자 (기술서 9장)
    const val GRID_CELLS_PER_DEGREE = 100
    const val METERS_PER_DEGREE_LAT = 111_320.0

    // SDK 재연결 (기술서 10장)
    val SDK_RECONNECT_DELAYS_MS = listOf(2_000L, 4_000L, 8_000L)
}
