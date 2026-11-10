package com.easycd.nextplace.feature.recommend.ui

import com.easycd.nextplace.core.ui.text.TimeText
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.domain.voice.VoicePrompt.NoneReason
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.RoundIconButton
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.SpinnerRing
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.recommend.R
import com.easycd.nextplace.feature.recommend.session.RecCard
import com.easycd.nextplace.feature.recommend.session.RecommendState

/** S-06 추천 · S-06b 추천할 가게 없음 · S-06c 선택됨. 주행 중에도 쓰는 화면 (스크롤 없음). */
@Composable
fun RecommendScreen(state: RecommendUiState, onAction: (RecommendAction) -> Unit) {
    val s = state.session
    val category = (s as? RecommendState.Showing)?.category ?: (s as? RecommendState.Empty)?.category
    val title = stringResource(R.string.rec_title)
    ScreenFrame(title, onBack = { onAction(RecommendAction.Back) }) {
        when (s) {
            is RecommendState.Showing -> Showing(s, onAction)
            is RecommendState.Empty -> Empty(s, onAction)
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SpinnerRing() }
        }
    }
}

@Composable
private fun Showing(s: RecommendState.Showing, onAction: (RecommendAction) -> Unit) {
    val portrait = LocalPortrait.current
    val shift = with(LocalDensity.current) { Motion.driveShift.roundToPx() }
    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 16.dp)) {
        AnimatedContent(
            targetState = s.page,
            transitionSpec = {
                (fadeIn(tween(Motion.BASE_MS)) + slideInVertically(tween(Motion.BASE_MS, easing = Motion.standard)) { shift })
                    .togetherWith(fadeOut(tween(Motion.BASE_MS)))
            },
            modifier = Modifier.weight(1f),
            label = "page",
        ) { _ ->
            if (portrait) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    s.cards.forEachIndexed { i, c -> PortraitCard(c, s, i, onAction) }
                }
            } else {
                Row(Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    s.cards.forEachIndexed { i, c -> LandscapeCard(c, s, i, Modifier.weight(1f), onAction) }
                    repeat(3 - s.cards.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillButton(stringResource(R.string.rec_more), { onAction(RecommendAction.More) }, Modifier.widthIn(min = 400.dp), enabled = s.selectedIndex == null)
        }
    }
}

@Composable
private fun cardLook(s: RecommendState.Showing, i: Int): Triple<Float, Float, Boolean> {
    val selected = s.selectedIndex == i
    val dim = s.selectedIndex != null && !selected
    val scale by animateFloatAsState(if (selected) SELECTED_SCALE else 1f, tween(Motion.BASE_MS, easing = Motion.standard), label = "s")
    val alpha by animateFloatAsState(if (dim) DIM else 1f, tween(Motion.BASE_MS), label = "a")
    return Triple(scale, alpha, selected || s.readingIndex == i)
}

@Composable
private fun LandscapeCard(c: RecCard, s: RecommendState.Showing, i: Int, modifier: Modifier, onAction: (RecommendAction) -> Unit) {
    val (scale, alpha, outlined) = cardLook(s, i)
    GlassCard(
        modifier.fillMaxHeight().scale(scale).alpha(alpha),
        highlight = if (outlined) Np.colors.brand else null,
        onClick = { onAction(RecommendAction.Select(i)) },
    ) {
        Column(Modifier.fillMaxHeight()) {
            NumberBadge(c.number)
            Spacer(Modifier.height(24.dp))
            Text(c.name, style = NpType.cardTitle, color = Np.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(16.dp))
            Text(meta(c), style = NpType.body, color = Np.colors.text, maxLines = 1)
            Spacer(Modifier.height(8.dp))
            Text(history(c), style = NpType.small, color = Np.colors.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            PillButton(stringResource(R.string.rec_start), { onAction(RecommendAction.Select(i)) }, Modifier.fillMaxWidth(), kind = PillKind.PRIMARY, icon = NpIcons.Navigation)
        }
    }
}

@Composable
private fun PortraitCard(c: RecCard, s: RecommendState.Showing, i: Int, onAction: (RecommendAction) -> Unit) {
    val (scale, alpha, outlined) = cardLook(s, i)
    GlassCard(
        Modifier.fillMaxWidth().scale(scale).alpha(alpha),
        highlight = if (outlined) Np.colors.brand else null,
        padding = 28.dp,
        onClick = { onAction(RecommendAction.Select(i)) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(c.number)
            Spacer(Modifier.width(24.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(meta(c), style = NpType.body, color = Np.colors.muted, maxLines = 1)
            }
            RoundIconButton(NpIcons.Navigation, stringResource(R.string.rec_start), { onAction(RecommendAction.Select(i)) }, size = 96.dp, primary = true)
        }
    }
}

private fun meta(c: RecCard) = listOfNotNull(c.category, c.distanceM?.let(DistanceText::of)).joinToString(" · ")

/** "저장 11월 2일 · 안 가 봄" 또는 "방문 1회 · “괜찮았어”" (설계서 S-06 카드 셋째 줄). */
@Composable
private fun history(c: RecCard): String {
    val first = if (c.visits > 0) stringResource(R.string.rec_visits, c.visits) else stringResource(R.string.rec_saved_on, TimeText.saved(c.savedAt))
    val second = c.memo?.let { "“$it”" } ?: if (c.visits == 0) stringResource(R.string.rec_not_visited) else null
    return listOfNotNull(first, second).joinToString(" · ")
}

/** 번호 원 56dp (주황 채움, 흰 숫자). TTS 가 읽는 번호와 같다. */
@Composable
private fun NumberBadge(n: Int) {
    Box(Modifier.size(56.dp).clip(CircleShape).background(Np.colors.brand), contentAlignment = Alignment.Center) {
        Text("$n", style = NpType.title, color = Color.White)
    }
}

@Composable
private fun Empty(s: RecommendState.Empty, onAction: (RecommendAction) -> Unit) {
    val cat = s.category?.let { Josa.iGa(it) }
    val text = when (s.reason) {
        NoneReason.NONE_SAVED -> stringResource(R.string.rec_none_saved)
        NoneReason.NONE_10KM -> cat?.let { stringResource(R.string.rec_none_10km_category, it) } ?: stringResource(R.string.rec_none_10km)
        NoneReason.NONE_30KM -> cat?.let { stringResource(R.string.rec_none_30km_category, it) } ?: stringResource(R.string.rec_none_30km)
    }
    Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(NpIcons.MapPin, null, tint = Np.colors.muted, modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(24.dp))
        Text(text, style = NpType.question, color = Np.colors.text, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.rec_none_sub), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
        if (s.reason == NoneReason.NONE_10KM) {
            Spacer(Modifier.height(40.dp))
            PillButton(stringResource(R.string.rec_widen), { onAction(RecommendAction.Widen) }, kind = PillKind.PRIMARY, height = 112.dp)
        }
    }
}

private const val SELECTED_SCALE = 1.04f
private const val DIM = 0.4f
