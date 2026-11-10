package com.easycd.nextplace.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** 화면 설계서 1장 색 토큰 (다크·라이트). 파랑은 쓰지 않는다. */
@Immutable
data class Palette(
    val isDark: Boolean,
    val background: Color,
    val text: Color,
    val muted: Color,
    val brand: Color,
    val brandText: Color,
    val onBrand: Color,
    val amber: Color,
    val green: Color,
    val danger: Color,
    val dangerText: Color,
    val glass: Color,
    val glassStrong: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val scrim: Color,
    val shadow: Color,
    val solidCard: Color,
)

object Palettes {
    private val brand = Color(0xFFE4572E)

    val dark = Palette(
        isDark = true,
        background = Color(0xFF15100E),
        text = Color(0xFFFBF6F2),
        muted = Color(0xFFB8A69B),
        brand = brand,
        brandText = Color(0xFFFF8A62),
        onBrand = Color.White,
        amber = Color(0xFFF2B544),
        green = Color(0xFF4CC38A),
        danger = Color(0xFFC62828),
        dangerText = Color(0xFFFF6B6B),
        glass = Color.White.copy(alpha = 0.09f),
        glassStrong = Color.White.copy(alpha = 0.14f),
        glassBorder = Color.White.copy(alpha = 0.16f),
        glassHighlight = Color.White.copy(alpha = 0.28f),
        scrim = Color.Black.copy(alpha = 0.72f),
        shadow = Color.Black.copy(alpha = 0.45f),
        solidCard = Color(0xFF1F1714),
    )

    val light = Palette(
        isDark = false,
        background = Color(0xFFF6EFEA),
        text = Color(0xFF1F1512),
        muted = Color(0xFF6E5B53),
        brand = brand,
        brandText = Color(0xFFB83A18),
        onBrand = Color.White,
        amber = Color(0xFF8A5A00),
        green = Color(0xFF1B7F4D),
        danger = Color(0xFFC62828),
        dangerText = Color(0xFFC62828),
        glass = Color.White.copy(alpha = 0.78f),
        glassStrong = Color.White.copy(alpha = 0.95f),
        glassBorder = Color(0xFF1F1512).copy(alpha = 0.08f),
        glassHighlight = Color.White,
        scrim = Color(0xFF1F1512).copy(alpha = 0.45f),
        shadow = Color(0xFF5A3A2A).copy(alpha = 0.16f),
        solidCard = Color.White,
    )
}
