package com.easycd.nextplace.core.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.ui.R
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.text.messageText
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * 토스트: 아래쪽, 좌우 140dp, 높이 120dp, 4초, 한 번에 1개 (새 것이 오면 바꾼다).
 * 되돌리기 버튼은 삭제 뒤에만. 세로 창에서는 말하기 버튼을 가리지 않게 [bottomPadding]을 크게 준다.
 */
@Composable
fun ToastHost(messages: Flow<UserMessage>, bottomPadding: Dp, sidePadding: Dp, modifier: Modifier = Modifier) {
    var current by remember { mutableStateOf<Pair<Long, UserMessage>?>(null) }
    LaunchedEffect(messages) {
        var n = 0L
        messages.collect { current = (++n) to it }
    }
    val shown = current
    LaunchedEffect(shown?.first) {
        if (shown != null) {
            delay(Tunables.TOAST_MS)
            if (current?.first == shown.first) current = null
        }
    }
    val shift = with(LocalDensity.current) { Motion.driveShift.roundToPx() }
    Box(modifier.fillMaxSize().padding(start = sidePadding, end = sidePadding, bottom = bottomPadding), contentAlignment = Alignment.BottomCenter) {
        AnimatedContent(
            targetState = shown,
            transitionSpec = {
                (fadeIn(tween(Motion.BASE_MS)) + slideInVertically(tween(Motion.BASE_MS, easing = Motion.standard)) { shift })
                    .togetherWith(fadeOut(tween(Motion.BASE_MS)))
            },
            label = "toast",
        ) { item ->
            if (item != null) Toast(item.second) { current = null }
        }
    }
}

@Composable
private fun Toast(message: UserMessage, dismiss: () -> Unit) {
    val c = Np.colors
    GlassCard(Modifier.fillMaxWidth().heightIn(min = 120.dp), strong = true, padding = 28.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val icon = when (message) {
                is UserMessage.Saved, is UserMessage.AlreadySaved, UserMessage.ResolvedSaved, UserMessage.Merged,
                is UserMessage.NoteSaved, is UserMessage.Imported, UserMessage.Edited, UserMessage.LinkCopied,
                -> NpIcons.Check
                is UserMessage.SavedLocationOnly, UserMessage.SavedTextOnly -> NpIcons.MapPin
                is UserMessage.Routing -> NpIcons.Navigation
                is UserMessage.Deleted -> NpIcons.Trash
                else -> NpIcons.Alert
            }
            val tint = when (message) {
                is UserMessage.SavedLocationOnly, UserMessage.SavedTextOnly -> c.amber
                UserMessage.SaveFailed, UserMessage.RouteFailed, UserMessage.ImportFailed -> c.dangerText
                else -> c.brandText
            }
            Icon(icon, null, tint = tint, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(24.dp))
            Text(messageText(message), style = NpType.body, color = c.text, modifier = Modifier.weight(1f), maxLines = 2)
            if (message is UserMessage.Deleted) {
                Spacer(Modifier.width(16.dp))
                PillButton(
                    stringResource(R.string.ui_undo),
                    onClick = { message.undo(); dismiss() },
                    icon = NpIcons.Undo,
                    height = 76.dp,
                )
            }
        }
    }
}
