package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border

/** 카드: 유리, 모서리 36dp, 안쪽 여백 32dp (화면 설계서 4장). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    highlight: Color? = null,
    padding: Dp = 32.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(36.dp)
    var m = modifier.glass(shape, strong)
    if (highlight != null) m = m.border(3.dp, highlight, shape)
    if (onClick != null) m = m.pressable(onClick = onClick)
    Box(m.padding(padding), content = content)
}
