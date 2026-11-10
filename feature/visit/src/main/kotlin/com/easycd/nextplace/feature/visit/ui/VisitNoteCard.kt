package com.easycd.nextplace.feature.visit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.rememberRemaining
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.visit.R
import com.easycd.nextplace.feature.visit.note.VisitNoteState

/**
 * S-15 방문 메모: 깃발 · "○○ 다녀오셨네요" · "방문 n회로 기록했어요 · 한 줄 남길까요?" · 바로 말해도 된다는 힌트,
 * 오른쪽에 다음에 · 말하기(주황). 듣는 중이면 힌트 자리에 받아 적은 말. 남은 시간 막대 10초.
 */
@Composable
fun VisitNoteCard(state: VisitNoteUiState, onAction: (VisitNoteAction) -> Unit) {
    val s = state.note as? VisitNoteState.Asking
    val portrait = LocalPortrait.current
    AlertHost(s != null) {
        if (s == null) return@AlertHost
        val remaining = rememberRemaining(s.waitStartedAt, Tunables.REMIND_BUTTON_WAIT_MS)
        AlertCard(remaining) {
            val info: @Composable (Modifier) -> Unit = { m ->
                Row(m, verticalAlignment = Alignment.CenterVertically) {
                    Icon(NpIcons.Flag, null, tint = Np.colors.text, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.width(28.dp))
                    Column {
                        Text(stringResource(R.string.visit_title, s.placeName), style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.visit_sub, s.visitCount), style = NpType.body, color = Np.colors.text, maxLines = 2)
                        if (s.partial.isNotBlank()) {
                            Text("“${s.partial}”", style = NpType.body, color = Np.colors.brandText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else {
                            Text(stringResource(if (s.listening) R.string.visit_listening else R.string.visit_hint), style = NpType.small, color = Np.colors.muted)
                        }
                    }
                }
            }
            val later: @Composable (Modifier) -> Unit = { m -> PillButton(stringResource(R.string.visit_later), { onAction(VisitNoteAction.Later) }, m, height = 112.dp) }
            val speak: @Composable (Modifier) -> Unit = { m ->
                PillButton(stringResource(R.string.visit_speak), { onAction(VisitNoteAction.Speak) }, m, kind = PillKind.PRIMARY, icon = NpIcons.Mic, height = 112.dp)
            }
            if (portrait) {
                Column {
                    info(Modifier)
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        later(Modifier.weight(1f))
                        speak(Modifier.weight(1f))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    info(Modifier.weight(1f))
                    Spacer(Modifier.width(24.dp))
                    later(Modifier.width(200.dp))
                    Spacer(Modifier.width(32.dp))
                    speak(Modifier.width(300.dp))
                }
            }
        }
    }
}
