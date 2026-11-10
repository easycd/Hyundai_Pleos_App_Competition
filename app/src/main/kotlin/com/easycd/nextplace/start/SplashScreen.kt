package com.easycd.nextplace.start

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easycd.nextplace.R
import com.easycd.nextplace.core.ui.component.AppMark
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.core.ui.theme.ReduceMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * S-00 시작 (화면 설계서 A안 확정): 아이콘이 흐리게 24dp 아래에서 떠오른 뒤(450ms) 숟가락(450ms 지연)·젓가락(570ms 지연)이
 * 원 안에서 대각선으로 올라와 X 로 교차, 가운데 원 1.06배 톡(865ms), 이름(900ms)·문구(1000ms) 페이드 인. 1.2초 뒤 [onDone].
 * 시스템 애니메이션을 끈 차는 바로 완성된 아이콘을 보여 준다.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val reduce = ReduceMotion.enabled
    val rise = remember { Animatable(if (reduce) 1f else 0f) }
    val spoon = remember { Animatable(if (reduce) 1f else 0f) }
    val sticks = remember { Animatable(if (reduce) 1f else 0f) }
    val badge = remember { Animatable(1f) }
    val name = remember { Animatable(if (reduce) 1f else 0f) }
    val tag = remember { Animatable(if (reduce) 1f else 0f) }
    val shift = with(LocalDensity.current) { Motion.driveShift.toPx() }

    LaunchedEffect(Unit) {
        if (!reduce) {
            launch { rise.animateTo(1f, tween(RISE_MS, easing = Motion.emphasized)) }
            launch { delay(SPOON_DELAY); spoon.animateTo(1f, tween(UTENSIL_MS, easing = Overshoot)) }
            launch { delay(STICKS_DELAY); sticks.animateTo(1f, tween(UTENSIL_MS, easing = Overshoot)) }
            launch {
                delay(BADGE_DELAY)
                badge.animateTo(BADGE_POP, tween(BADGE_MS / 2))
                badge.animateTo(1f, tween(BADGE_MS / 2))
            }
            launch { delay(NAME_DELAY); name.animateTo(1f, tween(Motion.BASE_MS, easing = Motion.standard)) }
            launch { delay(TAG_DELAY); tag.animateTo(1f, tween(Motion.BASE_MS)) }
        }
        delay(TOTAL_MS)
        onDone()
    }

    Column(
        Modifier.fillMaxSize().background(Np.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
    ) {
        AppMark(
            Modifier
                .graphicsLayer {
                    alpha = rise.value
                    translationY = (1f - rise.value) * shift
                }
                .blur((BLUR * (1f - rise.value)).dp),
            size = 240.dp,
            spoon = spoon.value,
            sticks = sticks.value,
            badge = badge.value,
        )
        Text(
            stringResource(R.string.app_name),
            color = Np.colors.text,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer {
                alpha = name.value
                translationY = (1f - name.value) * shift
            },
        )
        Text(stringResource(R.string.splash_tagline), style = NpType.body, color = Np.colors.muted, modifier = Modifier.graphicsLayer { alpha = tag.value })
    }
}

private val Overshoot = CubicBezierEasing(0.34f, 1.4f, 0.64f, 1f)
private const val RISE_MS = 450
private const val UTENSIL_MS = 380
private const val SPOON_DELAY = 450L
private const val STICKS_DELAY = 570L
private const val BADGE_DELAY = 865L
private const val BADGE_MS = 180
private const val BADGE_POP = 1.06f
private const val NAME_DELAY = 900L
private const val TAG_DELAY = 1_000L
private const val TOTAL_MS = 1_200L
private const val BLUR = 14f
