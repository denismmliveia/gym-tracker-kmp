package com.gymtracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gymtracker.data.db.Exercises
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.data.db.Muscle_groups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExerciseRepository(private val db: GymTrackerDatabase) {

    fun getAllGroups(): Flow<List<Muscle_groups>> =
        db.muscleGroupsQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getExercisesForGroup(groupId: Long): Flow<List<Exercises>> =
        db.exercisesQueries.selectByGroup(groupId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getExerciseById(id: Long): Exercises? = withContext(Dispatchers.IO) {
        db.exercisesQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun insertExercise(muscleGroupId: Long, name: String, description: String): Long =
        withContext(Dispatchers.IO) {
            db.exercisesQueries.transactionWithResult {
                db.exercisesQueries.insert(muscleGroupId, name, description, null)
                db.exercisesQueries.selectByGroup(muscleGroupId).executeAsList().last().id
            }
        }

    suspend fun updateExercise(exercise: Exercises) = withContext(Dispatchers.IO) {
        db.exercisesQueries.updateExercise(exercise.name, exercise.description, exercise.photoPath, exercise.id)
    }

    suspend fun updatePhotoPath(exerciseId: Long, path: String?) = withContext(Dispatchers.IO) {
        db.exercisesQueries.updatePhotoPath(path, exerciseId)
    }
}
