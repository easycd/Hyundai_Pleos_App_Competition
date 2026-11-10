package com.easycd.nextplace.feature.remind.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** app 이 모든 화면 위에 올리는 S-05 / S-05b 카드. */
@Composable
fun RemindRoute(vm: RemindViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    RemindCard(state, vm::onAction)
}
