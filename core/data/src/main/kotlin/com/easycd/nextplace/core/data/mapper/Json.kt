package com.easycd.nextplace.core.data.mapper

import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.DirectionCue
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.core.domain.model.Side
import com.easycd.nextplace.core.domain.model.TimeCue
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** JSON 컬럼 변환 (cues · candidates · share places). 알 수 없는 키는 무시해 필드 추가에 견딘다. */
internal object DbJson {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    fun cues(c: Cues): String = json.encodeToString(CuesDto.serializer(), CuesDto.from(c))
    fun cues(s: String): Cues = runCatching { json.decodeFromString(CuesDto.serializer(), s).toModel() }.getOrDefault(Cues())

    fun candidates(list: List<PoiCandidate>): String =
        json.encodeToString(ListSerializer(CandidateDto.serializer()), list.map(CandidateDto::from))
    fun candidates(s: String): List<PoiCandidate> =
        runCatching { json.decodeFromString(ListSerializer(CandidateDto.serializer()), s).map { it.toModel() } }.getOrDefault(emptyList())

    fun shared(list: List<SharedPlace>): String =
        json.encodeToString(ListSerializer(SharedDto.serializer()), list.map(SharedDto::from))
}

@Serializable
internal data class CuesDto(
    val category: String? = null,
    val time: String = TimeCue.NONE.name,
    val direction: String = DirectionCue.BOTH.name,
    val appearance: List<String> = emptyList(),
    val nameHint: String? = null,
    val memo: String? = null,
    val extra: List<String> = emptyList(),
) {
    fun toModel() = Cues(
        category = category,
        time = runCatching { TimeCue.valueOf(time) }.getOrDefault(TimeCue.NONE),
        direction = runCatching { DirectionCue.valueOf(direction) }.getOrDefault(DirectionCue.BOTH),
        appearance = appearance,
        nameHint = nameHint,
        memo = memo,
        extraUtterances = extra,
    )

    companion object {
        fun from(c: Cues) = CuesDto(c.category, c.time.name, c.direction.name, c.appearance, c.nameHint, c.memo, c.extraUtterances)
    }
}

@Serializable
internal data class CandidateDto(
    val provider: String,
    val id: String,
    val name: String,
    val category: String? = null,
    val address: String? = null,
    val lat: Double,
    val lon: Double,
    val dist: Double = 0.0,
    val side: String = Side.UNKNOWN.name,
    val score: Double = 0.0,
) {
    fun toModel() = PoiCandidate(
        provider, id, name, category, address, lat, lon, dist,
        runCatching { Side.valueOf(side) }.getOrDefault(Side.UNKNOWN), score,
    )

    companion object {
        fun from(c: PoiCandidate) = CandidateDto(c.provider, c.providerId, c.name, c.category, c.address, c.lat, c.lon, c.distanceM, c.side.name, c.score)
    }
}

@Serializable
internal data class SharedDto(
    val name: String,
    val category: String? = null,
    val address: String? = null,
    val lat: Double,
    val lon: Double,
    val memo: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
) {
    companion object {
        fun from(p: SharedPlace) = SharedDto(p.name, p.category, p.address, p.lat, p.lon, p.memo, p.provider, p.providerId)
    }
}
