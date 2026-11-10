package com.easycd.nextplace.feature.share.ui.send

import com.easycd.nextplace.core.domain.Tunables
import android.graphics.Bitmap

data class ShareRowModel(val id: String, val name: String, val category: String?, val checked: Boolean, val visits: Int = 0)

/** S-10 맛집 지도 공유. [link] 가 null 이면 0곳 선택 (링크 복사 · 보내기 흐림). */
data class ShareUiState(
    val loaded: Boolean = false,
    val rows: List<ShareRowModel> = emptyList(),
    val title: String = "",
    val titleEdited: Boolean = false,
    val link: String? = null,
    val qr: Bitmap? = null,
    val copied: Boolean = false,
    /** 고른 가게가 많아 링크가 상한(압축 후 8KB)을 넘음: 링크 대신 몇 곳을 빼 달라는 안내. */
    val tooBig: Boolean = false,
) {
    val selectedCount: Int get() = rows.count { it.checked }
    /** '전체 선택'이 켜진 상태: 고를 수 있는 만큼(최대 30곳) 다 골랐다. 31곳 이상이어도 다시 누르면 모두 해제된다. */
    val allSelected: Boolean get() = rows.isNotEmpty() && selectedCount >= minOf(rows.size, Tunables.SHARE_MAX_PLACES)
}
