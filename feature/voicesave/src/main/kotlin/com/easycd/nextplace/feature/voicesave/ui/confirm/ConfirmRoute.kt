package com.easycd.nextplace.feature.voicesave.ui.confirm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConfirmRoute(vm: ConfirmViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ConfirmCard(state, vm::onAction)
}
