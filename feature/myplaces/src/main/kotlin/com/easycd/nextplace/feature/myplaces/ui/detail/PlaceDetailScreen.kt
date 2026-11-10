package com.easycd.nextplace.feature.myplaces.ui.detail

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

/** S-08 가게 상세 + S-08c 삭제 확인. 삭제는 다른 버튼과 떨어뜨린다. */
@Composable
fun PlaceDetailScreen(state: PlaceDetailUiState, onAction: (PlaceDetailAction) -> Unit) {
    val portrait = LocalPortrait.current
    Box {
        ScreenFrame(
            state.name,
            onBack = { onAction(PlaceDetailAction.Back) },
            action = { PillButton(stringResource(R.string.detail_edit), { onAction(PlaceDetailAction.Edit) }, icon = NpIcons.Pencil) },
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Info(state)
                    Spacer(Modifier.height(24.dp))
                    Actions(state, onAction)
                    Spacer(Modifier.height(48.dp))
                    PillButton(stringResource(R.string.detail_delete), { onAction(PlaceDetailAction.Delete) }, kind = PillKind.DANGER_OUTLINE, icon = NpIcons.Trash)
                    Spacer(Modifier.height(32.dp))
                }
                if (!portrait) {
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.weight(1f).fillMaxSize().padding(bottom = 24.dp)) {
                        PinMap(listOfNotNull(state.point?.let { MapPin(state.id, it, if (state.visits > 0) PinTone.VISITED else PinTone.SAVED, true) }), null, Modifier.fillMaxSize())
                    }
                }
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
private fun Info(s: PlaceDetailUiState) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(listOfNotNull(s.category, s.distanceM?.let(DistanceText::of)).joinToString(" · "), style = NpType.body, color = Np.colors.text)
            Line(stringResource(R.string.detail_address), s.address)
            Line(stringResource(R.string.detail_saved), TimeText.date(s.savedAt))
            Line(stringResource(R.string.detail_visits), stringResource(R.string.detail_visits_value, s.visits))
            Line(stringResource(R.string.detail_memo), s.memo)
            Line(stringResource(R.string.detail_visit_notes), s.visitNotes.takeIf { it.isNotEmpty() }?.joinToString("\n") { "· $it" })
            Line(stringResource(R.string.detail_utterance), s.utterance?.let { "“$it”" })
        }
    }
}

@Composable
private fun Line(label: String, value: String?) {
    Row {
        Text(label, style = NpType.small, color = Np.colors.muted, modifier = Modifier.width(180.dp))
        Text(value ?: stringResource(R.string.detail_none), style = NpType.body, color = if (value == null) Np.colors.muted else Np.colors.text)
    }
}

@Composable
private fun Actions(s: PlaceDetailUiState, onAction: (PlaceDetailAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PillButton(
            stringResource(R.string.detail_route),
            { onAction(PlaceDetailAction.Route) },
            Modifier.fillMaxWidth(),
            kind = PillKind.PRIMARY,
            icon = NpIcons.Navigation,
            enabled = s.point != null,
            height = 112.dp,
        )
        AnimatedContent(s.inShareCart, transitionSpec = { fadeIn(tween(Motion.SCRIM_MS)).togetherWith(fadeOut(tween(Motion.SCRIM_MS))) }, label = "cart") { added ->
            PillButton(
                stringResource(if (added) R.string.detail_added_share else R.string.detail_add_share),
                { onAction(PlaceDetailAction.AddToShare) },
                Modifier.fillMaxWidth(),
                icon = if (added) NpIcons.Check else NpIcons.Plus,
            )
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 88.dp).alpha(if (s.globalRemind) 1f else DIM), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.detail_remind), style = NpType.button, color = Np.colors.text)
                if (!s.globalRemind) Text(stringResource(R.string.detail_remind_off_global), style = NpType.small, color = Np.colors.muted)
            }
            Switch(
                checked = s.remindEnabled,
                onCheckedChange = { onAction(PlaceDetailAction.Remind(it)) },
                enabled = s.globalRemind,
                colors = SwitchDefaults.colors(checkedTrackColor = Np.colors.brand, checkedThumbColor = Color.White),
            )
        }
    }
}

private const val DIM = 0.4f
