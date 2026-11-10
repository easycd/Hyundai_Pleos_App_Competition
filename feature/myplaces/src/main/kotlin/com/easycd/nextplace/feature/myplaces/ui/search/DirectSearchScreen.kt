package com.easycd.nextplace.feature.myplaces.ui.search

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R
import com.easycd.nextplace.feature.myplaces.ui.common.CandidateRow

/** S-09b 가게 이름으로 찾기: 검색창(그때 위치 2km 안) · 결과 목록(페이드 교체) · 이걸로 저장. 화면이 열리면 키보드가 올라온다. */
@Composable
fun DirectSearchScreen(state: DirectSearchUiState, onAction: (DirectSearchAction) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    ScreenFrame(stringResource(R.string.ds_title), onBack = { onAction(DirectSearchAction.Back) }) {
        Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 40.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onAction(DirectSearchAction.Query(it)) },
                placeholder = { Text(stringResource(R.string.search_hint), style = NpType.body) },
                leadingIcon = { Icon(NpIcons.Search, null, modifier = Modifier.size(36.dp)) },
                trailingIcon = { Text(stringResource(R.string.ds_scope), style = NpType.small, color = Np.colors.muted, modifier = Modifier.padding(end = 24.dp)) },
                textStyle = NpType.body.copy(color = Np.colors.text),
                singleLine = true,
                shape = RoundedCornerShape(44.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Np.colors.brand, cursorColor = Np.colors.brand),
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).focusRequester(focus),
            )
            Spacer(Modifier.height(16.dp))
            Crossfade(state.results, Modifier.weight(1f, fill = false), animationSpec = tween(Motion.SCRIM_MS), label = "results") { list ->
                if (list.isEmpty() && state.query.isNotBlank() && !state.loading) {
                    Text(
                        stringResource(if (state.offline) R.string.search_offline else R.string.search_empty),
                        style = NpType.body,
                        color = Np.colors.muted,
                        modifier = Modifier.padding(24.dp),
                    )
                } else {
                    LazyColumn {
                        itemsIndexed(list) { i, c -> CandidateRow(c, i == state.selected, searchStyle = true) { onAction(DirectSearchAction.Pick(i)) } }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.End) {
                PillButton(
                    stringResource(R.string.resolve_save),
                    { onAction(DirectSearchAction.Save) },
                    Modifier.widthIn(min = 300.dp),
                    kind = PillKind.PRIMARY,
                    icon = NpIcons.Check,
                    enabled = state.canSave,
                )
            }
        }
    }
}
