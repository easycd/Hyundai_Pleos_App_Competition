package com.easycd.nextplace.feature.myplaces.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.myplaces.R

@Composable
fun DirectSearchRoute(vm: DirectSearchViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(stringResource(R.string.search_title), onBack = { vm.onAction(DirectSearchAction.Back) }) {
        DirectSearchScreen(state, vm::onAction)
    }
}
