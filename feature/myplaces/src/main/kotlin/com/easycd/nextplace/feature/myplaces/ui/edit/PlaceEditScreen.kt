package com.easycd.nextplace.feature.myplaces.ui.edit

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

/** S-08b 가게 수정: 이름 · 업종 칩 · 내 메모 · 후보 다시 고르기 · 취소(왼쪽) · 저장(오른쪽). 키보드는 정차 중에만. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceEditScreen(state: PlaceEditUiState, onAction: (PlaceEditAction) -> Unit) {
    ScreenFrame(stringResource(R.string.edit_title), onBack = { onAction(PlaceEditAction.Cancel) }) {
        Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 40.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Field(stringResource(R.string.edit_name), state.name, { onAction(PlaceEditAction.Name(it)) }, error = !state.canSave)
                Text(stringResource(R.string.edit_category), style = NpType.small, color = Np.colors.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.categories.forEach { c -> FilterChip(c, state.category == c, { onAction(PlaceEditAction.Category(c)) }) }
                }
                Field(stringResource(R.string.edit_memo), state.memo, { onAction(PlaceEditAction.Memo(it)) })
                PillButton(
                    stringResource(R.string.edit_repick),
                    { onAction(PlaceEditAction.Repick) },
                    enabled = state.canRepick,
                    icon = NpIcons.Search,
                )
                if (!state.canRepick) Text(stringResource(R.string.edit_repick_disabled), style = NpType.small, color = Np.colors.muted)
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PillButton(stringResource(R.string.cancel), { onAction(PlaceEditAction.Cancel) }, Modifier.weight(1f))
                PillButton(stringResource(R.string.edit_save), { onAction(PlaceEditAction.Save) }, Modifier.weight(1f), kind = PillKind.PRIMARY, enabled = state.canSave)
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
