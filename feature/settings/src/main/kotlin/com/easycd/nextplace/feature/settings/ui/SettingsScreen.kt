package com.easycd.nextplace.feature.settings.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.easycd.nextplace.core.ui.component.GlassCard
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

/** S-11 설정: 리마인드 · 알림 거리 · 식사 시간 · 음량 · 시점 · (떨어뜨린) 모두 삭제 → S-11b. */
@Composable
fun SettingsScreen(state: SettingsUiState, onAction: (SettingsAction) -> Unit) {
    val s = state.settings
    Box {
        ScreenFrame(stringResource(R.string.settings_title), onBack = { onAction(SettingsAction.Back) }) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 40.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Toggle(stringResource(R.string.set_remind), stringResource(R.string.set_remind_sub), s.remindEnabled) { onAction(SettingsAction.Remind(it)) }
                        Column(Modifier.alpha(if (s.remindEnabled) 1f else DIM)) {
                            Label(stringResource(R.string.set_radius), null)
                            Segmented(
                                Tunables.REMIND_RADIUS_CHOICES_M.map { if (it >= KM) stringResource(R.string.set_km) else stringResource(R.string.set_meters, it) },
                                Tunables.REMIND_RADIUS_CHOICES_M.indexOf(s.remindRadiusM),
                                enabled = s.remindEnabled,
                            ) { onAction(SettingsAction.Radius(Tunables.REMIND_RADIUS_CHOICES_M[it])) }
                        }
                        Toggle(stringResource(R.string.set_meal), stringResource(R.string.set_meal_sub), s.mealTimeOnly, enabled = s.remindEnabled) { onAction(SettingsAction.MealOnly(it)) }
                    }
                }
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Label(stringResource(R.string.set_volume), stringResource(R.string.set_volume_sub))
                        Slider(
                            value = s.voiceVolume,
                            onValueChange = { onAction(SettingsAction.Volume(it)) },
                            colors = SliderDefaults.colors(thumbColor = Np.colors.brand, activeTrackColor = Np.colors.brand),
                            modifier = Modifier.heightIn(min = 76.dp),
                        )
                        Label(stringResource(R.string.set_lookback), stringResource(R.string.set_lookback_sub))
                        Segmented(
                            Tunables.LOOKBACK_CHOICES_SEC.map { stringResource(R.string.set_seconds, it) },
                            Tunables.LOOKBACK_CHOICES_SEC.indexOf(s.lookbackSec),
                        ) { onAction(SettingsAction.Lookback(Tunables.LOOKBACK_CHOICES_SEC[it])) }
                    }
                }
                Spacer(Modifier.height(40.dp))
                PillButton(stringResource(R.string.set_delete_all), { onAction(SettingsAction.DeleteAll) }, kind = PillKind.DANGER_OUTLINE, icon = NpIcons.Trash, enabled = state.total > 0)
                Spacer(Modifier.height(40.dp))
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
private fun Label(title: String, sub: String?) {
    Column {
        Text(title, style = NpType.button, color = Np.colors.text)
        if (sub != null) Text(sub, style = NpType.small, color = Np.colors.muted)
    }
}

@Composable
private fun Toggle(title: String, sub: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 88.dp).alpha(if (enabled) 1f else DIM), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { Label(title, sub) }
        Spacer(Modifier.width(16.dp))
        Switch(checked, onChange, enabled = enabled, colors = SwitchDefaults.colors(checkedTrackColor = Np.colors.brand, checkedThumbColor = Color.White))
    }
}

/** 구간 선택: 주황 배경이 옆 칸으로 미끄러짐 (160ms). 각 칸 누르는 영역 76dp 이상. */
@Composable
private fun Segmented(labels: List<String>, selected: Int, enabled: Boolean = true, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).glass(CircleShape, elevation = 0.dp).padding(6.dp)) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            val bg by animateColorAsState(if (on) Np.colors.brand else Color.Transparent, tween(Motion.SCRIM_MS), label = "seg")
            Box(
                Modifier.weight(1f).heightIn(min = 76.dp).clip(CircleShape).background(bg).pressable(enabled) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { Text(label, style = NpType.button, color = if (on) Color.White else Np.colors.text) }
        }
    }
}

private const val DIM = 0.4f
private const val KM = 1_000
