package com.easycd.nextplace.feature.myplaces.ui.common

import com.easycd.nextplace.core.ui.icon.NpIcons
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.ui.component.ChipTone
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.StatusChip
import com.easycd.nextplace.core.ui.component.pressable
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.myplaces.R

/** 목록 한 줄 (S-07): 이름·업종·거리·저장일 또는 방문 횟수·상태 칩. 미확인은 이름 뒤에 ?. */
data class PlaceRowModel(
    val id: String,
    val name: String?,
    val category: String?,
    val distanceM: Double?,
    val savedAt: Long,
    val visits: Int,
    val unconfirmed: Boolean,
    val found: Boolean,
)

fun PlaceWithVisits.toRow(distanceM: Double?) = PlaceRowModel(
    id = place.id,
    name = place.displayName,
    category = place.category,
    distanceM = distanceM,
    savedAt = place.savedAt,
    visits = visitCount,
    unconfirmed = place.status == PlaceStatus.UNCONFIRMED,
    found = place.candidatesFoundLater,
)

fun PlaceWithVisits.toPin(selected: Boolean): MapPin? = place.point?.let {
    val tone = when {
        place.status == PlaceStatus.UNCONFIRMED -> PinTone.UNCONFIRMED
        visitCount > 0 -> PinTone.VISITED
        else -> PinTone.SAVED
    }
    MapPin(place.id, it, tone, selected)
}

@Composable
fun PlaceRow(row: PlaceRowModel, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, fresh: Boolean = false) {
    val glow = remember { Animatable(if (fresh) FRESH_ALPHA else 0f) }
    LaunchedEffect(fresh) { if (fresh) glow.animateTo(0f, tween(FRESH_MS, easing = LinearOutSlowInEasing)) }
    val sel by animateColorAsState(if (selected) Np.colors.brand.copy(alpha = 0.16f) else Color.Transparent, tween(Motion.FAST_MS), label = "row")
    val bg = if (glow.value > 0f) Np.colors.brand.copy(alpha = glow.value) else sel
    Row(
        modifier.fillMaxWidth().heightIn(min = 104.dp).clip(RoundedCornerShape(24.dp)).background(bg)
            .pressable(onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(NpIcons.MapPin, null, tint = Np.colors.muted, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(row.name ?: stringResource(R.string.mp_unknown), style = NpType.cardTitle.copy(fontSize = NpType.title.fontSize), color = Np.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val second = if (row.visits > 0) stringResource(R.string.mp_visits, row.visits) else stringResource(R.string.mp_saved_on, TimeText.saved(row.savedAt))
            Text(listOfNotNull(row.category, row.distanceM?.let(DistanceText::of), second).joinToString(" · "), style = NpType.small, color = Np.colors.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(16.dp))
        // 상태 칩은 미확인 · 후보 찾음 · 방문만 (그냥 저장된 가게는 칩 없음, 설계서 S-07)
        val chip = when {
            row.unconfirmed && row.found -> stringResource(R.string.chip_found) to ChipTone.FOUND
            row.unconfirmed -> stringResource(R.string.chip_unconfirmed) to ChipTone.UNCONFIRMED
            row.visits > 0 -> stringResource(R.string.mp_chip_visited) to ChipTone.VISITED
            else -> null
        }
        chip?.let { (text, tone) -> StatusChip(text, tone) }
    }
}

private const val FRESH_ALPHA = 0.28f
private const val FRESH_MS = 1_200
