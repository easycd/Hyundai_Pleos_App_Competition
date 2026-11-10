package com.easycd.nextplace.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np

/**
 * 리퀴드 글라스 (화면 설계서 1장): 반투명 흰색 + 밝은 윗선 + 1dp 테두리 + 그림자.
 * 뒤 내용 흐림(backdrop blur)은 Compose 에 없어 반투명 채움으로 근사한다.
 */
@Composable
fun Modifier.glass(shape: Shape, strong: Boolean = false, elevation: Dp = 12.dp): Modifier {
    val c = Np.colors
    val fill = if (strong) c.glassStrong else c.glass
    return this
        .shadow(elevation, shape, ambientColor = c.shadow, spotColor = c.shadow)
        .clip(shape)
        .background(fill)
        .background(Brush.verticalGradient(listOf(c.glassHighlight.copy(alpha = c.glassHighlight.alpha * HIGHLIGHT), fill.copy(alpha = 0f))))
        .border(1.dp, c.glassBorder, shape)
}

/** 모든 버튼 공통: 누르면 96%로 줄었다 복귀 (120ms). */
fun Modifier.pressable(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) PRESSED else 1f, tween(Motion.FAST_MS, easing = Motion.standard), label = "press")
    this
        .scale(scale)
        .clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)
}

private const val PRESSED = 0.96f
private const val HIGHLIGHT = 0.35f
