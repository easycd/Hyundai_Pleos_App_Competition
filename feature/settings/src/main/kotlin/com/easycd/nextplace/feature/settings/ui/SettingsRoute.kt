package com.easycd.nextplace.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.settings.R

@Composable
fun SettingsRoute(vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(stringResource(R.string.settings_title), onBack = { vm.onAction(SettingsAction.Back) }) {
        SettingsScreen(state, vm::onAction)
    }
}
