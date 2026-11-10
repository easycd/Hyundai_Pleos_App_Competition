package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

enum class ChipTone { UNCONFIRMED, VISITED, SELECTED, SAVED, FOUND }

/** 상태 칩 48dp: 글자와 색을 같이 써서 색만으로 구분하지 않는다. */
@Composable
fun StatusChip(text: String, tone: ChipTone, modifier: Modifier = Modifier) {
    val color = toneColor(tone)
    Box(
        modifier.height(48.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)).padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = NpType.small.copy(fontWeight = FontWeight.Bold), color = color, maxLines = 1, softWrap = false) }
}

@Composable
fun toneColor(tone: ChipTone): Color {
    val c = Np.colors
    return when (tone) {
        ChipTone.UNCONFIRMED, ChipTone.FOUND -> c.amber
        ChipTone.VISITED -> c.green
        ChipTone.SELECTED -> c.brandText
        ChipTone.SAVED -> c.muted
    }
}

/** 누르는 필터 칩: 48dp 로 보이고 누르는 영역은 76dp. */
@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Np.colors
    Box(modifier.defaultMinSize(minHeight = 76.dp).pressable(onClick = onClick), contentAlignment = Alignment.Center) {
        val shape = CircleShape
        val chip = if (selected) Modifier.clip(shape).background(c.brand) else Modifier.glass(shape, elevation = 0.dp).border(1.dp, c.glassBorder, shape)
        Box(chip.height(48.dp).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
            Text(text, style = NpType.small.copy(fontWeight = FontWeight.Bold), color = if (selected) Color.White else c.text, maxLines = 1, softWrap = false)
        }
    }
}
