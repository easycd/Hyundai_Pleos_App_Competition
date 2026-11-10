package com.easycd.nextplace.core.domain.voice

/**
 * 음성 안내 문장 (기획서 6장 규칙 5: 문장은 한 곳에 모은다). 실제 문장은 core/pleos 의 res/values/strings_voice.xml.
 * 가게 이름은 [Josa.spokenName]으로 12자까지만 넣는다.
 */
sealed interface VoicePrompt {
    data class Confirm(val name: String) : VoicePrompt
    data class ConfirmSecond(val name: String) : VoicePrompt
    data object Saved : VoicePrompt
    data class SavedNamed(val name: String) : VoicePrompt
    data object AlreadySaved : VoicePrompt
    data object SavedLocationOnly : VoicePrompt
    data object SavedTextOnly : VoicePrompt
    data object SaveFailed : VoicePrompt
    data object Cancelled : VoicePrompt
    data object SayAgain : VoicePrompt
    data class RemindAsk(val name: String, val savedWhen: SavedWhen, val distanceM: Int, val ahead: Boolean) : VoicePrompt
    data class RouteStart(val name: String) : VoicePrompt
    data class RouteChange(val name: String) : VoicePrompt
    data object RouteFailed : VoicePrompt
    data class RecommendList(val names: List<String>) : VoicePrompt
    data class RecommendNone(val category: String?, val reason: NoneReason) : VoicePrompt
    data class ChooseRange(val count: Int) : VoicePrompt
    data object WhichNumber : VoicePrompt
    data object SayNumber : VoicePrompt
    data object NoMore : VoicePrompt
    data class VisitAsk(val name: String) : VoicePrompt
    data class NoteSaved(val name: String) : VoicePrompt

    enum class NoneReason { NONE_SAVED, NONE_10KM, NONE_30KM }

    /** '지난주에 저장한' 같은 저장 시기 표현. */
    enum class SavedWhen { TODAY, YESTERDAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, LONG_AGO, SHARED }
}
