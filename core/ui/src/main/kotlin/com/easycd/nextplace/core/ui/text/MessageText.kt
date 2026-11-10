package com.easycd.nextplace.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.ui.R

/** 토스트 문장. 문장은 리소스 한 곳 (R8). */
@Composable
fun messageText(m: UserMessage): String = when (m) {
    is UserMessage.Saved -> stringResource(R.string.toast_saved, Josa.eulReul(m.name))
    is UserMessage.AlreadySaved -> stringResource(R.string.toast_already, m.name)
    is UserMessage.SavedLocationOnly -> m.roadName?.let { stringResource(R.string.toast_location_only, it) }
        ?: stringResource(R.string.toast_location_only_no_road)
    UserMessage.SavedTextOnly -> stringResource(R.string.toast_text_only)
    UserMessage.SaveFailed -> stringResource(R.string.toast_save_failed)
    UserMessage.Cancelled -> stringResource(R.string.toast_cancelled)
    UserMessage.SayAgainLater -> stringResource(R.string.toast_say_again_later)
    is UserMessage.Routing -> if (m.changing) {
        stringResource(R.string.toast_route_change, Josa.ro(m.name))
    } else {
        stringResource(R.string.toast_route, Josa.ro(m.name))
    }
    UserMessage.RouteFailed -> stringResource(R.string.toast_route_failed)
    is UserMessage.Deleted -> stringResource(R.string.toast_deleted, Josa.eulReul(m.name))
    UserMessage.Edited -> stringResource(R.string.toast_edited)
    UserMessage.ResolvedSaved -> stringResource(R.string.toast_resolved)
    UserMessage.Merged -> stringResource(R.string.toast_merged)
    is UserMessage.NoteSaved -> stringResource(R.string.toast_note, m.name, m.memo)
    is UserMessage.Imported -> stringResource(R.string.toast_imported, m.count)
    UserMessage.ImportFailed -> stringResource(R.string.toast_import_failed)
    UserMessage.LinkCopied -> stringResource(R.string.toast_link_copied)
    UserMessage.DriveLocked -> stringResource(R.string.toast_drive_locked)
    UserMessage.NoMore -> stringResource(R.string.toast_no_more)
}
