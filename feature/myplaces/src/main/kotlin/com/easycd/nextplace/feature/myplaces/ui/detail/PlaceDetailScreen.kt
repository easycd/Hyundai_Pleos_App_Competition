package com.easycd.nextplace.feature.myplaces.ui.detail

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import com.easycd.nextplace.core.ui.component.ChipTone
import com.easycd.nextplace.core.ui.component.StatusChip
import com.easycd.nextplace.core.ui.component.RoundIconButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.ui.component.ConfirmDialog
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.PinMap
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R

/**
 * S-08 가게 상세 + S-08c 삭제 확인. 왼쪽 정보(업종·거리 / 주소 / 저장일·방문 칩 / 메모 카드 / 저장할 때 한 말 / 근처 알림),
 * 오른쪽 위치 지도, 아래 줄 안내 시작 · 공유에 담기 … (떨어뜨린) 삭제. 수정은 상단 오른쪽 둥근 버튼.
 */
@Composable
fun PlaceDetailScreen(state: PlaceDetailUiState, onAction: (PlaceDetailAction) -> Unit) {
    val portrait = LocalPortrait.current
    Box {
        ScreenFrame(
            state.name,
            onBack = { onAction(PlaceDetailAction.Back) },
            action = { RoundIconButton(NpIcons.Pencil, stringResource(R.string.detail_edit), { onAction(PlaceDetailAction.Edit) }, size = 80.dp) },
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 40.dp)) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { Info(state, onAction) }
                    if (!portrait) {
                        Spacer(Modifier.width(40.dp))
                        Box(Modifier.width(MAP_W.dp).fillMaxHeight().padding(bottom = 24.dp)) {
                            PinMap(listOfNotNull(state.point?.let { MapPin(state.id, it, if (state.visits > 0) PinTone.VISITED else PinTone.SAVED, true) }), null, Modifier.fillMaxSize())
                        }
                    }
                }
                Actions(state, onAction)
                Spacer(Modifier.height(32.dp))
            }
        }
        ConfirmDialog(
            visible = state.confirmDelete,
            question = stringResource(R.string.delete_q, Josa.eulReul(state.name)),
            description = stringResource(R.string.delete_desc),
            confirmText = stringResource(R.string.delete_ok),
            cancelText = stringResource(R.string.cancel),
            onConfirm = { onAction(PlaceDetailAction.ConfirmDelete) },
            onCancel = { onAction(PlaceDetailAction.CancelDelete) },
            danger = true,
        )
    }
}

@Composable
private fun Info(s: PlaceDetailUiState, onAction: (PlaceDetailAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(listOfNotNull(s.category, s.distanceM?.let(DistanceText::of)).joinToString(" · ").ifEmpty { s.name }, style = NpType.title, color = Np.colors.text)
        s.address?.let { Text(it, style = NpType.body, color = Np.colors.muted) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusChip(stringResource(R.string.detail_saved_chip, TimeText.saved(s.savedAt)), ChipTone.SAVED)
            if (s.visits > 0) StatusChip(stringResource(R.string.detail_visits_chip, s.visits), ChipTone.VISITED)
        }
        if (s.memo != null || s.visitNotes.isNotEmpty()) {
            GlassCard(Modifier.fillMaxWidth(), padding = 28.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (s.memo != null) {
                        Text(stringResource(R.string.detail_memo), style = NpType.small, color = Np.colors.muted)
                        Text(s.memo, style = NpType.cardTitle.copy(fontSize = NpType.title.fontSize), color = Np.colors.text)
                    }
                    s.visitNotes.forEach { note ->
                        Text("${stringResource(R.string.detail_visit_notes)} · “$note”", style = NpType.small, color = Np.colors.muted)
                    }
                }
            }
        }
        s.utterance?.let { Text("${stringResource(R.string.detail_utterance)} · “$it”", style = NpType.small, color = Np.colors.muted) }
        Row(Modifier.heightIn(min = 88.dp).alpha(if (s.globalRemind) 1f else DIM), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.detail_remind), style = NpType.body, color = Np.colors.text)
            Spacer(Modifier.width(24.dp))
            Switch(
                checked = s.remindEnabled,
                onCheckedChange = { onAction(PlaceDetailAction.Remind(it)) },
                enabled = s.globalRemind,
                colors = SwitchDefaults.colors(checkedTrackColor = Np.colors.brand, checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent),
                modifier = Modifier.scale(SWITCH_SCALE),
            )
            if (!s.globalRemind) {
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.detail_remind_off_global), style = NpType.small, color = Np.colors.muted)
            }
        }
    }
}

@Composable
private fun Actions(s: PlaceDetailUiState, onAction: (PlaceDetailAction) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PillButton(
            stringResource(R.string.detail_route),
            { onAction(PlaceDetailAction.Route) },
            Modifier.widthIn(min = 400.dp),
            kind = PillKind.PRIMARY,
            icon = NpIcons.Navigation,
            enabled = s.point != null,
            height = 112.dp,
        )
        Spacer(Modifier.width(24.dp))
        AnimatedContent(s.inShareCart, transitionSpec = { fadeIn(tween(Motion.SCRIM_MS)).togetherWith(fadeOut(tween(Motion.SCRIM_MS))) }, label = "cart") { added ->
            PillButton(
                stringResource(if (added) R.string.detail_added_share else R.string.detail_add_share),
                { onAction(PlaceDetailAction.AddToShare) },
                Modifier.widthIn(min = 380.dp),
                icon = if (added) NpIcons.Check else NpIcons.Share,
                height = 112.dp,
            )
        }
        Spacer(Modifier.weight(1f))
        PillButton(stringResource(R.string.detail_delete), { onAction(PlaceDetailAction.Delete) }, kind = PillKind.DANGER_OUTLINE, icon = NpIcons.Trash, height = 112.dp)
    }
}

private const val DIM = 0.4f
private const val MAP_W = 480
private const val SWITCH_SCALE = 1.5f
