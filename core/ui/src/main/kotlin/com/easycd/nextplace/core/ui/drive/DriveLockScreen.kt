package com.easycd.nextplace.core.ui.drive

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.R
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.SpeakButton
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/**
 * 정차 전용 화면(S-07 ~ S-11, S-16)을 감싼다 (F-14). 주행 제한 중이거나 상태를 모르면 S-12 를 대신 보이고,
 * 제한이 풀리면 원래 화면으로 페이드. 속도·브레이크로 직접 판단하지 않는다.
 */
@Composable
fun DriveGate(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    val restricted = LocalDriveRestricted.current.isRestricted()
    Crossfade(restricted, animationSpec = tween(if (restricted) Motion.SCRIM_MS else Motion.BASE_MS), label = "drive") { locked ->
        if (locked) DriveLockScreen(title, onBack) else content()
    }
}

/** S-12 주행 중 안내: 제목은 열려던 화면 이름, 말하기 버튼, 뒤로 = 홈. */
@Composable
fun DriveLockScreen(title: String, onBack: () -> Unit) {
    val speak = LocalSpeak.current
    val label = stringResource(R.string.ui_speak)
    ScreenFrame(title.ifBlank { stringResource(R.string.drive_title_fallback) }, onBack) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(NpIcons.Wheel, null, tint = Np.colors.brandText, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.drive_line1), style = NpType.cardTitle, color = Np.colors.text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.drive_line2), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            SpeakButton(speak, size = 200.dp, label = label)
        }
    }
}
