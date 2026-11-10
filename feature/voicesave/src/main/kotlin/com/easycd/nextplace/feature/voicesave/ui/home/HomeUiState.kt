package com.easycd.nextplace.feature.voicesave.ui.home

/** S-01 / S-01b 홈 상태. [latest] 가 null 이면 저장 전(S-01b). */
data class HomeUiState(
    val locationLabel: LocationLabel? = null,
    val total: Int = 0,
    val unconfirmed: Int = 0,
    val nearbyCount: Int = 0,
    val latest: LatestCard? = null,
    val simulated: Boolean = false,
) {
    val empty: Boolean get() = total == 0
}

/** 현재 위치: 도로 이름 + 방향. [road] 가 null 이면 "현재 위치". */
data class LocationLabel(val road: String?, val heading: String?)

enum class LatestChip { UNCONFIRMED, SAVED, FOUND, VISITED }

data class LatestCard(
    val id: String,
    val name: String?,
    val detail: String?,
    val noLocation: Boolean,
    val savedAt: Long,
    val chip: LatestChip,
    val visits: Int,
    val needsResolve: Boolean,
)
