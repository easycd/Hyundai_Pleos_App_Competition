package com.easycd.nextplace.feature.myplaces.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.contract.ShareCart
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.usecase.RouteStarter
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.myplaces.usecase.DeletePlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val places: PlaceRepository,
    settings: SettingsRepository,
    location: LocationSource,
    private val cart: ShareCart,
    private val routes: RouteStarter,
    private val delete: DeletePlaceUseCase,
    private val permissions: PermissionChecker,
    private val tts: SpeechSynthesizer,
    private val navigator: Navigator,
    private val messages: Messages,
) : ViewModel() {
    private val id: String = checkNotNull(handle[Routes.ARG_ID])
    private val dialog = MutableStateFlow(false)

    val state: StateFlow<PlaceDetailUiState> = combine(
        places.observe(id), settings.settings, location.latest, cart.ids, dialog,
    ) { pw, s, here, ids, confirm ->
        val p = pw?.place ?: return@combine PlaceDetailUiState(loaded = true)
        PlaceDetailUiState(
            loaded = true,
            id = p.id,
            name = p.name ?: p.displayName.orEmpty(),
            category = p.category,
            distanceM = here?.let { h -> p.point?.let { Geo.distanceM(h.point, it) } },
            address = p.address,
            savedAt = p.savedAt,
            visits = pw.visitCount,
            memo = p.memo,
            visitNotes = pw.visitNotes,
            utterance = p.utterance,
            point = p.point,
            remindEnabled = p.remindEnabled,
            globalRemind = s.remindEnabled,
            inShareCart = p.id in ids,
            confirmDelete = confirm,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_MS), PlaceDetailUiState())

    fun onAction(a: PlaceDetailAction) {
        when (a) {
            PlaceDetailAction.Route -> route()
            PlaceDetailAction.Edit -> navigator.go(Destination.Edit(id))
            PlaceDetailAction.Delete -> dialog.value = true
            PlaceDetailAction.CancelDelete -> dialog.value = false
            PlaceDetailAction.ConfirmDelete -> {
                dialog.value = false
                delete.delete(id, state.value.name)
                navigator.go(Destination.Back)
            }
            PlaceDetailAction.AddToShare -> viewModelScope.launch {
                cart.add(id)
                delay(SHARE_JUMP_MS)
                navigator.go(Destination.Share(id))
            }
            is PlaceDetailAction.Remind -> viewModelScope.launch {
                places.get(id)?.let { places.update(it.copy(remindEnabled = a.on)) }
            }
            PlaceDetailAction.Back -> navigator.go(Destination.Back)
        }
    }

    /** 안내 시작 (S-05b 와 같음): Pleos 내비에 목적지만 넘기고 앱은 길을 그리지 않는다. */
    private fun route() {
        if (!permissions.hasNavi()) return navigator.go(Destination.PermissionNeeded(PermissionKind.NAVI))
        viewModelScope.launch {
            val p = places.get(id) ?: return@launch
            val name = p.name ?: return@launch
            val changing = routes.hasDestination()
            messages.show(UserMessage.Routing(name, changing))
            launch { tts.speak(if (changing) VoicePrompt.RouteChange(name) else VoicePrompt.RouteStart(name)) }
            if (!routes.send(p)) {
                messages.show(UserMessage.RouteFailed)
                tts.speak(VoicePrompt.RouteFailed)
            }
        }
    }

    private companion object {
        const val STOP_MS = 5_000L
        const val SHARE_JUMP_MS = 400L
    }
}
