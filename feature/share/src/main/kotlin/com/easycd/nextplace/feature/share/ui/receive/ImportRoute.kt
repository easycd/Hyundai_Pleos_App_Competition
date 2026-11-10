package com.easycd.nextplace.feature.share.ui.receive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.share.R

@Composable
fun ImportRoute(vm: ImportViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    DriveGate(stringResource(R.string.import_title), onBack = { vm.onAction(ImportAction.Close) }) {
        ImportScreen(state, vm::onAction)
    }
}
