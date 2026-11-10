package com.easycd.nextplace.feature.voicesave.ui

import androidx.compose.runtime.Composable
import com.easycd.nextplace.feature.voicesave.ui.confirm.ConfirmRoute
import com.easycd.nextplace.feature.voicesave.ui.listen.ListenRoute

/** app 이 모든 화면 위에 한 번 올리는 음성 저장 레이어 (S-02a~c, S-14, S-03/03b). */
@Composable
fun VoiceOverlayHost() {
    ConfirmRoute()
    ListenRoute()
}
