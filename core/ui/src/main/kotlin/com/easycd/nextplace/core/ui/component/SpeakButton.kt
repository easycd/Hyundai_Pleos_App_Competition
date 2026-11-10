package com.easycd.nextplace.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np

/** 말하기 버튼: 주황 유리 원형 (색이 있는 유리는 이것 하나). 바깥 빛이 2초 주기로 숨 쉰다. */
@Composable
fun SpeakButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 360.dp, label: String) {
    val c = Np.colors
    val t = rememberInfiniteTransition(label = "breath")
    val glow by t.animateFloat(GLOW_MIN, GLOW_MAX, infiniteRepeatable(tween(BREATH_MS / 2, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")
    Box(modifier.size(size + 80.dp).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.toPx() / 2
            val g = glow.dp.toPx()
            // 버튼 가장자리에서 진하게 시작해 바깥으로 사라지는 빛 (설계서 S-01 ①: 버튼 둘레 고리 2겹)
            val edge = r / (r + g)
            drawCircle(
                Brush.radialGradient(
                    0f to c.brand.copy(alpha = 0.5f),
                    edge to c.brand.copy(alpha = 0.5f),
                    (edge + 1f) / 2f to c.brand.copy(alpha = 0.22f),
                    1f to Color.Transparent,
                    center = center,
                    radius = r + g,
                ),
                r + g,
            )
        }
        Box(
            Modifier.size(size).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFF08A66), c.brand, Color(0xFFD94A22))))
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .pressable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.fillMaxSize().padding(14.dp).border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape))
            Icon(NpIcons.Mic, null, tint = Color.White, modifier = Modifier.size(size * ICON))
        }
    }
}

private const val BREATH_MS = 2_000
private const val GLOW_MIN = 24f
private const val GLOW_MAX = 40f
private const val ICON = 0.36f
