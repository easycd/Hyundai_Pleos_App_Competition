package com.easycd.nextplace.feature.myplaces.ui.list

import com.easycd.nextplace.core.ui.component.PillKind
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.FilterChip
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PinMap
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.drive.LocalCompact
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R
import com.easycd.nextplace.feature.myplaces.ui.common.PlaceRow

/** S-07 내 맛집 (목록 + 지도) · S-07b 저장 전. 정차 전용. */
@Composable
fun MyPlacesScreen(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    ScreenFrame(
        stringResource(R.string.mp_title),
        onBack = { onAction(MyPlacesAction.Back) },
        action = {
            if (state.total > 0) PillButton(stringResource(R.string.mp_share), { onAction(MyPlacesAction.Share) }, icon = NpIcons.Share)
        },
    ) {
        if (state.loaded && state.total == 0) Empty(state, onAction) else Content(state, onAction)
    }
}

@Composable
private fun Content(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    val portrait = LocalPortrait.current
    Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp)) {
        Column(Modifier.weight(1f)) {
            Filters(state, onAction)
            Spacer(Modifier.height(8.dp))
            if (state.rows.isEmpty()) {
                Text(stringResource(R.string.mp_empty_filter), style = NpType.body, color = Np.colors.muted, modifier = Modifier.padding(24.dp))
            }
            val listState = rememberLazyListState()
            LaunchedEffect(state.selectedId) {
                val i = state.rows.indexOfFirst { it.id == state.selectedId }
                if (i >= 0) listState.animateScrollToItem(i)
            }
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.rows, key = { it.id }) { row ->
                    PlaceRow(
                        row,
                        selected = row.id == state.selectedId,
                        onClick = { onAction(MyPlacesAction.Open(row.id)) },
                        fresh = row.id in state.freshIds,
                        modifier = Modifier.animateItem(fadeInSpec = tween(Motion.BASE_MS), fadeOutSpec = tween(Motion.BASE_MS)),
                    )
                }
            }
        }
        if (!portrait) {
            Spacer(Modifier.width(if (LocalCompact.current) 24.dp else 40.dp))
            Box(Modifier.weight(mapWeight()).fillMaxSize().padding(bottom = 24.dp)) {
                PinMap(state.pins, state.me, Modifier.fillMaxSize(), onPin = { onAction(MyPlacesAction.Pin(it)) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Filters(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    // 좁은 창에서는 칩을 줄이지 않고 다음 줄로 넘긴다 (글자가 잘리거나 칩 안에서 두 줄이 되지 않게)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip("${stringResource(R.string.mp_filter_all)} ${state.total}", state.filter == PlaceFilter.ALL, { onAction(MyPlacesAction.Filter(PlaceFilter.ALL)) })
        FilterChip("${stringResource(R.string.mp_filter_unconfirmed)} ${state.unconfirmedCount}", state.filter == PlaceFilter.UNCONFIRMED, { onAction(MyPlacesAction.Filter(PlaceFilter.UNCONFIRMED)) })
        FilterChip("${stringResource(R.string.mp_filter_visited)} ${state.visitedCount}", state.filter == PlaceFilter.VISITED, { onAction(MyPlacesAction.Filter(PlaceFilter.VISITED)) })
        // 정렬은 칩 하나를 눌러 바꾼다 (가까운 순 ↔ 최근 저장 순)
        val near = state.sort == PlaceSort.NEAR
        FilterChip(
            stringResource(if (near) R.string.mp_sort_near else R.string.mp_sort_recent),
            false,
            { onAction(MyPlacesAction.Sort(if (near) PlaceSort.RECENT else PlaceSort.NEAR)) },
        )
    }
}

@Composable
private fun Empty(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    val portrait = LocalPortrait.current
    Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 8.dp)) {
        Column(Modifier.weight(1f).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(NpIcons.MapPin, null, tint = Np.colors.muted, modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.mp_empty_title), style = NpType.title, color = Np.colors.text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.mp_empty_sub), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            Text(stringResource(R.string.mp_empty_example), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            PillButton(stringResource(R.string.mp_speak), { onAction(MyPlacesAction.Speak) }, Modifier.widthIn(min = 320.dp), kind = PillKind.PRIMARY, icon = NpIcons.Mic, height = 112.dp)
        }
        if (!portrait) {
            Spacer(Modifier.width(40.dp))
            Box(Modifier.weight(mapWeight()).fillMaxSize().padding(bottom = 24.dp)) { PinMap(emptyList(), state.me, Modifier.fillMaxSize()) }
        }
    }
}

/** 목록 : 지도 = 1 : 1. 좁은 가로 창은 목록에 자리를 더 준다 (가게 이름이 잘리지 않게). */
@Composable
private fun mapWeight(): Float = if (LocalCompact.current) COMPACT_MAP_WEIGHT else 1f

private const val COMPACT_MAP_WEIGHT = 0.6f
