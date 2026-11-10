package com.easycd.nextplace.feature.myplaces.ui.common

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

/** 후보 한 줄 (S-09 · S-09b): 라디오 + 이름 · 업종 · 거리 · 좌우 · 주소. */
@Composable
fun CandidateRow(c: PoiCandidate, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Np.colors.brand.copy(alpha = 0.14f) else Color.Transparent, tween(Motion.FAST_MS), label = "c")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 96.dp).clip(RoundedCornerShape(24.dp)).background(bg)
            .pressable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).border(3.dp, if (selected) Np.colors.brand else Np.colors.muted, CircleShape), contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(18.dp).clip(CircleShape).background(Np.colors.brand))
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(c.name, style = NpType.button, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val side = when (c.side) {
                Side.LEFT -> "왼쪽"
                Side.RIGHT -> "오른쪽"
                Side.UNKNOWN -> null
            }
            Text(
                listOfNotNull(c.category?.substringAfterLast(" > "), DistanceText.of(c.distanceM), side, c.address).joinToString(" · "),
                style = NpType.small,
                color = Np.colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
