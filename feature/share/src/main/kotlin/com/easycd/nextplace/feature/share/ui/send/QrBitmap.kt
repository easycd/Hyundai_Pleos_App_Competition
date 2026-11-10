package com.easycd.nextplace.feature.share.ui.send

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** 링크 → QR 비트맵 (동승자가 폰으로 바로 찍는다). */
object QrBitmap {
    fun of(text: String, size: Int = SIZE): Bitmap? = runCatching {
        val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L, EncodeHintType.MARGIN to 1))
        val px = IntArray(size * size) { i -> if (m[i % size, i / size]) Color.BLACK else Color.WHITE }
        Bitmap.createBitmap(px, size, size, Bitmap.Config.ARGB_8888)
    }.getOrNull()

    private const val SIZE = 480
}
