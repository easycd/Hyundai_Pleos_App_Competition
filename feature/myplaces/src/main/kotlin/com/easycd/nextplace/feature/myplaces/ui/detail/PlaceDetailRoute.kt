package com.easycd.nextplace.feature.myplaces.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate

@Composable
fun PlaceDetailRoute(vm: PlaceDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(state.name, onBack = { vm.onAction(PlaceDetailAction.Back) }) {
        PlaceDetailScreen(state, vm::onAction)
    }
}
