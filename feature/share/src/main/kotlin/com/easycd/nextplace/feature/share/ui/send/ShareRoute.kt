package com.easycd.nextplace.feature.share.ui.send

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easycd.nextplace.core.ui.drive.DriveGate
import com.easycd.nextplace.feature.share.R

/** 링크 복사(클립보드)와 보내기(차량의 다른 앱, ACTION_SEND)는 화면 쪽 동작이라 여기서 한다. */
@Composable
fun ShareRoute(vm: ShareViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooser = stringResource(R.string.share_chooser)
    DriveGate(stringResource(R.string.share_title), onBack = { vm.onAction(ShareAction.Back) }) {
        ShareScreen(state) { a ->
            val link = state.link
            when {
                a == ShareAction.Copy && link != null -> {
                    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(state.title, link))
                }
                a == ShareAction.Send && link != null -> send(context, state.title, link, chooser)
            }
            vm.onAction(a)
        }
    }
}

private fun send(context: Context, title: String, link: String, chooser: String) {
    val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, title).putExtra(Intent.EXTRA_TEXT, "$title\n$link")
    runCatching { context.startActivity(Intent.createChooser(i, chooser).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
