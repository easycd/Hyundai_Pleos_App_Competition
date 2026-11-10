package com.easycd.nextplace.feature.remind.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.domain.voice.VoicePrompt.SavedWhen
import com.easycd.nextplace.core.ui.component.AlertCard
import com.easycd.nextplace.core.ui.component.AlertHost
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.rememberRemaining
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.remind.R
import com.easycd.nextplace.feature.remind.pipeline.RemindState

/** S-05 리마인드 (닫기 왼쪽 · 안내 시작 오른쪽, 남은 시간 막대 10초) → S-05b 확인 문구만 남고 1.5초 뒤 닫힘. */
@Composable
fun RemindCard(state: RemindUiState, onAction: (RemindAction) -> Unit) {
    val s = state.remind
    AlertHost(s != RemindState.Hidden) {
        AnimatedContent(
            targetState = s,
            contentKey = { it::class },
            transitionSpec = { fadeIn(tween(Motion.BASE_MS)).togetherWith(fadeOut(tween(Motion.BASE_MS))) },
            label = "remind",
        ) { cur ->
            when (cur) {
                is RemindState.Asking -> Asking(cur, onAction)
                is RemindState.Routing -> AlertCard(null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(NpIcons.Navigation, null, tint = Np.colors.brandText, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.width(20.dp))
                        val n = Josa.ro(cur.name)
                        Text(
                            if (cur.changing) stringResource(R.string.remind_route_change, n) else stringResource(R.string.remind_route, n),
                            style = NpType.cardTitle,
                            color = Np.colors.text,
                        )
                    }
                }
                RemindState.Hidden -> Unit
            }
        }
    }
}

@Composable
private fun Asking(s: RemindState.Asking, onAction: (RemindAction) -> Unit) {
    val remaining = rememberRemaining(s.waitStartedAt, Tunables.REMIND_BUTTON_WAIT_MS)
    AlertCard(remaining) {
        Column {
            Text(s.name, style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            val where = if (s.ahead) stringResource(R.string.remind_ahead, DistanceText.of(s.distanceM.toDouble())) else stringResource(R.string.remind_near)
            Text(
                listOf(stringResource(R.string.remind_saved_when, whenText(s.savedWhen)), where, stringResource(R.string.remind_question)).joinToString(" · "),
                style = NpType.body,
                color = Np.colors.muted,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PillButton(stringResource(R.string.remind_close), { onAction(RemindAction.Close) }, Modifier.weight(1f))
                PillButton(stringResource(R.string.remind_start), { onAction(RemindAction.Go) }, Modifier.weight(1f), kind = PillKind.PRIMARY, icon = NpIcons.Navigation)
            }
        }
    }
}

@Composable
private fun whenText(w: SavedWhen): String = stringResource(
    when (w) {
        SavedWhen.TODAY -> R.string.when_today
        SavedWhen.YESTERDAY -> R.string.when_yesterday
        SavedWhen.THIS_WEEK -> R.string.when_this_week
        SavedWhen.LAST_WEEK -> R.string.when_last_week
        SavedWhen.THIS_MONTH -> R.string.when_this_month
        SavedWhen.LONG_AGO -> R.string.when_long_ago
        SavedWhen.SHARED -> R.string.when_shared
    },
)
