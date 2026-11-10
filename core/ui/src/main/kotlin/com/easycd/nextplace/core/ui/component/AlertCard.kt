package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Np

/**
 * 차량 알림 카드 (S-05 · S-15): 위쪽 48dp, 높이 300dp, 아래 가장자리에 남은 시간 막대.
 * Android heads-up 이 차량에서 안 뜰 때 앱 창 안에서 같은 모양으로 쓴다.
 */
@Composable
fun AlertCard(remaining: Float?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(36.dp)
    Box(modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(top = 48.dp), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 1100.dp).fillMaxWidth().glass(shape, strong = true, elevation = 24.dp)) {
            Column(Modifier.padding(36.dp).padding(bottom = 8.dp)) { content() }
            if (remaining != null) {
                Box(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth(remaining.coerceIn(0f, 1f)).height(8.dp)
                        .clip(RoundedCornerShape(4.dp)).background(Np.colors.brand),
                )
            }
        }
    }
}

/** 알림 카드 들어옴·나감: 위에서 24dp 내려오며 페이드 240ms(emphasized), 닫힐 때 위로 24dp 200ms. */
@Composable
fun AlertHost(visible: Boolean, content: @Composable () -> Unit) {
    val shift = with(androidx.compose.ui.platform.LocalDensity.current) { com.easycd.nextplace.core.ui.theme.Motion.driveShift.roundToPx() }
    androidx.compose.animation.AnimatedVisibility(
        visible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(com.easycd.nextplace.core.ui.theme.Motion.ALERT_MS, easing = com.easycd.nextplace.core.ui.theme.Motion.emphasized)) +
            androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(com.easycd.nextplace.core.ui.theme.Motion.ALERT_MS, easing = com.easycd.nextplace.core.ui.theme.Motion.emphasized)) { -shift },
        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(com.easycd.nextplace.core.ui.theme.Motion.BASE_MS)) +
            androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(com.easycd.nextplace.core.ui.theme.Motion.BASE_MS)) { -shift },
    ) { content() }
}

/** [startedAt]부터 [totalMs] 동안 1 → 0. 시작 전(null)이면 1. */
@Composable
fun rememberRemaining(startedAt: Long?, totalMs: Long): Float {
    var f by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    androidx.compose.runtime.LaunchedEffect(startedAt) {
        f = 1f
        if (startedAt == null) return@LaunchedEffect
        while (f > 0f) {
            androidx.compose.runtime.withFrameMillis { }
            f = (1f - (System.currentTimeMillis() - startedAt).toFloat() / totalMs).coerceAtLeast(0f)
        }
    }
    return f
}
