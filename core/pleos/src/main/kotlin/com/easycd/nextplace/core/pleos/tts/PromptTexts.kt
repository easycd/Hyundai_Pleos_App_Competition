package com.easycd.nextplace.core.pleos.tts

import android.content.Context
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoicePrompt.NoneReason
import com.easycd.nextplace.core.domain.voice.VoicePrompt.SavedWhen
import com.easycd.nextplace.core.pleos.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** [VoicePrompt] → 실제 문장 (res/values/strings_voice.xml). 가게 이름은 12자까지만. */
class PromptTexts @Inject constructor(@ApplicationContext private val context: Context) {

    fun text(p: VoicePrompt): String = when (p) {
        is VoicePrompt.Confirm -> s(R.string.voice_confirm, n(p.name))
        is VoicePrompt.ConfirmSecond -> s(R.string.voice_confirm_second, n(p.name))
        VoicePrompt.Saved -> s(R.string.voice_saved)
        is VoicePrompt.SavedNamed -> s(R.string.voice_saved_named, n(p.name))
        VoicePrompt.AlreadySaved -> s(R.string.voice_already_saved)
        VoicePrompt.SavedLocationOnly -> s(R.string.voice_saved_location_only)
        VoicePrompt.SavedTextOnly -> s(R.string.voice_saved_text_only)
        VoicePrompt.SaveFailed -> s(R.string.voice_save_failed)
        VoicePrompt.Cancelled -> s(R.string.voice_cancelled)
        VoicePrompt.SayAgain -> s(R.string.voice_say_again)
        is VoicePrompt.RemindAsk -> remind(p)
        is VoicePrompt.RouteStart -> s(R.string.voice_route_start, Josa.ro(n(p.name)))
        is VoicePrompt.RouteChange -> s(R.string.voice_route_change, Josa.ro(n(p.name)))
        VoicePrompt.RouteFailed -> s(R.string.voice_route_failed)
        is VoicePrompt.RecommendList -> p.names.mapIndexed { i, name -> s(R.string.voice_recommend_item, i + 1, n(name)) }
            .joinToString(", ") + ". " + s(R.string.voice_recommend_tail)
        is VoicePrompt.RecommendNone -> none(p)
        is VoicePrompt.ChooseRange -> s(R.string.voice_choose_range, p.count)
        VoicePrompt.WhichNumber -> s(R.string.voice_which_number)
        VoicePrompt.SayNumber -> s(R.string.voice_say_number)
        VoicePrompt.NoMore -> s(R.string.voice_no_more)
        is VoicePrompt.VisitAsk -> s(R.string.voice_visit_ask, n(p.name))
        is VoicePrompt.NoteSaved -> s(R.string.voice_note_saved, Josa.e(n(p.name)))
    }

    private fun remind(p: VoicePrompt.RemindAsk): String {
        val whenText = s(
            when (p.savedWhen) {
                SavedWhen.TODAY -> R.string.voice_when_today
                SavedWhen.YESTERDAY -> R.string.voice_when_yesterday
                SavedWhen.THIS_WEEK -> R.string.voice_when_this_week
                SavedWhen.LAST_WEEK -> R.string.voice_when_last_week
                SavedWhen.THIS_MONTH -> R.string.voice_when_this_month
                SavedWhen.LONG_AGO -> R.string.voice_when_long_ago
                SavedWhen.SHARED -> R.string.voice_when_shared
            },
        )
        val subject = Josa.iGa(n(p.name))
        return if (p.ahead) {
            s(R.string.voice_remind_ahead, whenText, subject, distance(p.distanceM))
        } else {
            s(R.string.voice_remind_near, whenText, subject)
        }
    }

    private fun none(p: VoicePrompt.RecommendNone): String {
        val cat = p.category?.let { Josa.iGa(it) }
        return when (p.reason) {
            NoneReason.NONE_SAVED -> s(R.string.voice_none_saved)
            NoneReason.NONE_10KM -> if (cat != null) s(R.string.voice_none_10km_category, cat) else s(R.string.voice_none_10km)
            NoneReason.NONE_30KM -> if (cat != null) s(R.string.voice_none_30km_category, cat) else s(R.string.voice_none_30km)
        }
    }

    private fun distance(m: Int): String =
        if (m < KM) s(R.string.voice_meters, (m / ROUND) * ROUND) else s(R.string.voice_km, String.format("%.1f", m / KM.toDouble()))

    private fun n(name: String) = Josa.spokenName(name)
    private fun s(id: Int, vararg args: Any): String = context.getString(id, *args)

    private companion object {
        const val KM = 1_000
        const val ROUND = 10
    }
}
