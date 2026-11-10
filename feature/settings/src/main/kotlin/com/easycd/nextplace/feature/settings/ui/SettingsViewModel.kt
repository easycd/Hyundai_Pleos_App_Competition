package com.easycd.nextplace.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.DataResetHook
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.ShareCart
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 설정은 DataStore 하나. 리마인드·반경을 바꾸면 RemindController 가 감시를 바로 다시 등록한다. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val places: PlaceRepository,
    private val tts: SpeechSynthesizer,
    private val navigator: Navigator,
    private val cart: ShareCart,
    private val resetHooks: Set<@JvmSuppressWildcards DataResetHook>,
) : ViewModel() {
    private val dialog = MutableStateFlow(false)

    val state: StateFlow<SettingsUiState> = combine(repo.settings, places.observeCounts(), dialog) { s, c, d ->
        SettingsUiState(s, c.total, d, volumeAdjustable = tts.supportsVolume)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_MS), SettingsUiState())

    fun onAction(a: SettingsAction) {
        when (a) {
            is SettingsAction.Remind -> update { it.copy(remindEnabled = a.on) }
            is SettingsAction.Radius -> update { it.copy(remindRadiusM = a.meters) }
            is SettingsAction.MealOnly -> update { it.copy(mealTimeOnly = a.on) }
            is SettingsAction.Lookback -> update { it.copy(lookbackSec = a.seconds) }
            is SettingsAction.Volume -> {
                tts.setVolume(a.value)
                update { it.copy(voiceVolume = a.value) }
            }
            SettingsAction.DeleteAll -> dialog.value = true
            SettingsAction.CancelDeleteAll -> dialog.value = false
            SettingsAction.ConfirmDeleteAll -> viewModelScope.launch {
                dialog.value = false
                places.deleteEverything()
                // DB 밖 상태도 처음처럼: 마지막 알림 시각(새 가게가 10분 제한에 걸리지 않게) · 공유 장바구니 · 예약 작업 · 캐시
                repo.update { it.copy(lastRemindAt = null) }
                cart.clear()
                resetHooks.forEach { runCatching { it.onAllDataDeleted() } }
                navigator.go(Destination.Home)
            }
            SettingsAction.Back -> navigator.go(Destination.Back)
        }
    }

    private fun update(f: (com.easycd.nextplace.core.domain.model.AppSettings) -> com.easycd.nextplace.core.domain.model.AppSettings) {
        viewModelScope.launch { repo.update(f) }
    }

    private companion object {
        const val STOP_MS = 5_000L
    }
}
