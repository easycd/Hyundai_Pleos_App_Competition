package com.easycd.nextplace.feature.share

import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.feature.share.codec.DecodeResult
import com.easycd.nextplace.feature.share.codec.ShareCodec
import com.easycd.nextplace.feature.share.codec.SharePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareCodecTest {
    private val payload = SharePayload(
        "내 맛집 2곳", 1_700_000_000_000,
        listOf(
            SharedPlace("서울갈비", "고깃집", "서울 강남구 강남대로 123", 37.4979, 127.0276, "갈비 맛있음", "kakao", "123"),
            SharedPlace("모퉁이 커피", "카페", null, 37.5012345678, 127.0259876543, null, null, null),
        ),
    )

    @Test fun roundTrip() {
        val link = ShareCodec.link(payload)
        assertTrue(link.startsWith(ShareCodec.BASE_URL))
        val r = ShareCodec.decode(link) as DecodeResult.Ok
        assertEquals("내 맛집 2곳", r.payload.title)
        assertEquals(2, r.payload.places.size)
        assertEquals(37.501235, r.payload.places[1].lat, 1e-9)
        assertEquals("갈비 맛있음", r.payload.places[0].memo)
    }

    @Test fun brokenLink() {
        assertEquals(DecodeResult.Broken, ShareCodec.decode("https://easycd.github.io/nextplace/s#abc!!"))
        assertEquals(DecodeResult.Broken, ShareCodec.decode(""))
    }

    @Test fun thirtyPlacesFitQr() {
        val many = SharePayload("t", 0, (1..30).map { SharedPlace("가게$it", "고깃집", "서울 강남구 강남대로 $it", 37.49 + it / 1000.0, 127.02, null, "kakao", "$it") })
        assertTrue(ShareCodec.link(many).length < 4000)
    }
}
