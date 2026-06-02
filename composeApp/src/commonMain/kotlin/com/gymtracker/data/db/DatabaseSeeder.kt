package com.gymtracker.data.db

object DatabaseSeeder {
    fun seed(db: GymTrackerDatabase) {
        val groups = db.muscleGroupQueries.selectAll().executeAsList()
        if (groups.isNotEmpty()) return

        val muscleData = listOf(
            "Pecho" to listOf(
                "Press Banca" to "Ejercicio básico de empuje horizontal.",
                "Press Inclinado" to "Trabaja la parte superior del pecho.",
                "Aperturas" to "Ejercicio de aislamiento para el pecho.",
                "Press Declinado" to "Trabaja la parte inferior del pecho.",
                "Fondos en paralelas" to "Empuje con peso corporal."
            ),
            "Espalda" to listOf(
                "Dominadas" to "Tirón vertical con peso corporal.",
                "Remo con barra" to "Tirón horizontal con barra.",
                "Remo en polea" to "Tirón horizontal en máquina.",
                "Jalón al pecho" to "Tirón vertical en máquina.",
                "Peso muerto" to "Ejercicio compuesto de cadena posterior."
            ),
            "Hombros" to listOf(
                "Press militar" to "Empuje vertical con barra.",
                "Elevaciones laterales" to "Aislamiento del deltoides lateral.",
                "Elevaciones frontales" to "Aislamiento del deltoides frontal.",
                "Pájaros" to "Aislamiento del deltoides posterior."
            ),
            "Bíceps" to listOf(
                "Curl con barra" to "Flexión de codo con barra.",
                "Curl martillo" to "Curl con agarre neutro.",
                "Curl concentrado" to "Aislamiento unilateral de bíceps.",
                "Curl en polea" to "Curl con resistencia constante."
            ),
            "Tríceps" to listOf(
                "Press francés" to "Extensión de codo con barra.",
                "Extensión en polea" to "Extensión de codo en máquina.",
                "Fondos en banco" to "Empuje con peso corporal.",
                "Patada de tríceps" to "Aislamiento unilateral de tríceps."
            ),
            "Piernas" to listOf(
                "Sentadilla" to "Ejercicio compuesto de piernas.",
                "Prensa" to "Empuje de piernas en máquina.",
                "Zancadas" to "Ejercicio unilateral de piernas.",
                "Curl femoral" to "Aislamiento de isquiotibiales.",
                "Extensión de cuádriceps" to "Aislamiento de cuádriceps.",
                "Gemelos de pie" to "Aislamiento de gemelos."
            ),
            "Abdominales" to listOf(
                "Crunch" to "Flexión de tronco básica.",
                "Plancha" to "Ejercicio isométrico de core.",
                "Elevación de piernas" to "Trabaja la zona baja abdominal.",
                "Rueda abdominal" to "Ejercicio avanzado de core."
            )
        )

        for ((groupName, exercises) in muscleData) {
            val groupId = db.muscleGroupQueries.transactionWithResult {
                db.muscleGroupQueries.insert(groupName)
                db.muscleGroupQueries.selectAll().executeAsList().last().id
            }
            for ((exerciseName, description) in exercises) {
                db.exerciseQueries.insert(groupId, exerciseName, description, null)
            }
        }
    }
}
