package com.easycd.nextplace.feature.visit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** app 이 모든 화면 위에 올리는 S-15 카드. */
@Composable
fun VisitNoteRoute(vm: VisitNoteViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    VisitNoteCard(state, vm::onAction)
}
