package com.gymtracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.Sessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SessionRepository(private val db: GymTrackerDatabase) {

    fun getSessionsForExercise(exerciseId: Long): Flow<List<Sessions>> =
        db.sessionQueries.selectByExercise(exerciseId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getLatestSession(exerciseId: Long): Sessions? = withContext(Dispatchers.IO) {
        db.sessionQueries.selectLatestByExercise(exerciseId).executeAsOneOrNull()
    }

    suspend fun getMaxWeight(exerciseId: Long): Double? = withContext(Dispatchers.IO) {
        db.sessionQueries.selectMaxWeightByExercise(exerciseId).executeAsOneOrNull()
            ?.MAX
    }

    suspend fun insertSession(
        exerciseId: Long, date: Long, sets: Long, reps: Long, weightKg: Double
    ): Unit = withContext(Dispatchers.IO) {
        db.sessionQueries.insert(exerciseId, date, sets, reps, weightKg)
    }

    suspend fun deleteSession(session: Sessions) = withContext(Dispatchers.IO) {
        db.sessionQueries.delete(session.id)
    }
}
