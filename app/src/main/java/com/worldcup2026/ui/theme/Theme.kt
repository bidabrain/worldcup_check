package com.worldcup2026.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = WcBlue,
    onPrimary        = Color.White,
    primaryContainer = WcBlueLight,
    secondary        = WcGold,
    onSecondary      = Color.Black,
    background       = BackgroundGray,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundGray,
    outline          = CardBorder
)

@Composable
fun WorldCup2026Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography(),
        content     = content
    )
}
