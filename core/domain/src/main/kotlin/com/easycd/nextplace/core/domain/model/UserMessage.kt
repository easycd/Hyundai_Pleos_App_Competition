package com.easycd.nextplace.core.domain.model

/** 화면 아래 토스트 (화면 설계서 4장: 4초, 한 번에 1개). 문장은 core/ui 리소스에 있다. */
sealed interface UserMessage {
    data class Saved(val name: String) : UserMessage
    data class AlreadySaved(val name: String) : UserMessage
    data class SavedLocationOnly(val roadName: String?) : UserMessage
    data object SavedTextOnly : UserMessage
    data object SaveFailed : UserMessage
    data object Cancelled : UserMessage
    data object SayAgainLater : UserMessage
    data class Routing(val name: String, val changing: Boolean) : UserMessage
    data object RouteFailed : UserMessage
    data class Deleted(val name: String, val undo: () -> Unit) : UserMessage
    data object Edited : UserMessage
    data object ResolvedSaved : UserMessage
    data object Merged : UserMessage
    data class NoteSaved(val name: String, val memo: String) : UserMessage
    data class Imported(val count: Int) : UserMessage
    data object ImportFailed : UserMessage
    data object LinkCopied : UserMessage
    data object DriveLocked : UserMessage
    data object NoMore : UserMessage
}
