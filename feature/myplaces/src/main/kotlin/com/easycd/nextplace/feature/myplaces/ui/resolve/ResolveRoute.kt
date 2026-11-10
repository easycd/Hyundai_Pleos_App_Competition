package com.easycd.nextplace.feature.myplaces.ui.resolve

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.myplaces.R

@Composable
fun ResolveRoute(vm: ResolveViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(stringResource(R.string.resolve_title), onBack = { vm.onAction(ResolveAction.Back) }) {
        ResolveScreen(state, vm::onAction)
    }
}
