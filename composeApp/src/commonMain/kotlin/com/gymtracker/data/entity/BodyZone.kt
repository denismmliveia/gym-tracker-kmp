package com.gymtracker.data.entity

enum class BodyZone {
    FULL_BODY, CHEST, BACK, ARMS, LEGS, SHOULDERS;

    fun displayName(): String = when (this) {
        FULL_BODY  -> "Cuerpo entero"
        CHEST      -> "Pecho"
        BACK       -> "Espalda"
        ARMS       -> "Brazos"
        LEGS       -> "Piernas"
        SHOULDERS  -> "Hombros"
    }

    companion object {
        fun fromString(value: String): BodyZone =
            entries.firstOrNull { it.name == value } ?: FULL_BODY
    }
}
