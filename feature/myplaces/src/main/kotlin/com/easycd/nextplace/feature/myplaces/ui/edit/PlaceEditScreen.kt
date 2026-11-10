package com.easycd.nextplace.feature.myplaces.ui.edit

import com.easycd.nextplace.core.ui.drive.LocalPortrait
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.FilterChip
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R

/**
 * S-08b 수정: 위 줄 이름(왼쪽) · 업종 칩(오른쪽), 다음 줄 내 메모, 아래 줄 후보 다시 고르기(왼쪽) … 취소 · 저장(오른쪽).
 * 그 아래는 시스템 키보드 자리(320dp). 방문 기록이 있으면 후보 다시 고르기는 흐리고 누를 수 없음.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceEditScreen(state: PlaceEditUiState, onAction: (PlaceEditAction) -> Unit) {
    val portrait = LocalPortrait.current
    ScreenFrame(stringResource(R.string.edit_title), onBack = { onAction(PlaceEditAction.Cancel) }) {
        Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 40.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            val name: @Composable (Modifier) -> Unit = { m -> Box(m) { Field(stringResource(R.string.edit_name), state.name, { onAction(PlaceEditAction.Name(it)) }, error = !state.canSave) } }
            val cats: @Composable (Modifier) -> Unit = { m ->
                Column(m) {
                    Text(stringResource(R.string.edit_category), style = NpType.small, color = Np.colors.muted)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.categories.forEach { c -> FilterChip(c, state.category == c, { onAction(PlaceEditAction.Category(c)) }) }
                    }
                }
            }
            if (portrait) {
                name(Modifier.fillMaxWidth())
                cats(Modifier.fillMaxWidth())
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    name(Modifier.weight(1f))
                    cats(Modifier.weight(1f))
                }
            }
            Field(stringResource(R.string.edit_memo), state.memo, { onAction(PlaceEditAction.Memo(it)) })
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PillButton(stringResource(R.string.edit_repick), { onAction(PlaceEditAction.Repick) }, enabled = state.canRepick, icon = NpIcons.Search)
                Spacer(Modifier.weight(1f))
                PillButton(stringResource(R.string.cancel), { onAction(PlaceEditAction.Cancel) }, Modifier.widthIn(min = 200.dp))
                Spacer(Modifier.width(24.dp))
                PillButton(stringResource(R.string.edit_save), { onAction(PlaceEditAction.Save) }, Modifier.widthIn(min = 260.dp), kind = PillKind.PRIMARY, icon = NpIcons.Check, enabled = state.canSave)
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, error: Boolean = false) {
    Column {
        Text(label, style = NpType.small, color = Np.colors.muted)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            textStyle = NpType.body.copy(color = Np.colors.text),
            singleLine = true,
            isError = error,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Np.colors.brand, cursorColor = Np.colors.brand),
            supportingText = if (error) ({ Text(stringResource(R.string.edit_name_required), style = NpType.small) }) else null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
        )
    }
}
