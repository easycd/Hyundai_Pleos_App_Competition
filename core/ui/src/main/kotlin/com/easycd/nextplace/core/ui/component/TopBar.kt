package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.R
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/** 상단 바 96dp: 뒤로(80) · 제목 36 · 오른쪽 동작 1개 (화면 설계서 4장). */
@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier, action: @Composable (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().height(96.dp).padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            RoundIconButton(NpIcons.Back, stringResource(R.string.ui_back), onBack, size = 80.dp)
            Spacer(Modifier.width(24.dp))
        }
        Text(title, style = NpType.title, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (action != null) action()
    }
}

/** 정차 화면 공통 틀: 단색 배경 + 상단 바 + 내용. */
@Composable
fun ScreenFrame(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().background(Np.colors.background).padding(top = 16.dp)) {
        TopBar(title, onBack, action = action)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, content = content)
        }
    }
}
