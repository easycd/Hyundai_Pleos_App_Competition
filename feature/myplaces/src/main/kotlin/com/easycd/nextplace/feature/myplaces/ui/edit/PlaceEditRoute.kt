package com.easycd.nextplace.feature.myplaces.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.myplaces.R

@Composable
fun PlaceEditRoute(vm: PlaceEditViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler { vm.onAction(PlaceEditAction.Cancel) }
    DriveGate(stringResource(R.string.edit_title), onBack = { vm.onAction(PlaceEditAction.Cancel) }) {
        PlaceEditScreen(state, vm::onAction)
    }
}
