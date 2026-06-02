package com.gymtracker.domain.voice

import kotlin.math.sqrt

data class ParsedSession(
    val sets: Int?,
    val reps: Int?,
    val weightKg: Float?
) {
    val isComplete: Boolean get() = sets != null && reps != null && weightKg != null
}

class SessionParser {
    fun parse(text: String): ParsedSession {
        val t = normalizeNumbers(text.lowercase().trim())
        return ParsedSession(
            sets = extractSets(t),
            reps = extractReps(t),
            weightKg = extractWeight(t)
        )
    }

    private fun normalizeNumbers(t: String): String {
        var result = t
        for ((word, digit) in SPANISH_NUMBERS) {
            result = result.replace(Regex("""\b$word\b"""), digit.toString())
        }
        return result
    }

    private fun extractSets(t: String): Int? {
        SETS_SERIES.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_X.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_POR.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_AFTER_KG.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractReps(t: String): Int? {
        REPS_EXPLICIT.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_SERIES_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_X.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_POR.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_AFTER_KG.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractWeight(t: String): Float? {
        if (WEIGHT_ZERO.containsMatchIn(t)) return 0f
        WEIGHT_KG.find(t)?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        WEIGHT_CON_A.find(t)?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        return null
    }

    companion object {
        // Longest first so that e.g. "veintiuno" is replaced before "uno"
        private val SPANISH_NUMBERS = listOf(
            "veintiuno" to 21, "veintiuna" to 21,
            "veintidós" to 22, "veintidos" to 22,
            "veintitrés" to 23, "veintitres" to 23,
            "veinticuatro" to 24,
            "veinticinco" to 25,
            "veintiséis" to 26, "veintiseis" to 26,
            "veintisiete" to 27,
            "veintiocho" to 28,
            "veintinueve" to 29,
            "diecinueve" to 19,
            "dieciocho" to 18,
            "diecisiete" to 17,
            "dieciséis" to 16, "dieciseis" to 16,
            "quince" to 15,
            "catorce" to 14,
            "trece" to 13,
            "doce" to 12,
            "once" to 11,
            "cincuenta" to 50,
            "cuarenta" to 40,
            "treinta" to 30,
            "veinte" to 20,
            "nueve" to 9,
            "ocho" to 8,
            "siete" to 7,
            "seis" to 6,
            "cinco" to 5,
            "cuatro" to 4,
            "tres" to 3,
            "dos" to 2,
            "uno" to 1, "una" to 1,
            "diez" to 10,
        )

        private val SETS_SERIES = Regex("""(\d+)\s*series?""")
        private val SETS_X = Regex("""(\d+)\s*[xX×]\s*\d+""")
        private val SETS_POR = Regex("""(\d+)\s*por\s*\d+""")
        private val SETS_AFTER_KG = Regex("""(?:kilos?|kg)[^0-9]*(\d+)\s*de\s*\d+""")
        private val SETS_DE = Regex("""(\d+)\s*de\s*\d+""")

        private val REPS_EXPLICIT = Regex("""(\d+)\s*repeticiones?""")
        private val REPS_SERIES_DE = Regex("""series?\s+de\s+(\d+)""")
        private val REPS_X = Regex("""\d+\s*[xX×]\s*(\d+)""")
        private val REPS_POR = Regex("""\d+\s*por\s*(\d+)""")
        private val REPS_AFTER_KG = Regex("""(?:kilos?|kg)[^0-9]*\d+\s*de\s*(\d+)""")
        private val REPS_DE = Regex("""\d+\s*de\s*(\d+)""")

        private val WEIGHT_ZERO = Regex("""sin\s*peso|peso\s*corporal|cuerpo""")
        private val WEIGHT_KG = Regex("""(\d+(?:[.,]\d+)?)\s*(?:kilos?|kg)""")
        private val WEIGHT_CON_A = Regex("""\b(?:con|a)\s+(\d+(?:[.,]\d+)?)(?!\s*(?:series?|reps?|repeticiones?))""")
    }
}
