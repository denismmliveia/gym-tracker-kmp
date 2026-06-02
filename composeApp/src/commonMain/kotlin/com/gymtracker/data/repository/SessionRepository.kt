package com.gymtracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.data.db.Sessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SessionRepository(private val db: GymTrackerDatabase) {

    fun getSessionsForExercise(exerciseId: Long): Flow<List<Sessions>> =
        db.sessionsQueries.selectByExercise(exerciseId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getLatestSession(exerciseId: Long): Sessions? = withContext(Dispatchers.IO) {
        db.sessionsQueries.selectLatestByExercise(exerciseId).executeAsOneOrNull()
    }

    suspend fun getMaxWeight(exerciseId: Long): Double? = withContext(Dispatchers.IO) {
        db.sessionsQueries.selectMaxWeightByExercise(exerciseId).executeAsOneOrNull()
            ?.MAX_weightKg_
    }

    suspend fun insertSession(
        exerciseId: Long, date: Long, sets: Long, reps: Long, weightKg: Double
    ): Unit = withContext(Dispatchers.IO) {
        db.sessionsQueries.insert(exerciseId, date, sets, reps, weightKg)
    }

    suspend fun deleteSession(session: Sessions) = withContext(Dispatchers.IO) {
        db.sessionsQueries.delete(session.id)
    }
}
