package com.easycd.nextplace.feature.share.ui.send

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PillKind
import com.easycd.nextplace.core.ui.component.ScreenFrame
import com.easycd.nextplace.core.ui.component.pressable
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.share.R

/** S-10 맛집 지도 공유: 가게 고르기(체크) · 제목 · QR · 링크 복사 / 보내기 · 받는 사람 안내. */
@Composable
fun ShareScreen(state: ShareUiState, onAction: (ShareAction) -> Unit) {
    val portrait = LocalPortrait.current
    ScreenFrame(stringResource(R.string.share_title), onBack = { onAction(ShareAction.Back) }) {
        if (state.loaded && state.rows.isEmpty()) {
            Text(stringResource(R.string.share_none), style = NpType.cardTitle, color = Np.colors.muted, modifier = Modifier.padding(40.dp))
            return@ScreenFrame
        }
        Row(Modifier.fillMaxSize().imePadding().padding(horizontal = 32.dp, vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Check(state.rows.isNotEmpty() && state.rows.all { it.checked }) { onAction(ShareAction.ToggleAll) }
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.share_select_all), style = NpType.button, color = Np.colors.text)
                    Spacer(Modifier.weight(1f))
                    Text(stringResource(R.string.share_count, state.selectedCount), style = NpType.small, color = Np.colors.muted)
                }
                LazyColumn(Modifier.weight(1f)) {
                    items(state.rows, key = { it.id }) { r ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 96.dp).pressable(role = Role.Checkbox) { onAction(ShareAction.Toggle(r.id)) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Check(r.checked) { onAction(ShareAction.Toggle(r.id)) }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(r.name, style = NpType.button, color = Np.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                r.category?.let { Text(it, style = NpType.small, color = Np.colors.muted) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(32.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onAction(ShareAction.Title(it)) },
                    label = { Text(stringResource(R.string.share_list_title), style = NpType.small) },
                    textStyle = NpType.body.copy(color = Np.colors.text),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Np.colors.brand, cursorColor = Np.colors.brand),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!portrait) Qr(state)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Crossfade(state.copied, Modifier.weight(1f), animationSpec = tween(Motion.SCRIM_MS), label = "copy") { copied ->
                        PillButton(
                            stringResource(if (copied) R.string.share_copied else R.string.share_copy),
                            { onAction(ShareAction.Copy) },
                            Modifier.fillMaxWidth(),
                            icon = if (copied) NpIcons.Check else NpIcons.Link,
                            enabled = state.link != null,
                        )
                    }
                    PillButton(stringResource(R.string.share_send), { onAction(ShareAction.Send) }, Modifier.weight(1f), kind = PillKind.PRIMARY, icon = NpIcons.Send, enabled = state.link != null)
                }
                Text(stringResource(R.string.share_receivers), style = NpType.small, color = Np.colors.muted)
                Text(stringResource(R.string.share_notice), style = NpType.small, color = Np.colors.muted)
            }
        }
    }
}

@Composable
private fun Qr(state: ShareUiState) {
    GlassCard(Modifier.fillMaxWidth(), padding = 24.dp) {
        Crossfade(state.qr, Modifier.fillMaxWidth(), animationSpec = tween(Motion.BASE_MS), label = "qr") { qr ->
            Box(Modifier.fillMaxWidth().height(QR_DP.dp), contentAlignment = Alignment.Center) {
                when {
                    qr != null -> Image(qr.asImageBitmap(), null, Modifier.size(QR_DP.dp).clip(RoundedCornerShape(16.dp)).background(Color.White))
                    state.link != null -> Text(stringResource(R.string.share_qr_too_long), style = NpType.body, color = Np.colors.muted)
                }
            }
        }
    }
}

@Composable
private fun Check(checked: Boolean, onClick: () -> Unit) {
    val c = Np.colors
    Box(
        Modifier.size(76.dp).pressable(role = Role.Checkbox, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (checked) c.brand else Color.Transparent)
                .border(3.dp, if (checked) c.brand else c.muted, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) { if (checked) Icon(NpIcons.Check, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
    }
}

private const val QR_DP = 280
