package com.easycd.nextplace.core.domain.sdk

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** SDK 연결 상태 (기술서 10장: 재연결 3회 뒤 UNAVAILABLE). */
enum class SdkStatus { CONNECTING, READY, UNAVAILABLE }

/** STT 이벤트. SDK 콜백은 래퍼에서 Flow 로 바뀐다 (R7). */
sealed interface SttEvent {
    data object Started : SttEvent
    data class Partial(val text: String) : SttEvent
    data class Final(val text: String) : SttEvent
    data class Level(val level: Float) : SttEvent
    data class Failed(val reason: SttFailure) : SttEvent
}

enum class SttFailure { NO_SPEECH, FOCUS_LOST, PERMISSION, UNAVAILABLE, OTHER }

/** 음성 인식 (SpeechToText SDK). [listen]을 모으는 동안만 마이크가 켜지고, 취소하면 stop 된다. */
interface SpeechRecognizer {
    val status: StateFlow<SdkStatus>
    fun listen(): Flow<SttEvent>
}

/** 음성 안내 (TextToSpeech SDK). [speak]는 다 읽은 뒤(또는 실패 시) 돌아온다. */
interface SpeechSynthesizer {
    val status: StateFlow<SdkStatus>
    val speaking: StateFlow<Boolean>
    suspend fun speak(prompt: VoicePrompt): Boolean
    fun stop()
    fun setVolume(volume: Float)
}

/** 위치를 받는 이유. 집합이 비어 있지 않은 동안 앱이 뒤에 있어도 위치를 받는다 (기술서 7-4). */
enum class LocationReason { FOREGROUND, REMIND, VISIT }

/** 위치 (FusedLocation SDK) + 위치 버퍼. */
interface LocationSource {
    val status: StateFlow<SdkStatus>
    val latest: StateFlow<LocationSample?>
    val samples: Flow<LocationSample>
    fun hasPermission(): Boolean
    /** 버튼 누른 순간을 '말한 곳'으로 고정한다 (Capture). */
    fun capture(source: PlaceSource = PlaceSource.VOICE): LocationSnapshot
    fun acquire(reason: LocationReason)
    fun release(reason: LocationReason)
    /** 개발 빌드의 가짜 위치 재생 중인가 (PT: 화면 구석에 표시). */
    val isSimulated: Boolean
}

/** 내비 목적지 (좌표·이름·주소만, 카카오 ID는 넘기지 않음 · F-10). */
data class RouteTarget(val point: GeoPoint, val name: String, val address: String?)

sealed interface NaviEvent {
    data class Arrived(val name: String?) : NaviEvent
    data object RouteEnded : NaviEvent
    data object RouteCancelled : NaviEvent
}

/** NaviHelper SDK 래퍼. 길안내는 Pleos 내비가 하고 앱은 목적지만 넘긴다. */
interface NaviController {
    val status: StateFlow<SdkStatus>
    /** 안내 중(목적지 있음)인가. 리마인드 필터 3. */
    val routeActive: StateFlow<Boolean>
    /** 내비가 알려 준 현재 도로명 (없으면 null). */
    val roadName: StateFlow<String?>
    val events: Flow<NaviEvent>
    suspend fun currentDestination(): RouteTarget?
    suspend fun requestRoute(target: RouteTarget): Boolean
}

/** 주행 제한 (CarUxRestrictions, F-14). null = 아직 모름 → 제한으로 본다. */
interface UxRestrictionSource {
    val restricted: StateFlow<Boolean?>
}

/** 런타임 권한 상태. */
interface PermissionChecker {
    fun hasMicrophone(): Boolean
    fun hasLocation(): Boolean
    fun hasNavi(): Boolean
    fun hasNotification(): Boolean
}

/** 시계. 날짜 경계는 차량 시간 0시 (기술서 7-2). */
interface Clock {
    fun now(): Long
    fun hourOfDay(atMs: Long = now()): Int
    fun dayIndex(atMs: Long): Long
}

/** 개발 빌드의 가짜 음성 입력 (가짜 SDK 일 때만 enabled). 화면에 입력 칸을 띄운다. */
interface DevVoiceInput {
    val enabled: Boolean
    val listening: StateFlow<Boolean>
    fun submit(text: String)
}
