package com.easycd.nextplace.feature.voicesave.ui.listen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.component.GlassCard
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PillButton
import com.easycd.nextplace.core.ui.component.PinMap
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.ScrimOverlay
import com.easycd.nextplace.core.ui.component.SoundBars
import com.easycd.nextplace.core.ui.component.SpinnerRing
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.icon.NpIcons
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.voicesave.R

/**
 * 어느 화면 위에도 겹쳐 뜨는 음성 레이어 (S-02a/b/c). 뒤 화면은 흐리게만 하고 바꾸지 않는다.
 * 카드 최대 960×640dp, 좁은 창에서는 가장자리 24dp까지 (세로 창은 폭 가득) 안에 상태 문구 · 가운데 표시 · 문장 · 취소가 위에서 아래로 놓인다.
 * S-14 가게 찾는 중은 겹침이 아니라 화면 전체(문구 + 추정 위치 지도 + 말한 문장).
 */
@Composable
fun ListenOverlay(state: ListenUiState, onAction: (ListenAction) -> Unit) {
    val portrait = LocalPortrait.current
    val talking = state.phase == ListenPhase.LISTENING || state.phase == ListenPhase.UNDERSTANDING || state.phase == ListenPhase.RETRY
    ScrimOverlay(talking) {
        val size = if (portrait) Modifier.fillMaxWidth().padding(horizontal = 24.dp).heightIn(min = 640.dp) else Modifier.padding(24.dp).widthIn(max = CARD_W.dp).fillMaxWidth().heightIn(max = CARD_H.dp).fillMaxHeight()
        GlassCard(size, strong = true, padding = 48.dp) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = state.phase,
                    transitionSpec = { fadeIn(tween(Motion.SCRIM_MS)).togetherWith(fadeOut(tween(Motion.SCRIM_MS))) },
                    modifier = Modifier.weight(1f),
                    label = "phase",
                ) { phase ->
                    when (phase) {
                        ListenPhase.LISTENING -> Listening(state, onAction)
                        ListenPhase.UNDERSTANDING -> Understanding(state.text)
                        ListenPhase.RETRY -> Retry()
                        ListenPhase.SEARCHING, ListenPhase.HIDDEN -> Unit
                    }
                }
                PillButton(
                    stringResource(R.string.cancel),
                    { onAction(ListenAction.Cancel) },
                    Modifier.widthIn(min = 320.dp),
                    icon = NpIcons.Close,
                    height = if (portrait) 112.dp else 88.dp,
                )
            }
        }
    }
    AnimatedVisibility(state.phase == ListenPhase.SEARCHING, enter = fadeIn(tween(Motion.SCRIM_MS)), exit = fadeOut(tween(Motion.SCRIM_MS))) {
        Searching(state)
    }
}

@Composable
private fun Listening(state: ListenUiState, onAction: (ListenAction) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.listen_title), style = NpType.question, color = Np.colors.text)
        Spacer(Modifier.height(32.dp))
        SoundBars(state.level, Modifier.widthIn(max = 600.dp).fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(32.dp))
        Text(
            state.text.ifBlank { stringResource(R.string.listen_hint) },
            style = NpType.live,
            color = if (state.text.isBlank()) Np.colors.muted else Np.colors.text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (state.devInput) DevInput(state.devDraft, onAction)
    }
}

/** 개발 빌드(가짜 SDK)에서만: 말할 문장을 글자로 넣는다. */
@Composable
private fun DevInput(text: String, onAction: (ListenAction) -> Unit) {
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { onAction(ListenAction.DevDraft(it)) },
            placeholder = { Text(stringResource(R.string.dev_input_hint), style = NpType.small) },
            textStyle = NpType.body,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onAction(ListenAction.DevSubmit(text)) }),
            modifier = Modifier.weight(1f).heightIn(min = 76.dp),
        )
        Spacer(Modifier.width(16.dp))
        PillButton(stringResource(R.string.dev_input_send), { onAction(ListenAction.DevSubmit(text)) }, height = 76.dp)
    }
}

@Composable
private fun Understanding(text: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.understand_title), style = NpType.question, color = Np.colors.text)
        Spacer(Modifier.height(32.dp))
        SpinnerRing()
        Spacer(Modifier.height(32.dp))
        Text("“$text”", style = NpType.live, color = Np.colors.text, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Retry() {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        listOf(SHAKE, -SHAKE, SHAKE / 2, 0f).forEach { shake.animateTo(it, tween(SHAKE_STEP_MS)) }
    }
    Column(Modifier.fillMaxWidth().offset(x = shake.value.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.retry_title), style = NpType.question, color = Np.colors.text)
        Spacer(Modifier.height(32.dp))
        SoundBars(0f, Modifier.widthIn(max = 600.dp).fillMaxWidth().height(120.dp), color = Np.colors.muted, active = false)
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.retry_sub), style = NpType.live, color = Np.colors.muted, textAlign = TextAlign.Center)
        Text(stringResource(R.string.retry_example), style = NpType.live, color = Np.colors.text, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Searching(state: ListenUiState) {
    // S-14: 보통 1초 안에 지나가는 화면이라 글자는 두 줄뿐. 지도가 없으면(위치 못 잡음) 그 자리는 비움.
    Column(
        Modifier.fillMaxSize().background(Np.colors.background).padding(horizontal = 140.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpinnerRing(size = 72.dp)
            Spacer(Modifier.width(24.dp))
            Text(stringResource(R.string.search_title), style = NpType.question, color = Np.colors.text)
        }
        Spacer(Modifier.height(32.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            val pins = listOfNotNull(state.target?.let { MapPin("target", it, PinTone.CANDIDATE, selected = true) })
            if (pins.isNotEmpty() || state.current != null) {
                GlassCard(Modifier.fillMaxSize(), padding = 24.dp) {
                    PinMap(pins, state.current, Modifier.fillMaxSize())
                    Text(stringResource(R.string.search_map_label), style = NpType.small, color = Np.colors.muted, modifier = Modifier.align(Alignment.TopStart))
                    Text(stringResource(R.string.search_map_legend), style = NpType.small, color = Np.colors.muted, modifier = Modifier.align(Alignment.BottomStart))
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("“${state.text}”", style = NpType.live, color = Np.colors.muted, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private const val SHAKE = 8f
private const val SHAKE_STEP_MS = 60
private const val CARD_W = 960
private const val CARD_H = 640
