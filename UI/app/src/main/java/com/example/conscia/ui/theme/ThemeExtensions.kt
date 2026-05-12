package com.example.conscia.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val ColorScheme.isDarkTheme: Boolean
    get() = background.luminance() < 0.5f

fun ColorScheme.tintedSurface(tint: Color, lightColor: Color = tint.copy(alpha = 0.12f)): Color {
    return if (isDarkTheme) tint.copy(alpha = 0.2f) else lightColor
}
