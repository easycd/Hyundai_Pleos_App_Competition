package com.easycd.nextplace.core.ui.icon

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

/** Lucide 선 아이콘 (24 격자, 선 2). 이모지는 쓰지 않는다 (화면 설계서 1장). */
object NpIcons {
    val Mic by lazy {
        icon("mic", "M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z", "M19 10v2a7 7 0 0 1-14 0v-2", "M12 19v3")
    }
    val ChevronRight by lazy { icon("chevron-right", "M9 18l6-6-6-6") }
    val Back by lazy { icon("back", "M15 18l-6-6 6-6") }
    val Check by lazy { icon("check", "M20 6L9 17l-5-5") }
    val Close by lazy { icon("x", "M18 6L6 18", "M6 6l12 12") }
    val MapPin by lazy {
        icon(
            "map-pin",
            "M20 10c0 5-5.5 10.2-7.4 11.8a1 1 0 0 1-1.2 0C9.5 20.2 4 15 4 10a8 8 0 0 1 16 0",
            "M15 10a3 3 0 1 1-6 0a3 3 0 1 1 6 0",
        )
    }
    val Navigation by lazy { icon("navigation", "M3 11L22 2L13 21L11 13L3 11Z") }
    val Utensils by lazy {
        icon(
            "utensils",
            "M3 2v7c0 1.1 0.9 2 2 2h4a2 2 0 0 0 2-2V2",
            "M7 2v20",
            "M21 15V2a5 5 0 0 0-5 5v6c0 1.1 0.9 2 2 2h3Zm0 0v7",
        )
    }
    val Settings by lazy {
        icon(
            "settings",
            "M12.22 2h-0.44a2 2 0 0 0-2 2v0.18a2 2 0 0 1-1 1.73l-0.43 0.25a2 2 0 0 1-2 0l-0.15-0.08a2 2 0 0 0-2.73 0.73l-0.22 0.38a2 2 0 0 0 0.73 2.73l0.15 0.1a2 2 0 0 1 1 1.72v0.51a2 2 0 0 1-1 1.74l-0.15 0.09a2 2 0 0 0-0.73 2.73l0.22 0.38a2 2 0 0 0 2.73 0.73l0.15-0.08a2 2 0 0 1 2 0l0.43 0.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h0.44a2 2 0 0 0 2-2v-0.18a2 2 0 0 1 1-1.73l0.43-0.25a2 2 0 0 1 2 0l0.15 0.08a2 2 0 0 0 2.73-0.73l0.22-0.39a2 2 0 0 0-0.73-2.73l-0.15-0.08a2 2 0 0 1-1-1.74v-0.5a2 2 0 0 1 1-1.74l0.15-0.09a2 2 0 0 0 0.73-2.73l-0.22-0.38a2 2 0 0 0-2.73-0.73l-0.15 0.08a2 2 0 0 1-2 0l-0.43-0.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2Z",
            "M15 12a3 3 0 1 1-6 0a3 3 0 1 1 6 0",
        )
    }
    val Trash by lazy {
        icon("trash", "M3 6h18", "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6", "M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2")
    }
    val Share by lazy {
        icon(
            "share",
            "M21 5a3 3 0 1 1-6 0a3 3 0 1 1 6 0",
            "M9 12a3 3 0 1 1-6 0a3 3 0 1 1 6 0",
            "M21 19a3 3 0 1 1-6 0a3 3 0 1 1 6 0",
            "M8.59 13.51l6.83 3.98",
            "M15.41 6.51l-6.82 3.98",
        )
    }
    val Search by lazy { icon("search", "M19 11a8 8 0 1 1-16 0a8 8 0 1 1 16 0", "M21 21l-4.3-4.3") }
    val Pencil by lazy { icon("pencil", "M17 3l4 4L7 21H3v-4L17 3Z", "M14 6l4 4") }
    val Bell by lazy { icon("bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9", "M10.3 21a1.94 1.94 0 0 0 3.4 0") }
    val Link by lazy {
        icon(
            "link",
            "M10 13a5 5 0 0 0 7.54 0.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71",
            "M14 11a5 5 0 0 0-7.54-0.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71",
        )
    }
    val Send by lazy { icon("send", "M22 2l-7 20-4-9-9-4Z", "M22 2L11 13") }
    val Plus by lazy { icon("plus", "M5 12h14", "M12 5v14") }
    val Alert by lazy { icon("alert", "M22 12a10 10 0 1 1-20 0a10 10 0 1 1 20 0", "M12 8v4", "M12 16h0.01") }
    val Car by lazy {
        icon(
            "car-front",
            "M21 8l-2 2l-1.5-3.7A2 2 0 0 0 15.6 5H8.4a2 2 0 0 0-1.9 1.3L5 10L3 8",
            "M7 14h0.01",
            "M17 14h0.01",
            "M5 10h14a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2Z",
            "M5 18v2",
            "M19 18v2",
        )
    }
    val Flag by lazy { icon("flag", "M4 15s1-1 4-1s5 2 8 2s4-1 4-1V3s-1 1-4 1s-5-2-8-2s-4 1-4 1z", "M4 22v-7") }
    val Wheel by lazy {
        icon(
            "wheel",
            "M22 12a10 10 0 1 1-20 0a10 10 0 1 1 20 0",
            "M14 12a2 2 0 1 1-4 0a2 2 0 1 1 4 0",
            "M2.5 10.5L10 12",
            "M21.5 10.5L14 12",
            "M12 14v7.5",
        )
    }
    val Volume by lazy { icon("volume", "M11 5L6 9H2v6h4l5 4V5Z", "M15.54 8.46a5 5 0 0 1 0 7.07", "M19.07 4.93a10 10 0 0 1 0 14.14") }
    val Clock by lazy { icon("clock", "M22 12a10 10 0 1 1-20 0a10 10 0 1 1 20 0", "M12 6v6l4 2") }
    val Download by lazy { icon("download", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", "M7 10l5 5 5-5", "M12 15V3") }
    val Undo by lazy { icon("undo", "M9 14L4 9l5-5", "M4 9h10.5a5.5 5.5 0 0 1 0 11H11") }
    val Shield by lazy { icon("shield", "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-0.67-0.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1Z") }

    private fun icon(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach { d ->
            b.addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }
}
