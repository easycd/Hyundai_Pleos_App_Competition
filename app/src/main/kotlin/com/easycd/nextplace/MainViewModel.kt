package com.easycd.nextplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.UxRestrictionSource
import com.easycd.nextplace.core.pleos.location.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 앱 셸 단계: S-00 시작 → (첫 실행) S-13 → 홈. */
enum class ShellPhase { SPLASH, ONBOARDING, MAIN }

data class ShellState(
    val phase: ShellPhase = ShellPhase.SPLASH,
    /** S-17 을 띄울 권한 (null = 닫힘). */
    val denied: PermissionKind? = null,
    /** S-17 '위치만 저장' 을 보일까: 마이크·음성 서비스 문제이고 위치는 쓸 수 있을 때만. */
    val canSaveLocationOnly: Boolean = false,
)

/**
 * 앱 셸 상태 (MainActivity 1개 창). 업무 로직은 없고 feature 계약(SaveRunner)과 통로(Navigator·Messages)만 잇는다.
 * S-00 애니메이션(1.2초) 동안 첫 실행 여부를 읽어 둔다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val navigator: Navigator,
    val messages: Messages,
    val ux: UxRestrictionSource,
    private val settings: SettingsRepository,
    private val permissions: PermissionChecker,
    private val tracker: LocationTracker,
    private val saveRunner: SaveRunner,
) : ViewModel() {
    private val _state = MutableStateFlow(ShellState())
    val state: StateFlow<ShellState> = _state.asStateFlow()
    private val onboardingDone = viewModelScope.async { runCatching { settings.settings.first().onboardingDone }.getOrDefault(true) }

    fun onSplashDone() {
        viewModelScope.launch {
            val next = if (onboardingDone.await()) ShellPhase.MAIN else ShellPhase.ONBOARDING
            _state.update { it.copy(phase = next) }
        }
    }

    /** S-13 시작하기(권한 창이 다 끝난 뒤) · 나중에: 둘 다 첫 실행 안내를 다시 보이지 않는다. */
    fun onOnboardingDone() {
        viewModelScope.launch { settings.update { it.copy(onboardingDone = true) } }
        tracker.retry()
        _state.update { it.copy(phase = ShellPhase.MAIN) }
    }

    fun speak() = saveRunner.startVoiceSave()

    fun onPermissionNeeded(kind: PermissionKind) {
        val voiceProblem = kind == PermissionKind.MICROPHONE || kind == PermissionKind.VOICE_SERVICE
        _state.update { it.copy(denied = kind, canSaveLocationOnly = voiceProblem && permissions.hasLocation()) }
    }

    fun closeDenied() = _state.update { it.copy(denied = null) }

    fun saveLocationOnly() {
        closeDenied()
        viewModelScope.launch { saveRunner.saveLocationOnly() }
    }

    /** 설정 앱에서 돌아오면 위치 수신을 다시 시도한다 (권한을 켰을 수 있음). */
    fun onResume() {
        if (permissions.hasLocation()) tracker.retry()
    }
}
