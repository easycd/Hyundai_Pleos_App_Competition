package com.easycd.nextplace.core.ui.nav

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import com.easycd.nextplace.core.domain.contract.Destination

/** 화면 경로 이름. feature 는 서로의 화면을 모르고 이 이름과 [Destination]만 쓴다. */
object Routes {
    const val HOME = "home"
    const val RECOMMEND = "recommend"
    const val MY_PLACES = "myplaces"
    const val PLACE = "place/{id}"
    const val RESOLVE = "resolve/{id}"
    const val EDIT = "edit/{id}"
    const val SEARCH = "search/{id}"
    const val SETTINGS = "settings"
    const val SHARE = "share?pre={pre}"
    const val IMPORT = "import?payload={payload}"
    const val ARG_ID = "id"
    const val ARG_PRE = "pre"
    const val ARG_PAYLOAD = "payload"

    fun of(d: Destination): String? = when (d) {
        Destination.Home -> HOME
        Destination.Recommend -> RECOMMEND
        Destination.MyPlaces -> MY_PLACES
        is Destination.PlaceDetail -> "place/${d.id}"
        is Destination.Resolve -> "resolve/${d.id}"
        is Destination.Edit -> "edit/${d.id}"
        is Destination.Search -> search(d.id)
        Destination.Settings -> SETTINGS
        is Destination.Share -> if (d.preselectId != null) "share?pre=${d.preselectId}" else "share"
        is Destination.Import -> "import?payload=${Uri.encode(d.payload)}"
        is Destination.PermissionNeeded, Destination.Back -> null
    }

    fun search(id: String) = "search/$id"
}

/** 기능 등록 (기술서 6장): 화면을 그래프에 올린다. app/registry/FeatureRegistry 가 손으로 쓴 목록으로 모은다. */
interface Feature {
    val name: String
    fun register(graph: NavGraphBuilder)
}
