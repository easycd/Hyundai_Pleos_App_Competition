package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

enum class PillKind { PRIMARY, SECONDARY, DANGER, DANGER_OUTLINE }

/** 알약형 버튼. 1순위는 주황 채움(화면에 1개), 2순위는 유리, 삭제는 빨강. 높이 88dp 기본. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: PillKind = PillKind.SECONDARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 88.dp,
) {
    val c = Np.colors
    val shape = CircleShape
    val base = modifier.heightIn(min = height).alpha(if (enabled) 1f else DISABLED)
    val styled = when (kind) {
        PillKind.PRIMARY -> base.shadow(16.dp, shape, ambientColor = c.brand, spotColor = c.brand).clip(shape).background(c.brand)
        PillKind.SECONDARY -> base.glass(shape)
        PillKind.DANGER -> base.clip(shape).background(c.danger)
        PillKind.DANGER_OUTLINE -> base.clip(shape).border(2.dp, c.dangerText, shape)
    }
    val content = when (kind) {
        PillKind.PRIMARY, PillKind.DANGER -> Color.White
        PillKind.SECONDARY -> c.text
        PillKind.DANGER_OUTLINE -> c.dangerText
    }
    Row(
        styled.pressable(enabled, onClick = onClick).padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = content, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(16.dp))
        }
        Text(text, style = NpType.button, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** 둥근 아이콘 버튼 (상단 바 · 설정 · 세로 창 안내 시작). 누르는 영역 80dp 이상. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    val c = Np.colors
    val m = if (primary) modifier.size(size).clip(CircleShape).background(c.brand) else modifier.size(size).glass(CircleShape)
    androidx.compose.foundation.layout.Box(
        m.alpha(if (enabled) 1f else DISABLED).pressable(enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = if (primary) Color.White else c.text, modifier = Modifier.size(size * ICON_RATIO))
    }
}

private const val DISABLED = 0.4f
private const val ICON_RATIO = 0.42f
