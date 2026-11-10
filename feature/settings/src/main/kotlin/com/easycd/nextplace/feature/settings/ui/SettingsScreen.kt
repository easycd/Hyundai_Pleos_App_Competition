package com.easycd.nextplace.feature.settings.ui

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.ui.component.ConfirmDialog
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.glass
import com.easycd.nextplace.core.ui.component.pressable
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.settings.R

/** S-11 설정: 한 줄에 이름(왼쪽) · 조절(오른쪽). 리마인드 · 알림 거리 · 식사 시간 · 음량 · 시점, 맨 아래 모두 삭제 → S-11b. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsUiState, onAction: (SettingsAction) -> Unit) {
    val s = state.settings
    Box {
        ScreenFrame(stringResource(R.string.settings_title), onBack = { onAction(SettingsAction.Back) }) {
            Column(Modifier.fillMaxSize().padding(horizontal = 48.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    SettingRow(stringResource(R.string.set_remind)) {
                        Switch(s.remindEnabled, { onAction(SettingsAction.Remind(it)) }, colors = switchColors(), modifier = Modifier.scale(SWITCH_SCALE))
                    }
                    SettingRow(stringResource(R.string.set_radius), enabled = s.remindEnabled) {
                        Segmented(
                            Tunables.REMIND_RADIUS_CHOICES_M.map { if (it >= KM) stringResource(R.string.set_km) else stringResource(R.string.set_meters, it) },
                            Tunables.REMIND_RADIUS_CHOICES_M.indexOf(s.remindRadiusM),
                            enabled = s.remindEnabled,
                        ) { onAction(SettingsAction.Radius(Tunables.REMIND_RADIUS_CHOICES_M[it])) }
                    }
                    SettingRow(stringResource(R.string.set_meal), enabled = s.remindEnabled) {
                        Switch(s.mealTimeOnly, { onAction(SettingsAction.MealOnly(it)) }, enabled = s.remindEnabled, colors = switchColors(), modifier = Modifier.scale(SWITCH_SCALE))
                    }
                    SettingRow(stringResource(R.string.set_volume)) {
                        Icon(NpIcons.Volume, null, tint = Np.colors.muted, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(16.dp))
                        Slider(
                            value = s.voiceVolume,
                            onValueChange = { onAction(SettingsAction.Volume(it)) },
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Np.colors.brand, inactiveTrackColor = Np.colors.glassBorder),
                            thumb = { Box(Modifier.size(52.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(Color.White)) },
                            modifier = Modifier.width(SLIDER_W.dp).heightIn(min = 76.dp),
                        )
                    }
                    SettingRow(stringResource(R.string.set_lookback)) {
                        Segmented(
                            Tunables.LOOKBACK_CHOICES_SEC.map { stringResource(R.string.set_seconds, it) },
                            Tunables.LOOKBACK_CHOICES_SEC.indexOf(s.lookbackSec),
                        ) { onAction(SettingsAction.Lookback(Tunables.LOOKBACK_CHOICES_SEC[it])) }
                    }
                }
                SettingRow(stringResource(R.string.set_delete_all_row), titleColor = Np.colors.dangerText, enabled = state.total > 0) {
                    PillButton(stringResource(R.string.set_delete), { onAction(SettingsAction.DeleteAll) }, kind = PillKind.DANGER_OUTLINE, enabled = state.total > 0)
                }
                Text(stringResource(R.string.set_footer), style = NpType.small, color = Np.colors.muted, modifier = Modifier.padding(vertical = 20.dp))
            }
        }
        ConfirmDialog(
            visible = state.confirmDeleteAll,
            question = stringResource(R.string.delete_all_q, state.total),
            description = stringResource(R.string.delete_all_desc),
            confirmText = stringResource(R.string.delete_all_ok),
            cancelText = stringResource(R.string.cancel),
            onConfirm = { onAction(SettingsAction.ConfirmDeleteAll) },
            onCancel = { onAction(SettingsAction.CancelDeleteAll) },
            danger = true,
        )
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(checkedTrackColor = Np.colors.brand, checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent)

/** 설정 한 줄: 높이 112dp, 아래 구분선. 꺼진 항목은 흐리게. */
@Composable
private fun SettingRow(title: String, titleColor: Color = Np.colors.text, enabled: Boolean = true, control: @Composable () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = ROW_H.dp).alpha(if (enabled) 1f else DIM), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = NpType.button, color = titleColor, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { control() }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Np.colors.glassBorder))
    }
}

/** 구간 선택: 주황 배경이 옆 칸으로 미끄러짐 (160ms). 각 칸 누르는 영역 76dp 이상. */
@Composable
private fun Segmented(labels: List<String>, selected: Int, enabled: Boolean = true, onSelect: (Int) -> Unit) {
    Row(Modifier.width(SEG_W.dp).glass(CircleShape, elevation = 0.dp).padding(4.dp)) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            val bg by animateColorAsState(if (on) Np.colors.brand else Color.Transparent, tween(Motion.SCRIM_MS), label = "seg")
            Box(
                Modifier.weight(1f).heightIn(min = 76.dp).clip(CircleShape).background(bg).pressable(enabled) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { Text(label, style = NpType.body, color = if (on) Color.White else Np.colors.muted) }
        }
    }
}

private const val ROW_H = 112
private const val SEG_W = 400
private const val SLIDER_W = 420
private const val SWITCH_SCALE = 1.5f
private const val DIM = 0.4f
private const val KM = 1_000
