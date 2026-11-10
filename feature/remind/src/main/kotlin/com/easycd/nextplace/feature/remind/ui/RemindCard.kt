package com.easycd.nextplace.feature.remind.ui

import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.domain.model.Side
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

/**
 * S-05 리마인드: 알림 카드 한 줄 (핀 · "지난주에 저장한 서울갈비" / "300m 앞 · 오른쪽" / "“응”이라고 말해도 돼요" · 닫기 · 안내 시작),
 * 아래 가장자리 남은 시간 막대 10초. → S-05b 확인 문구 "서울갈비로 안내할게요 · Pleos 내비로 넘어가요" 1.5초.
 * 세로 창은 버튼을 아래 줄로 내린다.
 */
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
                is RemindState.Routing -> Routing(cur)
                RemindState.Hidden -> Unit
            }
        }
    }
}

@Composable
private fun Routing(cur: RemindState.Routing) {
    AlertCard(null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(NpIcons.Check, null, tint = Np.colors.green, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(20.dp))
            val n = Josa.ro(cur.name)
            Text(
                if (cur.changing) stringResource(R.string.remind_route_change, n) else stringResource(R.string.remind_route, n),
                style = NpType.cardTitle,
                color = Np.colors.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!LocalPortrait.current) Text(stringResource(R.string.remind_to_navi), style = NpType.body, color = Np.colors.muted)
        }
    }
}

@Composable
private fun Asking(s: RemindState.Asking, onAction: (RemindAction) -> Unit) {
    val remaining = rememberRemaining(s.waitStartedAt, Tunables.REMIND_BUTTON_WAIT_MS)
    val portrait = LocalPortrait.current
    AlertCard(remaining) {
        val info: @Composable (Modifier) -> Unit = { m ->
            Row(m, verticalAlignment = Alignment.CenterVertically) {
                Icon(NpIcons.MapPin, null, tint = Np.colors.text, modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(28.dp))
                Column {
                    Text(
                        stringResource(R.string.remind_title, whenText(s.savedWhen), s.name),
                        style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    val where = if (s.ahead) stringResource(R.string.remind_ahead, DistanceText.of(s.distanceM.toDouble())) else stringResource(R.string.remind_near)
                    val side = when (s.side) {
                        Side.LEFT -> stringResource(R.string.remind_left)
                        Side.RIGHT -> stringResource(R.string.remind_right)
                        Side.UNKNOWN -> null
                    }
                    Text(listOfNotNull(where, side).joinToString(" · "), style = NpType.body, color = Np.colors.text)
                    Text(stringResource(R.string.remind_voice_hint), style = NpType.small, color = Np.colors.muted)
                }
            }
        }
        val close: @Composable (Modifier) -> Unit = { m -> PillButton(stringResource(R.string.remind_close), { onAction(RemindAction.Close) }, m, height = 112.dp) }
        val go: @Composable (Modifier) -> Unit = { m ->
            PillButton(stringResource(R.string.remind_start), { onAction(RemindAction.Go) }, m, kind = PillKind.PRIMARY, icon = NpIcons.Navigation, height = 112.dp)
        }
        if (portrait) {
            Column {
                info(Modifier)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    close(Modifier.weight(1f))
                    go(Modifier.weight(1f))
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                info(Modifier.weight(1f))
                Spacer(Modifier.width(24.dp))
                close(Modifier.width(200.dp))
                Spacer(Modifier.width(32.dp))
                go(Modifier.width(300.dp))
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
