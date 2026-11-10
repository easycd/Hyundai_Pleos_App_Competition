package com.easycd.nextplace.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.R
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScrimOverlay
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/**
 * S-13 처음 실행 안내 (홈 위 겹침 카드 1장). 무엇이 차 안에만 남고 무엇에 인터넷을 쓰는지 + 허용 항목 3줄.
 * 시작하기 = 권한 창 순서대로 (MainActivity 가 요청), 나중에 = 권한 없이 홈.
 */
@Composable
fun OnboardingScreen(visible: Boolean, onStart: () -> Unit, onLater: () -> Unit) {
    val portrait = LocalPortrait.current
    ScrimOverlay(visible) {
        GlassCard(Modifier.widthIn(max = 880.dp).padding(horizontal = 24.dp), strong = true, padding = 48.dp) {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(stringResource(R.string.onb_title), style = NpType.question, color = Np.colors.text)
                Text(stringResource(R.string.onb_desc), style = NpType.body, color = Np.colors.muted)
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Item(NpIcons.Mic, stringResource(R.string.onb_mic), stringResource(R.string.onb_mic_why), portrait)
                    Item(NpIcons.MapPin, stringResource(R.string.onb_location), stringResource(R.string.onb_location_why), portrait)
                    Item(NpIcons.Navigation, stringResource(R.string.onb_navi), stringResource(R.string.onb_navi_why), portrait)
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End)) {
                    PillButton(stringResource(R.string.onb_later), onLater, if (portrait) Modifier.weight(1f) else Modifier.widthIn(min = 240.dp))
                    PillButton(stringResource(R.string.onb_start), onStart, if (portrait) Modifier.weight(1f) else Modifier.widthIn(min = 360.dp), kind = PillKind.PRIMARY, height = 96.dp)
                }
            }
        }
    }
}

@Composable
private fun Item(icon: ImageVector, title: String, why: String, portrait: Boolean) {
    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Np.colors.brandText, modifier = Modifier.size(40.dp))
        Spacer(Modifier.size(20.dp))
        if (portrait) {
            Column {
                Text(title, style = NpType.button, color = Np.colors.text)
                Text(why, style = NpType.small, color = Np.colors.muted)
            }
        } else {
            Text(title, style = NpType.button, color = Np.colors.text)
            Spacer(Modifier.size(20.dp))
            Box(Modifier.weight(1f)) { Text(why, style = NpType.body, color = Np.colors.muted) }
        }
    }
}
