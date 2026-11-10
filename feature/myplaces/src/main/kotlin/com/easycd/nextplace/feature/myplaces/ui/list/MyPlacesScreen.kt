package com.easycd.nextplace.feature.myplaces.ui.list

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
import com.easycd.nextplace.core.ui.component.SpeakButton
import com.easycd.nextplace.core.ui.drive.LocalPortrait
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
            Spacer(Modifier.width(24.dp))
            Box(Modifier.weight(1f).fillMaxSize().padding(bottom = 24.dp)) {
                PinMap(state.pins, state.me, Modifier.fillMaxSize(), onPin = { onAction(MyPlacesAction.Pin(it)) })
            }
        }
    }
}

@Composable
private fun Filters(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip(stringResource(R.string.mp_filter_all), state.filter == PlaceFilter.ALL, { onAction(MyPlacesAction.Filter(PlaceFilter.ALL)) })
        FilterChip(stringResource(R.string.mp_filter_unconfirmed), state.filter == PlaceFilter.UNCONFIRMED, { onAction(MyPlacesAction.Filter(PlaceFilter.UNCONFIRMED)) })
        FilterChip(stringResource(R.string.mp_filter_visited), state.filter == PlaceFilter.VISITED, { onAction(MyPlacesAction.Filter(PlaceFilter.VISITED)) })
        Spacer(Modifier.width(12.dp))
        FilterChip(stringResource(R.string.mp_sort_near), state.sort == PlaceSort.NEAR, { onAction(MyPlacesAction.Sort(PlaceSort.NEAR)) })
        FilterChip(stringResource(R.string.mp_sort_recent), state.sort == PlaceSort.RECENT, { onAction(MyPlacesAction.Sort(PlaceSort.RECENT)) })
    }
}

@Composable
private fun Empty(state: MyPlacesUiState, onAction: (MyPlacesAction) -> Unit) {
    val portrait = LocalPortrait.current
    Row(Modifier.fillMaxSize().padding(32.dp)) {
        Column(Modifier.weight(1f).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.mp_empty_title), style = NpType.question, color = Np.colors.text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.mp_empty_sub), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            Text(stringResource(R.string.mp_empty_example), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            SpeakButton({ onAction(MyPlacesAction.Speak) }, size = 200.dp, label = stringResource(R.string.mp_speak))
        }
        if (!portrait) {
            Spacer(Modifier.width(24.dp))
            Box(Modifier.weight(1f).fillMaxSize()) { PinMap(emptyList(), state.me, Modifier.fillMaxSize()) }
        }
    }
}
