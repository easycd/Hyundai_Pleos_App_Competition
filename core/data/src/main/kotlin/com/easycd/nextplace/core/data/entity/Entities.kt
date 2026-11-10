package com.easycd.nextplace.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** place 테이블 (기술서 8장). cues·candidates 는 JSON 문자열, 격자 컬럼은 0.01도 칸. */
@Entity(
    tableName = "place",
    indices = [
        Index("status"),
        Index("deletedAt"),
        Index("gridLat", "gridLon"),
        Index(value = ["provider", "providerId"], unique = true),
    ],
)
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val category: String?,
    val address: String?,
    val lat: Double?,
    val lon: Double?,
    val gridLat: Int?,
    val gridLon: Int?,
    val locationQuality: String,
    val provider: String?,
    val providerId: String?,
    val status: String,
    val source: String,
    val draft: Boolean,
    val utterance: String?,
    val cuesJson: String,
    val memo: String?,
    val savedAt: Long,
    val savedLat: Double?,
    val savedLon: Double?,
    val savedHeading: Double?,
    val candidatesJson: String,
    val remindEnabled: Boolean,
    val lastRemindedAt: Long?,
    val deletedAt: Long?,
    val candidatesFoundLater: Boolean,
)

@Entity(
    tableName = "visit",
    foreignKeys = [ForeignKey(PlaceEntity::class, ["id"], ["placeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("placeId"), Index("arrivedAt")],
)
data class VisitEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val arrivedAt: Long,
    val leftAt: Long?,
    val verified: Boolean,
    val note: String?,
    val rating: Int?,
)

/** 항상 1행 이하 (기술서 8장). */
@Entity(tableName = "visit_session")
data class VisitSessionEntity(
    @PrimaryKey val placeId: String,
    val state: String,
    val startedAt: Long,
    val updatedAt: Long,
    val arrivedAt: Long?,
    val stayStartedAt: Long?,
    val lastInsideAt: Long?,
    val outsideSince: Long?,
    val visitId: String?,
)

@Entity(tableName = "share_list")
data class ShareListEntity(
    @PrimaryKey val id: String,
    val title: String,
    val placesJson: String,
    val createdAt: Long,
)

/** 열쇠 = 엔드포인트·정규화 검색어·업종 코드·반경·100m 칸 (기술서 9장). */
@Entity(tableName = "poi_cache", indices = [Index("savedAt")])
data class PoiCacheEntity(
    @PrimaryKey val key: String,
    val json: String,
    val savedAt: Long,
)

/** 가게 + 방문 요약 (목록·추천용). */
data class PlaceRow(
    @Embedded val place: PlaceEntity,
    @ColumnInfo(name = "visitCount") val visitCount: Int,
    @ColumnInfo(name = "lastVisitAt") val lastVisitAt: Long?,
    @ColumnInfo(name = "notes") val notes: String?,
)
