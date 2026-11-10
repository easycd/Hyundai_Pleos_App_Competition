package com.easycd.nextplace.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.R
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScrimOverlay
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType

/**
 * S-17 권한 없음 (폭 920dp 창). 무엇을 못 쓰는지 + 왜 필요한지, 설정에서 허용 · 닫기 · (마이크·음성 서비스 문제일 때) 위치만 저장.
 * 주행 중이면 설정 버튼 대신 "멈춘 뒤 설정에서 허용해 주세요". 음성 서비스 오류는 설정으로 고칠 수 없어 설정 버튼이 없다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionDeniedScreen(
    kind: PermissionKind?,
    restricted: Boolean,
    canSaveLocationOnly: Boolean,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    onSaveLocationOnly: () -> Unit,
) {
    // 닫히는 페이드 동안 마지막 내용을 유지한다.
    val last = remember { arrayOfNulls<PermissionKind>(1) }
    if (kind != null) last[0] = kind
    val shown = kind ?: last[0] ?: PermissionKind.MICROPHONE
    val (title, desc) = when (shown) {
        PermissionKind.MICROPHONE -> R.string.perm_mic_title to R.string.perm_mic_desc
        PermissionKind.VOICE_SERVICE -> R.string.perm_voice_title to R.string.perm_voice_desc
        PermissionKind.LOCATION -> R.string.perm_location_title to R.string.perm_location_desc
        PermissionKind.NAVI -> R.string.perm_navi_title to R.string.perm_navi_desc
    }
    val fixable = shown != PermissionKind.VOICE_SERVICE
    ScrimOverlay(kind != null) {
        GlassCard(Modifier.widthIn(max = 920.dp).padding(horizontal = 24.dp), strong = true, padding = 48.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(NpIcons.Alert, null, tint = Np.colors.muted, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.size(20.dp))
                    Text(stringResource(title), style = NpType.cardTitle, color = Np.colors.text)
                }
                Text(stringResource(desc), style = NpType.body, color = Np.colors.muted)
                if (fixable && restricted) Text(stringResource(R.string.perm_stop_first), style = NpType.body, color = Np.colors.amber)
                FlowRow(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (canSaveLocationOnly) {
                        PillButton(stringResource(R.string.perm_location_only), onSaveLocationOnly, icon = NpIcons.MapPin)
                        Spacer(Modifier.weight(1f))
                    }
                    PillButton(stringResource(R.string.perm_close), onClose)
                    if (fixable && !restricted) {
                        PillButton(stringResource(R.string.perm_open_settings), onOpenSettings, kind = PillKind.PRIMARY, icon = NpIcons.Settings)
                    }
                }
            }
        }
    }
}
