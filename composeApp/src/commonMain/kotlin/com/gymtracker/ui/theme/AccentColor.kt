package com.gymtracker.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentColor(
    val key: String,
    val primary: Color,
    val secondary: Color,
    val label: String
) {
    ORANGE("orange", Color(0xFFFF6D00), Color(0xFFFF3D00), "Naranja Fuego"),
    BLUE  ("blue",   Color(0xFF2979FF), Color(0xFF1565C0), "Azul Eléctrico"),
    GREEN ("green",  Color(0xFF64DD17), Color(0xFF33691E), "Verde Lima"),
    RED   ("red",    Color(0xFFFF1744), Color(0xFFB71C1C), "Rojo Carmesí"),
    CYAN  ("cyan",   Color(0xFF00E5FF), Color(0xFF0097A7), "Cian Neón"),
    PURPLE("purple", Color(0xFFE040FB), Color(0xFF7B1FA2), "Púrpura Neón");

    companion object {
        fun fromKey(key: String): AccentColor =
            entries.firstOrNull { it.key == key } ?: ORANGE
    }
}
