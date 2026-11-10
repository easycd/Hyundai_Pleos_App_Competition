package com.easycd.nextplace.feature.share.codec

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.SharedPlace
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.math.roundToLong

/** 링크 안의 목록 (버전 1). 방문 기록·차량 위치·발화 원문은 넣지 않는다 (기획서 F-13). */
data class SharePayload(val title: String, val createdAt: Long, val places: List<SharedPlace>, val from: String? = null)

sealed interface DecodeResult {
    data class Ok(val payload: SharePayload) : DecodeResult
    data object Broken : DecodeResult
    data object TooNew : DecodeResult
}

/**
 * 공유 링크 코덱 (기술서 4장 ShareCodec): JSON → raw deflate → base64url (서버 없음).
 * 링크 형식 https://easycd.github.io/nextplace/s#<데이터>. 풀 때는 크기(압축 후 8KB, 푼 뒤 64KB)·버전·필드를 검사한다.
 * 보내는 쪽은 [fits]로 같은 기준을 미리 확인한다.
 */
object ShareCodec {
    const val VERSION = 1
    const val BASE_URL = "https://easycd.github.io/nextplace/s#"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; explicitNulls = false }

    fun link(payload: SharePayload): String = BASE_URL + encode(payload)

    /** 이 링크를 받는 쪽(같은 앱)이 읽을 수 있는가. 보내기 전에 왕복으로 확인한다. */
    fun fits(link: String): Boolean = decode(link) is DecodeResult.Ok

    fun encode(payload: SharePayload): String {
        val dto = Dto(
            v = VERSION,
            t = payload.title.take(Tunables.SHARE_TEXT_MAX),
            d = payload.createdAt,
            f = payload.from?.take(Tunables.SHARE_TEXT_MAX)?.takeIf { it.isNotBlank() },
            p = payload.places.take(Tunables.SHARE_MAX_PLACES).map {
                P(it.name.take(Tunables.SHARE_TEXT_MAX), it.category?.take(Tunables.SHARE_TEXT_MAX), it.address?.take(Tunables.SHARE_TEXT_MAX * 2), r6(it.lat), r6(it.lon), it.memo?.take(Tunables.SHARE_TEXT_MAX), it.provider, it.providerId)
            },
        )
        val raw = json.encodeToString(Dto.serializer(), dto).toByteArray(Charsets.UTF_8)
        val d = Deflater(Deflater.BEST_COMPRESSION, true).apply { setInput(raw); finish() }
        val out = ByteArrayOutputStream()
        val buf = ByteArray(BUF)
        while (!d.finished()) out.write(buf, 0, d.deflate(buf))
        d.end()
        return Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray())
    }

    /** 링크 전체 또는 # 뒤 데이터만 받아도 된다. */
    fun decode(linkOrData: String): DecodeResult = runCatching {
        val data = linkOrData.substringAfter('#').trim()
        val bytes = Base64.getUrlDecoder().decode(data)
        if (bytes.size > Tunables.SHARE_MAX_BYTES) return DecodeResult.Broken
        val inf = Inflater(true).apply { setInput(bytes) }
        val out = ByteArrayOutputStream()
        val buf = ByteArray(BUF)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break
            out.write(buf, 0, n)
            if (out.size() > Tunables.SHARE_MAX_INFLATED_BYTES) return DecodeResult.Broken
        }
        inf.end()
        val dto = json.decodeFromString(Dto.serializer(), out.toString(Charsets.UTF_8.name()))
        when {
            dto.v > VERSION -> DecodeResult.TooNew
            dto.v < 1 || dto.p.isEmpty() || dto.p.size > Tunables.SHARE_MAX_PLACES -> DecodeResult.Broken
            dto.p.any { it.n.isBlank() || it.y !in LAT || it.x !in LON } -> DecodeResult.Broken
            else -> DecodeResult.Ok(
                SharePayload(dto.t.take(Tunables.SHARE_TEXT_MAX), dto.d, dto.p.map { SharedPlace(it.n.take(Tunables.SHARE_TEXT_MAX), it.c, it.a, it.y, it.x, it.m, it.s, it.i) }, dto.f?.take(Tunables.SHARE_TEXT_MAX)),
            )
        }
    }.getOrDefault(DecodeResult.Broken)

    private fun r6(v: Double) = (v * E6).roundToLong() / E6

    @Serializable
    private data class Dto(val v: Int, val t: String = "", val d: Long = 0, val f: String? = null, val p: List<P> = emptyList())

    @Serializable
    private data class P(
        val n: String,
        val c: String? = null,
        val a: String? = null,
        val y: Double,
        val x: Double,
        val m: String? = null,
        @SerialName("s") val s: String? = null,
        @SerialName("i") val i: String? = null,
    )

    private const val BUF = 4096
    private const val E6 = 1_000_000.0
    private val LAT = -90.0..90.0
    private val LON = -180.0..180.0
}
