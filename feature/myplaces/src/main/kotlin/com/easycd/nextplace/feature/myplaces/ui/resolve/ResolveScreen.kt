package com.easycd.nextplace.feature.myplaces.ui.resolve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.component.ConfirmDialog
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.PinMap
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.SpinnerRing
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R
import com.easycd.nextplace.feature.myplaces.ui.common.CandidateRow

/**
 * S-09 어느 가게였나요?: 왼쪽 = 그때 상황 카드(시각 · 도로(방향) · 말한 문장) + 그때 위치 지도,
 * 오른쪽 = 후보 5곳(라디오) + 이걸로 저장 · 직접 검색. 삭제는 상단 오른쪽(S-08 수정 버튼 자리).
 */
@Composable
fun ResolveScreen(state: ResolveUiState, onAction: (ResolveAction) -> Unit) {
    val portrait = LocalPortrait.current
    Box {
        ScreenFrame(
            stringResource(R.string.resolve_title),
            onBack = { onAction(ResolveAction.Back) },
            action = { PillButton(stringResource(R.string.detail_delete), { onAction(ResolveAction.Delete) }, kind = PillKind.DANGER_OUTLINE, icon = NpIcons.Trash) },
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(bottom = 32.dp)) {
                if (!portrait) {
                    Column(Modifier.weight(1f)) {
                        Context(state)
                        Spacer(Modifier.height(24.dp))
                        GlassCard(Modifier.fillMaxWidth().weight(1f), padding = 24.dp) {
                            val pins = state.candidates.mapIndexed { i, c -> MapPin("c$i", GeoPoint(c.lat, c.lon), PinTone.CANDIDATE, i == state.selected) } +
                                listOfNotNull(state.estimated?.let { MapPin("est", it, PinTone.ESTIMATED) })
                            PinMap(pins, null, Modifier.fillMaxSize(), onPin = { pid -> pid.removePrefix("c").toIntOrNull()?.let { onAction(ResolveAction.Pick(it)) } })
                            Text(stringResource(R.string.resolve_map), style = NpType.small, color = Np.colors.muted, modifier = Modifier.align(Alignment.TopStart))
                        }
                    }
                    Spacer(Modifier.width(40.dp))
                }
                Column(Modifier.weight(1f)) {
                    if (portrait) {
                        Context(state)
                        Spacer(Modifier.height(16.dp))
                    }
                    Candidates(state, onAction, Modifier.weight(1f))
                    Spacer(Modifier.height(24.dp))
                    PillButton(
                        stringResource(R.string.resolve_save),
                        { onAction(ResolveAction.Save) },
                        Modifier.fillMaxWidth(),
                        kind = PillKind.PRIMARY,
                        icon = NpIcons.Check,
                        enabled = state.candidates.isNotEmpty(),
                        height = 112.dp,
                    )
                    Spacer(Modifier.height(20.dp))
                    PillButton(stringResource(R.string.resolve_search), { onAction(ResolveAction.Search) }, Modifier.fillMaxWidth(), icon = NpIcons.Search)
                }
            }
        }
        ConfirmDialog(
            visible = state.confirmDelete,
            question = stringResource(R.string.delete_q, com.easycd.nextplace.core.domain.voice.Josa.eulReul(state.title.ifBlank { stringResource(R.string.mp_unknown) })),
            description = stringResource(R.string.delete_desc),
            confirmText = stringResource(R.string.delete_ok),
            cancelText = stringResource(R.string.cancel),
            onConfirm = { onAction(ResolveAction.ConfirmDelete) },
            onCancel = { onAction(ResolveAction.CancelDelete) },
            danger = true,
        )
    }
}

@Composable
private fun Context(s: ResolveUiState) {
    GlassCard(Modifier.fillMaxWidth(), padding = 28.dp) {
        Column {
            val where = if (s.noLocation) stringResource(R.string.resolve_no_location) else s.where
            val moving = s.heading?.let { stringResource(R.string.resolve_moving, it) }
            val place = listOfNotNull(where, moving?.let { "($it)" }).joinToString(" ")
            Text(listOf(TimeText.saved(s.savedAt), place).filter { it.isNotBlank() }.joinToString(" · "), style = NpType.small, color = Np.colors.muted)
            s.utterance?.let {
                Spacer(Modifier.height(8.dp))
                Text("“$it”", style = NpType.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = Np.colors.text, maxLines = 3)
            }
        }
    }
}

@Composable
private fun Candidates(s: ResolveUiState, onAction: (ResolveAction) -> Unit, modifier: Modifier) {
    Column(modifier) {
        when {
            s.searching -> Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                SpinnerRing(size = 48.dp)
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.resolve_searching), style = NpType.body, color = Np.colors.muted)
            }
            s.candidates.isEmpty() -> Text(stringResource(R.string.resolve_no_candidates), style = NpType.body, color = Np.colors.muted, modifier = Modifier.padding(24.dp))
            else -> LazyColumn {
                itemsIndexed(s.candidates) { i, c -> CandidateRow(c, i == s.selected, showAddress = false) { onAction(ResolveAction.Pick(i)) } }
            }
        }
    }
}
