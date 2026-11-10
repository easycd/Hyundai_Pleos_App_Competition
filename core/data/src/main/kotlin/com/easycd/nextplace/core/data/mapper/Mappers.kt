package com.easycd.nextplace.core.data.mapper

import com.easycd.nextplace.core.data.entity.PlaceEntity
import com.easycd.nextplace.core.data.entity.PlaceRow
import com.easycd.nextplace.core.data.entity.VisitEntity
import com.easycd.nextplace.core.data.entity.VisitSessionEntity
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState

internal fun PlaceEntity.toModel() = Place(
    id = id, name = name, category = category, address = address, lat = lat, lon = lon,
    locationQuality = enumOr(locationQuality, LocationQuality.NONE),
    provider = provider, providerId = providerId,
    status = enumOr(status, PlaceStatus.UNCONFIRMED),
    source = enumOr(source, PlaceSource.VOICE),
    draft = draft, utterance = utterance, cues = DbJson.cues(cuesJson), memo = memo,
    savedAt = savedAt, savedLat = savedLat, savedLon = savedLon, savedHeading = savedHeading,
    candidates = DbJson.candidates(candidatesJson), remindEnabled = remindEnabled,
    lastRemindedAt = lastRemindedAt, deletedAt = deletedAt, candidatesFoundLater = candidatesFoundLater,
)

internal fun Place.toEntity() = PlaceEntity(
    id = id, name = name, category = category, address = address, lat = lat, lon = lon,
    gridLat = lat?.let(Geo::cell), gridLon = lon?.let(Geo::cell),
    locationQuality = locationQuality.name, provider = provider, providerId = providerId,
    status = status.name, source = source.name, draft = draft, utterance = utterance,
    cuesJson = DbJson.cues(cues), memo = memo, savedAt = savedAt,
    savedLat = savedLat, savedLon = savedLon, savedHeading = savedHeading,
    candidatesJson = DbJson.candidates(candidates), remindEnabled = remindEnabled,
    lastRemindedAt = lastRemindedAt, deletedAt = deletedAt, candidatesFoundLater = candidatesFoundLater,
)

internal fun PlaceRow.toModel(notes: List<String>? = null) = PlaceWithVisits(
    place = place.toModel(),
    visitCount = visitCount,
    lastVisitAt = lastVisitAt,
    visitNotes = notes ?: this.notes?.split('\n')?.filter { it.isNotBlank() }.orEmpty(),
)

internal fun VisitEntity.toModel() = Visit(id, placeId, arrivedAt, leftAt, verified, note, rating)
internal fun Visit.toEntity() = VisitEntity(id, placeId, arrivedAt, leftAt, verified, note, rating)

internal fun VisitSessionEntity.toModel() = VisitSession(
    placeId, enumOr(state, VisitState.ROUTING), startedAt, updatedAt, arrivedAt, stayStartedAt, lastInsideAt, outsideSince, visitId,
)
internal fun VisitSession.toEntity() = VisitSessionEntity(
    placeId, state.name, startedAt, updatedAt, arrivedAt, stayStartedAt, lastInsideAt, outsideSince, visitId,
)

private inline fun <reified E : Enum<E>> enumOr(value: String, fallback: E): E =
    runCatching { enumValueOf<E>(value) }.getOrDefault(fallback)
