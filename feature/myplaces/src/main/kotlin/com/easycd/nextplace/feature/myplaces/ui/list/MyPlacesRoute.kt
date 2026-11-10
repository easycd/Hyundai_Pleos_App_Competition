package com.easycd.nextplace.feature.myplaces.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.myplaces.R

@Composable
fun MyPlacesRoute(vm: MyPlacesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(stringResource(R.string.mp_title), onBack = { vm.onAction(MyPlacesAction.Back) }) {
        MyPlacesScreen(state, vm::onAction)
    }
}
