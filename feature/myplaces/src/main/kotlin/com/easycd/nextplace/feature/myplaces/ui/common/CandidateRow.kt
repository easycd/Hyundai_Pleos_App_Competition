package com.easycd.nextplace.feature.myplaces.ui.common

import com.easycd.nextplace.core.ui.icon.NpIcons
import androidx.compose.material3.Icon
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.model.Side
import com.easycd.nextplace.core.ui.component.pressable
import com.easycd.nextplace.core.ui.text.DistanceText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/**
 * 후보 한 줄. S-09: 라디오(왼쪽) + 이름 / "고깃집 · 80m 오른쪽".
 * S-09b([searchStyle]): 핀(왼쪽) + 이름 / "고깃집 · 강남대로 123" + 거리 · 라디오(오른쪽).
 */
@Composable
fun CandidateRow(c: PoiCandidate, selected: Boolean, showAddress: Boolean = false, searchStyle: Boolean = false, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Np.colors.brand.copy(alpha = 0.14f) else Color.Transparent, tween(Motion.FAST_MS), label = "c")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 104.dp).clip(RoundedCornerShape(24.dp)).background(bg)
            .pressable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (searchStyle) {
            Icon(NpIcons.MapPin, null, tint = Np.colors.muted, modifier = Modifier.size(36.dp))
        } else {
            Radio(selected)
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(c.name, style = NpType.button, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val side = when (c.side) {
                Side.LEFT -> "왼쪽"
                Side.RIGHT -> "오른쪽"
                Side.UNKNOWN -> null
            }
            val cat = c.category?.substringAfterLast(" > ")
            val meta = if (searchStyle) {
                listOfNotNull(cat, c.address).joinToString(" · ")
            } else {
                listOfNotNull(cat, listOfNotNull(DistanceText.of(c.distanceM), side).joinToString(" "), c.address?.takeIf { showAddress }).joinToString(" · ")
            }
            Text(meta, style = NpType.small, color = Np.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (searchStyle) {
            Spacer(Modifier.width(16.dp))
            Text(DistanceText.of(c.distanceM), style = NpType.small, color = Np.colors.muted)
            Spacer(Modifier.width(20.dp))
            Radio(selected)
        }
    }
}

@Composable
private fun Radio(selected: Boolean) {
    Box(Modifier.size(44.dp).clip(CircleShape).border(3.dp, if (selected) Np.colors.brand else Np.colors.muted, CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(24.dp).clip(CircleShape).background(Np.colors.brand))
    }
}
