package com.easycd.nextplace.feature.share.ui.send

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.ShareCart
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.model.ShareList
import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.ShareRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.share.R
import com.easycd.nextplace.feature.share.codec.ShareCodec
import com.easycd.nextplace.feature.share.codec.SharePayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/** 고를 때마다 링크·QR 을 다시 만든다. 미확인 가게는 목록에 없음, 최대 30곳. */
@HiltViewModel
class ShareViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val app: Application,
    private val places: PlaceRepository,
    private val shares: ShareRepository,
    private val cart: ShareCart,
    private val navigator: Navigator,
    private val clock: Clock,
) : ViewModel() {
    private val pre: String? = handle[Routes.ARG_PRE]
    private var source: Map<String, PlaceWithVisits> = emptyMap()
    private val _state = MutableStateFlow(ShareUiState())
    val state: StateFlow<ShareUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val all = places.allConfirmed().sortedByDescending { it.place.savedAt }
            source = all.associateBy { it.place.id }
            val chosen = cart.ids.value + listOfNotNull(pre)
            val rows = all.map { ShareRowModel(it.place.id, it.place.name.orEmpty(), it.place.category, it.place.id in chosen) }
            _state.value = ShareUiState(loaded = true, rows = limit(rows))
            rebuild()
        }
    }

    fun onAction(a: ShareAction) {
        when (a) {
            is ShareAction.Toggle -> {
                _state.update { s ->
                    val rows = s.rows.map { if (it.id == a.id) it.copy(checked = !it.checked) else it }
                    if (rows.count { it.checked } > Tunables.SHARE_MAX_PLACES) s else s.copy(rows = rows)
                }
                rebuild()
            }
            ShareAction.ToggleAll -> {
                _state.update { s ->
                    val all = s.rows.isNotEmpty() && s.rows.all { it.checked }
                    s.copy(rows = limit(s.rows.map { it.copy(checked = !all) }))
                }
                rebuild()
            }
            is ShareAction.Title -> {
                _state.update { it.copy(title = a.text.take(Tunables.SHARE_TEXT_MAX), titleEdited = true) }
                rebuild()
            }
            ShareAction.Copy -> viewModelScope.launch {
                record()
                _state.update { it.copy(copied = true) }
                delay(COPIED_MS)
                _state.update { it.copy(copied = false) }
            }
            ShareAction.Send -> viewModelScope.launch { record() }
            ShareAction.Back -> {
                cart.clear()
                navigator.go(Destination.Back)
            }
        }
    }

    private fun limit(rows: List<ShareRowModel>): List<ShareRowModel> {
        var n = 0
        return rows.map { if (it.checked && ++n > Tunables.SHARE_MAX_PLACES) it.copy(checked = false) else it }
    }

    private fun rebuild() {
        val s = _state.value
        val chosen = s.rows.filter { it.checked }.mapNotNull { source[it.id]?.place }
        val title = if (s.titleEdited) s.title else app.getString(R.string.share_default_title, chosen.size)
        if (chosen.isEmpty()) return _state.update { it.copy(title = title, link = null, qr = null) }
        val payload = SharePayload(title, clock.now(), chosen.map { SharedPlace(it.name.orEmpty(), it.category, it.address, it.lat!!, it.lon!!, it.memo, it.provider, it.providerId) })
        val link = ShareCodec.link(payload)
        _state.update { it.copy(title = title, link = link) }
        viewModelScope.launch {
            val qr = if (link.length <= Tunables.SHARE_QR_MAX_CHARS) withContext(Dispatchers.Default) { QrBitmap.of(link) } else null
            if (_state.value.link == link) _state.update { it.copy(qr = qr) }
        }
    }

    private suspend fun record() {
        val s = _state.value
        val chosen = s.rows.filter { it.checked }.mapNotNull { source[it.id]?.place }
        if (chosen.isEmpty()) return
        shares.save(ShareList(UUID.randomUUID().toString(), s.title, chosen.map { SharedPlace(it.name.orEmpty(), it.category, it.address, it.lat!!, it.lon!!, it.memo, it.provider, it.providerId) }, clock.now()))
    }

    private companion object {
        const val COPIED_MS = 1_500L
    }
}
