package com.easycd.nextplace.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPalette = staticCompositionLocalOf { Palettes.dark }

/** 테마는 차량 주/야간 설정(시스템 야간 모드)을 그대로 따른다. 앱 안 수동 전환은 없다. */
@Composable
fun NextPlaceTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (dark) Palettes.dark else Palettes.light
    val scheme = if (dark) {
        darkColorScheme(primary = palette.brand, background = palette.background, surface = palette.solidCard, onBackground = palette.text, onSurface = palette.text)
    } else {
        lightColorScheme(primary = palette.brand, background = palette.background, surface = palette.solidCard, onBackground = palette.text, onSurface = palette.text)
    }
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = NpType.material, content = content)
    }
}

object Np {
    val colors: Palette
        @Composable get() = LocalPalette.current
}
