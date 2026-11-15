package com.easycd.nextplace.core.ui.component

import com.easycd.nextplace.core.ui.icon.NpIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/** 겹침 화면 공통: 뒤 화면 72% 어둡게 (160ms) + 창 96→100% (200ms). 배경을 눌러도 아무 일 없음. */
@Composable
fun ScrimOverlay(visible: Boolean, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    AnimatedVisibility(visible, enter = fadeIn(tween(Motion.SCRIM_MS)), exit = fadeOut(tween(Motion.SCRIM_MS))) {
        Box(
            modifier.fillMaxSize().background(Np.colors.scrim)
                .clickable(remember { MutableInteractionSource() }, indication = null) { },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.animateEnterExit(enter = scaleIn(tween(Motion.BASE_MS, easing = Motion.standard), initialScale = 0.96f)), content = content)
        }
    }
}

/**
 * 취소 왼쪽 · 실행 오른쪽, 오른쪽 끝에 붙임 (설계서 S-08c · S-11b). 창이 두 버튼을 나란히 둘 만큼 넓지 않으면
 * (설계 기준보다 좁은 창) 위아래로 쌓고 실행을 아래에 둔다: 나란히 두면 실행 버튼이 눌러지지 않을 만큼 찌그러진다.
 */
@Composable
private fun DialogButtons(confirmText: String, cancelText: String, onConfirm: () -> Unit, onCancel: () -> Unit, danger: Boolean) {
    val kind = if (danger) PillKind.DANGER else PillKind.PRIMARY
    val icon = if (danger) NpIcons.Trash else null
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= CANCEL_MIN + CONFIRM_MIN + GAP) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GAP, Alignment.End)) {
                PillButton(cancelText, onCancel, Modifier.widthIn(min = CANCEL_MIN))
                PillButton(confirmText, onConfirm, Modifier.widthIn(min = CONFIRM_MIN), kind = kind, icon = icon)
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GAP / 2)) {
                PillButton(cancelText, onCancel, Modifier.fillMaxWidth())
                PillButton(confirmText, onConfirm, Modifier.fillMaxWidth(), kind = kind, icon = icon)
            }
        }
    }
}

private val CANCEL_MIN = 240.dp
private val CONFIRM_MIN = 280.dp
private val GAP = 24.dp

/** 확인 창 (S-08c, S-11b 등): 가운데 폭 800dp, 질문 40 + 설명 28 + 버튼. 취소 왼쪽, 실행 오른쪽. */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    question: String,
    description: String?,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    danger: Boolean = false,
    width: Dp = 800.dp,
) {
    ScrimOverlay(visible) {
        GlassCard(Modifier.widthIn(max = width).padding(horizontal = 24.dp), strong = true, padding = 48.dp) {
            Column {
                Text(question, style = NpType.cardTitle, color = Np.colors.text)
                if (description != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(description, style = NpType.body, color = Np.colors.muted)
                }
                Spacer(Modifier.height(40.dp))
                DialogButtons(confirmText, cancelText, onConfirm, onCancel, danger)
            }
        }
    }
}
