package com.easycd.nextplace.feature.share.ui.send

import android.graphics.Bitmap

data class ShareRowModel(val id: String, val name: String, val category: String?, val checked: Boolean)

/** S-10 맛집 지도 공유. [link] 가 null 이면 0곳 선택 (링크 복사 · 보내기 흐림). */
data class ShareUiState(
    val loaded: Boolean = false,
    val rows: List<ShareRowModel> = emptyList(),
    val title: String = "",
    val titleEdited: Boolean = false,
    val link: String? = null,
    val qr: Bitmap? = null,
    val copied: Boolean = false,
) {
    val selectedCount: Int get() = rows.count { it.checked }
}
