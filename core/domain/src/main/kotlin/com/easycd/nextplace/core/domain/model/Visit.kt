package com.easycd.nextplace.core.domain.model

/** 방문 기록 (기획서 7장 Visit). */
data class Visit(
    val id: String,
    val placeId: String,
    val arrivedAt: Long,
    val leftAt: Long?,
    val verified: Boolean,
    val note: String?,
    val rating: Int? = null,
)

/** 방문 판정 진행 상태 (기획서 7장 VisitSession). 한 번에 1개만 있다. */
data class VisitSession(
    val placeId: String,
    val state: VisitState,
    val startedAt: Long,
    val updatedAt: Long,
    val arrivedAt: Long? = null,
    val stayStartedAt: Long? = null,
    val lastInsideAt: Long? = null,
    val outsideSince: Long? = null,
    val visitId: String? = null,
)

/** 보낸 공유 목록 (기획서 7장 ShareList). */
data class ShareList(
    val id: String,
    val title: String,
    val places: List<SharedPlace>,
    val createdAt: Long,
)

/** 공유 링크에 담는 가게 1곳. 방문 기록·차량 위치·발화 원문은 넣지 않는다. */
data class SharedPlace(
    val name: String,
    val category: String?,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val memo: String?,
    val provider: String?,
    val providerId: String?,
)
