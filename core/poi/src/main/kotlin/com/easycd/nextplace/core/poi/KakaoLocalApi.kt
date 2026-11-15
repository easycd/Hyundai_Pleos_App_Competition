package com.easycd.nextplace.core.poi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Query

/** 카카오 로컬 REST API (기획서 9장 1안). 키는 OkHttp 인터셉터가 붙인다. */
internal interface KakaoLocalApi {
    @GET("v2/local/search/category.json")
    suspend fun category(
        @Query("category_group_code") group: String,
        @Query("x") lon: Double,
        @Query("y") lat: Double,
        @Query("radius") radius: Int,
        @Query("sort") sort: String = "distance",
        @Query("size") size: Int = 15,
    ): KakaoSearchResponse

    @GET("v2/local/search/keyword.json")
    suspend fun keyword(
        @Query("query") query: String,
        @Query("x") lon: Double,
        @Query("y") lat: Double,
        @Query("radius") radius: Int,
        @Query("category_group_code") group: String? = null,
        @Query("sort") sort: String = "distance",
        @Query("size") size: Int = 15,
    ): KakaoSearchResponse

    /** 기준 좌표 없이 전국에서 정확도 순 (예: "강남 할머니국밥"). */
    @GET("v2/local/search/keyword.json")
    suspend fun keywordAnywhere(
        @Query("query") query: String,
        @Query("sort") sort: String = "accuracy",
        @Query("size") size: Int = 15,
    ): KakaoSearchResponse

    /** 연결 미리 열기: 응답 본문은 쓰지 않는다 (검색 할당량을 쓰지 않는 HEAD). */
    @HEAD("/")
    suspend fun ping(): retrofit2.Response<Void>

    @GET("v2/local/geo/coord2address.json")
    suspend fun coord2address(@Query("x") lon: Double, @Query("y") lat: Double): KakaoAddressResponse
}

@Serializable
internal data class KakaoSearchResponse(val documents: List<KakaoPlace> = emptyList())

@Serializable
internal data class KakaoPlace(
    val id: String,
    @SerialName("place_name") val name: String,
    @SerialName("category_name") val category: String? = null,
    @SerialName("address_name") val address: String? = null,
    @SerialName("road_address_name") val roadAddress: String? = null,
    val x: String,
    val y: String,
)

@Serializable
internal data class KakaoAddressResponse(val documents: List<KakaoAddressDoc> = emptyList())

@Serializable
internal data class KakaoAddressDoc(
    @SerialName("road_address") val road: KakaoRoad? = null,
    val address: KakaoAddr? = null,
)

@Serializable
internal data class KakaoRoad(@SerialName("road_name") val roadName: String? = null)

@Serializable
internal data class KakaoAddr(@SerialName("region_3depth_name") val dong: String? = null)
