package com.example.conscia.ui.theme

import androidx.compose.ui.graphics.Color

val AppUsageChartPalette = listOf(
    Color(0xFF2563EB),
    Color(0xFF16A34A),
    Color(0xFFF59E0B),
    Color(0xFFDC2626),
    Color(0xFF7C3AED),
    Color(0xFF0891B2),
    Color(0xFFDB2777),
    Color(0xFF65A30D),
    Color(0xFFEA580C),
    Color(0xFF4F46E5),
    Color(0xFF0F766E),
    Color(0xFF9333EA)
)

fun appUsageChartColor(index: Int): Color {
    return AppUsageChartPalette[index.floorMod(AppUsageChartPalette.size)]
}

private fun Int.floorMod(other: Int): Int {
    return ((this % other) + other) % other
}
