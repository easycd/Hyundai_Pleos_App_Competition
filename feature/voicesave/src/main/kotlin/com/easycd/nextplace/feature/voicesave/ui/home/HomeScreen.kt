package com.easycd.nextplace.feature.voicesave.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.AppMark
import com.easycd.nextplace.core.ui.component.ChipTone
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.RoundIconButton
import com.easycd.nextplace.core.ui.component.SpeakButton
import com.easycd.nextplace.core.ui.component.StatusChip
import com.easycd.nextplace.core.ui.component.glass
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.voicesave.R

/** S-01 홈 / S-01b 홈 · 저장 전. 가로 2칸은 왼쪽 말하기 · 오른쪽 카드, 세로 1칸(P-01)은 카드 위 · 버튼 하단 가운데. */
@Composable
fun HomeScreen(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    val portrait = LocalPortrait.current
    Box(Modifier.fillMaxSize().background(Np.colors.background)) {
        if (portrait) PortraitHome(state, onAction) else LandscapeHome(state, onAction)
        if (state.simulated) {
            Text(
                stringResource(R.string.home_simulated),
                style = NpType.small,
                color = Np.colors.muted,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            )
        }
    }
}

@Composable
private fun LandscapeHome(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppNamePill()
            Spacer(Modifier.width(16.dp))
            state.locationLabel?.let { LocationPill(it) }
            Spacer(Modifier.weight(1f))
            RoundIconButton(NpIcons.Settings, stringResource(R.string.home_settings), { onAction(HomeAction.Settings) })
        }
        Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                SpeakButton({ onAction(HomeAction.Speak) }, size = 360.dp, label = stringResource(R.string.home_speak))
                Text(stringResource(R.string.home_speak), style = NpType.cardTitle, color = Np.colors.text)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.home_example), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(40.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                RecommendCard(state, onAction)
                MyPlacesCard(state, onAction)
                LatestOrEmpty(state, onAction)
            }
        }
    }
}

@Composable
private fun PortraitHome(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppNamePill()
            Spacer(Modifier.weight(1f))
            RoundIconButton(NpIcons.Settings, stringResource(R.string.home_settings), { onAction(HomeAction.Settings) })
        }
        state.locationLabel?.let {
            Spacer(Modifier.height(16.dp))
            LocationPill(it)
        }
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            LatestOrEmpty(state, onAction)
            RecommendCard(state, onAction)
            MyPlacesCard(state, onAction)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SpeakButton({ onAction(HomeAction.Speak) }, size = 240.dp, label = stringResource(R.string.home_speak))
        }
    }
}

@Composable
private fun AppNamePill() {
    Row(
        Modifier.glass(CircleShape).heightIn(min = 88.dp).padding(start = 16.dp, end = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppMark(size = 56.dp)
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.home_app_name), style = NpType.title, color = Np.colors.text)
    }
}

@Composable
private fun LocationPill(label: LocationLabel) {
    val road = label.road ?: stringResource(R.string.home_current_location)
    val text = listOfNotNull(road, label.heading.takeIf { label.road != null }).joinToString(" · ")
    Row(
        Modifier.glass(CircleShape).heightIn(min = 88.dp).padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(NpIcons.Navigation, null, tint = Np.colors.brandText, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = NpType.body, color = Np.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RecommendCard(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    val sub = when {
        state.empty -> stringResource(R.string.home_recommend_empty)
        state.nearbyCount > 0 -> stringResource(R.string.home_recommend_sub, state.nearbyCount)
        else -> stringResource(R.string.home_recommend_sub_far)
    }
    NavCard(NpIcons.Utensils, stringResource(R.string.home_recommend), sub) { onAction(HomeAction.Recommend) }
}

@Composable
private fun MyPlacesCard(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    val sub = if (state.unconfirmed > 0) {
        stringResource(R.string.home_myplaces_sub, state.total, state.unconfirmed)
    } else {
        stringResource(R.string.home_myplaces_sub_all, state.total)
    }
    NavCard(NpIcons.MapPin, stringResource(R.string.home_myplaces), sub) { onAction(HomeAction.MyPlaces) }
}

@Composable
private fun NavCard(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), padding = 28.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).glass(RoundedCornerShape(20.dp), elevation = 0.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Np.colors.brandText, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.width(28.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = NpType.cardTitle, color = Np.colors.text)
                Text(sub, style = NpType.body, color = Np.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(NpIcons.ChevronRight, null, tint = Np.colors.muted, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun LatestOrEmpty(state: HomeUiState, onAction: (HomeAction) -> Unit) {
    AnimatedContent(
        targetState = state.latest,
        transitionSpec = { fadeIn(tween(Motion.BASE_MS)).togetherWith(fadeOut(tween(Motion.BASE_MS))) },
        label = "latest",
    ) { latest ->
        if (latest == null) EmptyCard() else LatestCardView(latest) { onAction(HomeAction.OpenLatest) }
    }
}

@Composable
private fun EmptyCard() {
    GlassCard(Modifier.fillMaxWidth(), padding = 32.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_empty_label), style = NpType.body, color = Np.colors.muted, modifier = Modifier.weight(1f))
                StatusChip(stringResource(R.string.chip_none), ChipTone.SAVED)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.home_empty_title), style = NpType.cardTitle, color = Np.colors.text)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.home_empty_sub), style = NpType.body, color = Np.colors.muted)
        }
    }
}

@Composable
private fun LatestCardView(card: LatestCard, onClick: () -> Unit) {
    val (chipText, tone) = when (card.chip) {
        LatestChip.UNCONFIRMED -> stringResource(R.string.chip_unconfirmed) to ChipTone.UNCONFIRMED
        LatestChip.FOUND -> stringResource(R.string.chip_found) to ChipTone.FOUND
        LatestChip.VISITED -> stringResource(R.string.chip_visits, card.visits) to ChipTone.VISITED
        LatestChip.SAVED -> stringResource(R.string.chip_saved) to ChipTone.SAVED
    }
    val unknown = stringResource(R.string.home_unknown_name)
    val title = card.name ?: listOfNotNull(
        unknown,
        if (card.noLocation) stringResource(R.string.home_no_location) else card.detail,
    ).joinToString(" · ")
    GlassCard(Modifier.fillMaxWidth(), padding = 32.dp, onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_latest_label, TimeText.saved(card.savedAt)), style = NpType.body, color = Np.colors.muted, modifier = Modifier.weight(1f))
                StatusChip(chipText, tone)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(NpIcons.MapPin, null, tint = Np.colors.brandText, modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(20.dp))
                Text(title, style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
