package com.easycd.nextplace.feature.voicesave.ui.listen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

/** 어느 화면 위에도 겹쳐 뜨는 음성 레이어 (S-02a/b/c, S-14). 뒤 화면은 흐리게만 하고 바꾸지 않는다. */
@Composable
fun ListenOverlay(state: ListenUiState, onAction: (ListenAction) -> Unit) {
    val portrait = LocalPortrait.current
    ScrimOverlay(state.phase != ListenPhase.HIDDEN) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (portrait) Arrangement.SpaceBetween else Arrangement.Center,
        ) {
            if (portrait) Spacer(Modifier.height(1.dp))
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = { fadeIn(tween(Motion.SCRIM_MS)).togetherWith(fadeOut(tween(Motion.SCRIM_MS))) },
                label = "phase",
            ) { phase ->
                GlassCard(Modifier.widthIn(max = 1000.dp).fillMaxWidth(), strong = true, padding = 48.dp) {
                    when (phase) {
                        ListenPhase.LISTENING -> Listening(state, onAction)
                        ListenPhase.UNDERSTANDING -> Understanding(state.text)
                        ListenPhase.RETRY -> Retry()
                        ListenPhase.SEARCHING -> Searching(state)
                        ListenPhase.HIDDEN -> Unit
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
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
        if (state.devInput) DevInput(onAction)
    }
}

/** 개발 빌드(가짜 SDK)에서만: 말할 문장을 글자로 넣는다. */
@Composable
private fun DevInput(onAction: (ListenAction) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.dev_input_hint), style = NpType.small) },
            textStyle = NpType.body,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onAction(ListenAction.DevSubmit(text)); text = "" }),
            modifier = Modifier.weight(1f).heightIn(min = 76.dp),
        )
        Spacer(Modifier.width(16.dp))
        PillButton(stringResource(R.string.dev_input_send), { onAction(ListenAction.DevSubmit(text)); text = "" }, height = 76.dp)
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
        Spacer(Modifier.height(24.dp))
        SoundBars(0f, Modifier.widthIn(max = 600.dp).fillMaxWidth().height(80.dp), color = Np.colors.muted, active = false)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.retry_sub), style = NpType.cardTitle, color = Np.colors.text)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.retry_example), style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Searching(state: ListenUiState) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpinnerRing(size = 56.dp)
            Spacer(Modifier.width(24.dp))
            Text(stringResource(R.string.search_title), style = NpType.question, color = Np.colors.text)
        }
        val pins = listOfNotNull(state.target?.let { MapPin("target", it, PinTone.CANDIDATE, selected = true) })
        if (pins.isNotEmpty() || state.current != null) {
            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().height(MAP_HEIGHT.dp)) { PinMap(pins, state.current, Modifier.fillMaxSize()) }
        }
        Spacer(Modifier.height(24.dp))
        Text("“${state.text}”", style = NpType.body, color = Np.colors.muted, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private const val SHAKE = 8f
private const val SHAKE_STEP_MS = 60
private const val MAP_HEIGHT = 300
