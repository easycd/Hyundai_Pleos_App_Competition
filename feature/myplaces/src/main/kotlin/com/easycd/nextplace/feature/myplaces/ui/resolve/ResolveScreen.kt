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

/** S-09 미확인 가게 정리: 그때 상황 · 후보 5곳 · 지도 · 이걸로 저장 · 직접 검색. 삭제는 상단 오른쪽. */
@Composable
fun ResolveScreen(state: ResolveUiState, onAction: (ResolveAction) -> Unit) {
    val portrait = LocalPortrait.current
    Box {
        ScreenFrame(
            stringResource(R.string.resolve_title),
            onBack = { onAction(ResolveAction.Back) },
            action = { PillButton(stringResource(R.string.detail_delete), { onAction(ResolveAction.Delete) }, kind = PillKind.DANGER_OUTLINE, icon = NpIcons.Trash) },
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp)) {
                Column(Modifier.weight(1f)) {
                    Context(state)
                    Spacer(Modifier.height(16.dp))
                    Candidates(state, onAction, Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        PillButton(stringResource(R.string.resolve_search), { onAction(ResolveAction.Search) }, Modifier.weight(1f), icon = NpIcons.Search)
                        PillButton(
                            stringResource(R.string.resolve_save),
                            { onAction(ResolveAction.Save) },
                            Modifier.weight(1f),
                            kind = PillKind.PRIMARY,
                            enabled = state.candidates.isNotEmpty(),
                        )
                    }
                }
                if (!portrait) {
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.weight(1f).fillMaxSize().padding(bottom = 24.dp)) {
                        val pins = state.candidates.mapIndexed { i, c -> MapPin("c$i", GeoPoint(c.lat, c.lon), PinTone.CANDIDATE, i == state.selected) } +
                            listOfNotNull(state.estimated?.let { MapPin("est", it, PinTone.ESTIMATED) })
                        PinMap(pins, null, Modifier.fillMaxSize(), onPin = { pid -> pid.removePrefix("c").toIntOrNull()?.let { onAction(ResolveAction.Pick(it)) } })
                    }
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
            Text(stringResource(R.string.resolve_context), style = NpType.small, color = Np.colors.muted)
            val where = if (s.noLocation) stringResource(R.string.resolve_no_location) else s.where
            Text(listOfNotNull(TimeText.saved(s.savedAt), where, s.heading?.let { stringResource(R.string.resolve_heading, it) }).joinToString(" · "), style = NpType.body, color = Np.colors.text)
            s.utterance?.let { Text("“$it”", style = NpType.body, color = Np.colors.muted, maxLines = 2) }
        }
    }
}

@Composable
private fun Candidates(s: ResolveUiState, onAction: (ResolveAction) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(stringResource(R.string.resolve_candidates), style = NpType.small, color = Np.colors.muted)
        when {
            s.searching -> Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                SpinnerRing(size = 48.dp)
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.resolve_searching), style = NpType.body, color = Np.colors.muted)
            }
            s.candidates.isEmpty() -> Text(stringResource(R.string.resolve_no_candidates), style = NpType.body, color = Np.colors.muted, modifier = Modifier.padding(24.dp))
            else -> LazyColumn {
                itemsIndexed(s.candidates) { i, c -> CandidateRow(c, i == s.selected) { onAction(ResolveAction.Pick(i)) } }
            }
        }
    }
}
