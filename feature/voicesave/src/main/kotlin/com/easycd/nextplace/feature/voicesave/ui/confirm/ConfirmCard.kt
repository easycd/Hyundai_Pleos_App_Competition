package com.easycd.nextplace.feature.voicesave.ui.confirm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.Side
import com.easycd.nextplace.core.ui.component.CountdownRing
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.SoundBars
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.voicesave.R

/** S-03 확인 · S-03b 두 번째 후보. 주행 중 화면이라 이동은 24dp 이하. 예는 오른쪽, 아니요는 왼쪽. */
@Composable
fun ConfirmCard(state: ConfirmUiState, onAction: (ConfirmAction) -> Unit) {
    val portrait = LocalPortrait.current
    val shift = with(LocalDensity.current) { Motion.driveShift.roundToPx() }
    AnimatedVisibility(state.visible, enter = fadeIn(tween(Motion.BASE_MS)), exit = fadeOut(tween(Motion.SCRIM_MS))) {
        Column(
            Modifier.fillMaxSize().background(Np.colors.background).padding(horizontal = 48.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(if (state.second) R.string.confirm_q_second else R.string.confirm_q),
                style = NpType.question,
                color = Np.colors.text,
            )
            Spacer(Modifier.height(32.dp))
            AnimatedContent(
                targetState = state,
                contentKey = { it.key },
                transitionSpec = {
                    val enter = if (targetState.second) slideInHorizontally(tween(Motion.BASE_MS, easing = Motion.standard)) { shift }
                    else slideInVertically(tween(Motion.BASE_MS, easing = Motion.standard)) { shift }
                    (fadeIn(tween(Motion.BASE_MS)) + enter).togetherWith(fadeOut(tween(Motion.SCRIM_MS)))
                },
                label = "candidate",
            ) { s -> CandidateCard(s) }
            if (portrait) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(40.dp))
            Row(Modifier.widthIn(max = 1000.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                val h = if (portrait) 136.dp else 136.dp
                PillButton(stringResource(R.string.confirm_no), { onAction(ConfirmAction.No) }, Modifier.weight(1f), height = h)
                PillButton(stringResource(R.string.confirm_yes), { onAction(ConfirmAction.Yes) }, Modifier.weight(1f), kind = PillKind.PRIMARY, icon = NpIcons.Check, height = h)
            }
            if (!portrait) Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            WaitRow(state.waitStartedAt)
        }
    }
}

@Composable
private fun CandidateCard(s: ConfirmUiState) {
    val dir = stringResource(if (s.ahead) R.string.dir_ahead else R.string.dir_behind)
    val dist = DistanceText.of(s.distanceM.toDouble())
    val cat = s.category ?: ""
    val meta = when (s.side) {
        Side.LEFT -> stringResource(R.string.confirm_meta_side, cat, dist, dir, stringResource(R.string.side_left))
        Side.RIGHT -> stringResource(R.string.confirm_meta_side, cat, dist, dir, stringResource(R.string.side_right))
        Side.UNKNOWN -> stringResource(R.string.confirm_meta, cat, dist, dir)
    }.trimStart(' ', '·')
    GlassCard(Modifier.widthIn(max = 1000.dp).fillMaxWidth(), padding = 56.dp) {
        Column {
            Text(s.name, style = NpType.placeName, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(16.dp))
            Text(meta, style = NpType.body, color = Np.colors.text)
            s.address?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = NpType.body, color = Np.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** 5초 카운트다운 링 + 듣는 중 막대. TTS 가 끝난 뒤부터. */
@Composable
private fun WaitRow(startedAt: Long?) {
    var fraction by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(startedAt) {
        if (startedAt == null) {
            fraction = 1f
            return@LaunchedEffect
        }
        while (true) {
            withFrameMillis { }
            fraction = 1f - (System.currentTimeMillis() - startedAt).toFloat() / Tunables.CONFIRM_WAIT_MS
            if (fraction <= 0f) break
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CountdownRing(if (startedAt == null) 1f else fraction)
        Spacer(Modifier.width(24.dp))
        Text(stringResource(R.string.confirm_wait), style = NpType.body, color = Np.colors.muted)
        Spacer(Modifier.width(32.dp))
        Box(Modifier.width(72.dp).height(48.dp)) {
            SoundBars(0.6f, Modifier.fillMaxSize(), count = 4, color = Np.colors.brandText, active = startedAt != null)
        }
    }
}
