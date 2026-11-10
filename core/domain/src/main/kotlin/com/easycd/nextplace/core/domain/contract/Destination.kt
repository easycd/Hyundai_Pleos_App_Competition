package com.easycd.nextplace.core.domain.contract

/** 화면 이동 요청. feature 는 다른 feature 화면을 모르고 이 값만 보낸다 (app 이 그래프에서 연결). */
sealed interface Destination {
    data object Home : Destination
    data object Recommend : Destination
    data object MyPlaces : Destination
    data class PlaceDetail(val id: String) : Destination
    data class Resolve(val id: String) : Destination
    data class Edit(val id: String) : Destination
    data class Search(val id: String) : Destination
    data object Settings : Destination
    data class Share(val preselectId: String? = null) : Destination
    data class Import(val payload: String) : Destination
    data class PermissionNeeded(val kind: PermissionKind) : Destination
    data object Back : Destination
}

enum class PermissionKind { MICROPHONE, VOICE_SERVICE, LOCATION, NAVI }
