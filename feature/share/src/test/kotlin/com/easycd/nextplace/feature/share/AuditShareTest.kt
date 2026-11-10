package com.easycd.nextplace.feature.share

import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.feature.share.codec.DecodeResult
import com.easycd.nextplace.feature.share.codec.ShareCodec
import com.easycd.nextplace.feature.share.codec.SharePayload
import org.junit.Assert.assertTrue
import org.junit.Test

/** 전수점검 보고서(docs/05) 재현 테스트: B-03. */
class AuditShareTest {
    @Test fun validThirtyPlaceLinkCanBeReadByTheSameApp() {
        val rows = (1..30).map { i -> SharedPlace(
            "할머니손맛 왕돈까스 앤 수제버거 강남역삼본점 $i",
            "돈까스", "서울특별시 강남구 테헤란로 123번길 45 지하 1층 $i 호",
            37.5 + i / 10000.0, 127.02,
            "가족과 함께 방문했고 음식이 맛있어서 다음에도 다시 가고 싶은 가게입니다",
            "kakao", "$i") }
        val payload = SharePayload("서울 맛집", 0, rows)
        val link = ShareCodec.link(payload)
        val decoded = ShareCodec.decode(link)
        assertTrue("The app rejects its own valid 30-place link (${link.length} chars): $decoded", decoded is DecodeResult.Ok)
    }
}
