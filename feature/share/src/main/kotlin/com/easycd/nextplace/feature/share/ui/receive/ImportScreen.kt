package com.easycd.nextplace.feature.share.ui.receive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.component.ChipTone
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.PinMap
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.StatusChip
import com.easycd.nextplace.core.ui.component.pressable
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.share.R

/** S-16 받은 목록 가져오기: 제목·날짜, 체크 목록(이미 있음은 흐리게), 지도, 닫기(왼쪽) · 내 지도에 추가(오른쪽). */
@Composable
fun ImportScreen(state: ImportUiState, onAction: (ImportAction) -> Unit) {
    val portrait = LocalPortrait.current
    ScreenFrame(stringResource(R.string.import_title), onBack = { onAction(ImportAction.Close) }) {
        if (state.problem != ImportProblem.NONE) {
            Column(Modifier.fillMaxSize().padding(40.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(if (state.problem == ImportProblem.BROKEN) R.string.import_broken else R.string.import_too_new), style = NpType.question, color = Np.colors.text)
                Spacer(Modifier.height(32.dp))
                PillButton(stringResource(R.string.import_close), { onAction(ImportAction.Close) })
            }
            return@ScreenFrame
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 8.dp)) {
            Text(if (COUNTED.containsMatchIn(state.title)) state.title else stringResource(R.string.import_heading, state.title, state.rows.size), style = NpType.cardTitle, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val day = TimeText.monthDay(state.createdAt)
            Text(state.from?.let { stringResource(R.string.import_from, it, day) } ?: day, style = NpType.body, color = Np.colors.muted)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.weight(1f)) {
                LazyColumn(Modifier.weight(1f)) {
                    itemsIndexed(state.rows) { i, r ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 96.dp).alpha(if (r.exists) DIM else 1f)
                                .pressable(enabled = !r.exists, role = Role.Checkbox) { onAction(ImportAction.Toggle(i)) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.padding(end = 24.dp).size(56.dp).clip(RoundedCornerShape(14.dp)).background(if (r.checked) Np.colors.brand else Color.Transparent)
                                    .border(3.dp, if (r.checked) Np.colors.brand else Np.colors.muted, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) { if (r.checked) Icon(NpIcons.Check, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                            Column(Modifier.weight(1f)) {
                                Text(r.place.name, style = NpType.button, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(listOfNotNull(r.place.category, r.place.memo?.let { "“$it”" }).joinToString(" · "), style = NpType.small, color = Np.colors.muted, maxLines = 1)
                            }
                            if (r.exists) StatusChip(stringResource(R.string.import_exists), ChipTone.SAVED)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Np.colors.glassBorder))
                    }
                }
                if (!portrait) {
                    Spacer(Modifier.width(40.dp))
                    Box(Modifier.weight(MAP_WEIGHT).fillMaxHeight()) {
                        PinMap(state.rows.mapIndexed { i, r -> MapPin("$i", GeoPoint(r.place.lat, r.place.lon), PinTone.SAVED, r.checked) }, null, Modifier.fillMaxSize())
                        Text(stringResource(R.string.import_map), style = NpType.small, color = Np.colors.muted, modifier = Modifier.padding(20.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!portrait) {
                    Text(
                        if (state.existing > 0) stringResource(R.string.import_summary_skip, state.toAdd, state.existing) else stringResource(R.string.import_summary, state.toAdd),
                        style = NpType.body, color = Np.colors.muted, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                PillButton(stringResource(R.string.import_close), { onAction(ImportAction.Close) }, if (portrait) Modifier.weight(1f) else Modifier.width(200.dp), height = 112.dp)
                Spacer(Modifier.width(32.dp))
                PillButton(
                    stringResource(R.string.import_add), { onAction(ImportAction.Add) }, if (portrait) Modifier.weight(1f) else Modifier.width(400.dp),
                    kind = PillKind.PRIMARY, icon = NpIcons.Download, enabled = state.canAdd, height = 112.dp,
                )
            }
        }
    }
}

private const val MAP_WEIGHT = 0.6f

/** 제목에 이미 "N곳"이 있으면 개수를 다시 붙이지 않는다 ("내 맛집 3곳 3곳" 방지). */
private val COUNTED = Regex("""\d+\s*곳\s*$""")
private const val DIM = 0.45f
