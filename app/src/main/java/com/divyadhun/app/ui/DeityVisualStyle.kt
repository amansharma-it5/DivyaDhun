package com.divyadhun.app.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Restrained, original accent colors that help each deity feel recognisable. */
@Immutable
data class DeityVisualStyle(val accent: Color)

fun deityVisualStyle(id: String): DeityVisualStyle = when (id) {
    "hanuman" -> DeityVisualStyle(Color(0xFFD85A2A))
    "shiv" -> DeityVisualStyle(Color(0xFF5E7896))
    "krishna" -> DeityVisualStyle(Color(0xFF3E62A8))
    "ram" -> DeityVisualStyle(Color(0xFFB88728))
    "ganesh" -> DeityVisualStyle(Color(0xFFC86349))
    "durga" -> DeityVisualStyle(Color(0xFFB54445))
    "lakshmi" -> DeityVisualStyle(Color(0xFFB85C78))
    "sai" -> DeityVisualStyle(Color(0xFF9A7048))
    else -> DeityVisualStyle(Color(0xFFD5A33B))
}
