package com.easycd.nextplace.feature.voicesave.ui.listen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ListenRoute(vm: ListenViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ListenOverlay(state, vm::onAction)
}
