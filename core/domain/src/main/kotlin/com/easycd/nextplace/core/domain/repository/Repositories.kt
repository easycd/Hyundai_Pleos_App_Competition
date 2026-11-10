package com.easycd.nextplace.core.domain.repository

import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.model.ShareList
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitSession
import kotlinx.coroutines.flow.Flow

/** 개수 요약 (홈 '내 맛집 12곳 · 미확인 3곳'). */
data class PlaceCounts(val total: Int, val unconfirmed: Int)

/** 가게 저장소 (인터페이스는 domain, 구현은 core/data). 조회는 삭제 예정·초안을 숨긴다. */
interface PlaceRepository {
    fun observeAll(): Flow<List<PlaceWithVisits>>
    fun observeCounts(): Flow<PlaceCounts>
    fun observeLatest(): Flow<PlaceWithVisits?>
    fun observe(id: String): Flow<PlaceWithVisits?>
    suspend fun get(id: String): Place?
    suspend fun getWithVisits(id: String): PlaceWithVisits?
    suspend fun insert(place: Place)
    suspend fun update(place: Place)
    suspend fun deleteNow(id: String)
    suspend fun findByProvider(provider: String, providerId: String): Place?
    suspend fun findSimilar(name: String, point: GeoPoint, withinM: Double): Place?
    /** 반경 안의 확인된 가게 (격자 이웃 칸 조회 + 실제 거리). */
    suspend fun nearbyConfirmed(center: GeoPoint, radiusM: Double): List<PlaceWithVisits>
    suspend fun allConfirmed(): List<PlaceWithVisits>
    /** [from]의 원문·단서를 [targetId]에 덧붙이고 [from]을 지운다 (한 트랜잭션, 기술서 8장 합치기). */
    suspend fun mergeInto(targetId: String, from: Place)
    suspend fun markDeleted(id: String, at: Long)
    suspend fun restore(id: String)
    suspend fun purgeMarkedDeleted()
    suspend fun staleDrafts(olderThan: Long): List<Place>
    suspend fun unconfirmedWithoutCandidates(): List<Place>
    /** 받은 목록 가져오기: 전부 성공하거나 전부 되돌린다. */
    suspend fun insertAll(places: List<Place>)
    suspend fun deleteEverything()
    suspend fun setLastReminded(id: String, at: Long)
}

interface VisitRepository {
    suspend fun insert(visit: Visit)
    suspend fun get(id: String): Visit?
    suspend fun setLeftAt(id: String, at: Long)
    suspend fun setNote(id: String, note: String)
    /** [since] 이후에 도착했거나 아직 머무는 중인 가장 최근 방문. */
    suspend fun latestSince(since: Long): Visit?
    suspend fun lastFor(placeId: String): Visit?
    suspend fun countFor(placeId: String): Int
}

interface VisitSessionRepository {
    fun observe(): Flow<VisitSession?>
    suspend fun get(): VisitSession?
    suspend fun save(session: VisitSession)
    suspend fun clear()
}

interface ShareRepository {
    suspend fun save(list: ShareList)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
