package com.easycd.nextplace.feature.visit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.ui.component.AlertCard
import com.easycd.nextplace.core.ui.component.AlertHost
import com.easycd.nextplace.core.ui.component.ChipTone
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.StatusChip
import com.easycd.nextplace.core.ui.component.rememberRemaining
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.visit.R
import com.easycd.nextplace.feature.visit.note.VisitNoteState

/** S-15 방문 메모: 가게 이름 · 방문 횟수 · 질문, 다음에(왼쪽) · 말하기(오른쪽), 남은 시간 막대 10초. */
@Composable
fun VisitNoteCard(state: VisitNoteUiState, onAction: (VisitNoteAction) -> Unit) {
    val s = state.note as? VisitNoteState.Asking
    AlertHost(s != null) {
        if (s == null) return@AlertHost
        val remaining = rememberRemaining(s.waitStartedAt, Tunables.REMIND_BUTTON_WAIT_MS)
        AlertCard(remaining) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.visit_title, s.placeName), style = NpType.cardTitle, color = Np.colors.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                StatusChip(stringResource(R.string.visit_count, s.visitCount), ChipTone.VISITED)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (s.partial.isNotBlank()) "“${s.partial}”" else stringResource(R.string.visit_question),
                style = NpType.body,
                color = if (s.partial.isNotBlank()) Np.colors.text else Np.colors.muted,
                maxLines = 2,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PillButton(stringResource(R.string.visit_later), { onAction(VisitNoteAction.Later) }, Modifier.weight(1f))
                PillButton(stringResource(R.string.visit_speak), { onAction(VisitNoteAction.Speak) }, Modifier.weight(1f), kind = PillKind.PRIMARY, icon = NpIcons.Mic)
            }
            Spacer(Modifier.width(1.dp))
        }
    }
}
