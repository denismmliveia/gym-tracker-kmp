package com.gymtracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gymtracker.Exercises
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.Muscle_groups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExerciseRepository(private val db: GymTrackerDatabase) {

    fun getAllGroups(): Flow<List<Muscle_groups>> =
        db.muscleGroupQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getExercisesForGroup(groupId: Long): Flow<List<Exercises>> =
        db.exerciseQueries.selectByGroup(groupId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getExerciseById(id: Long): Exercises? = withContext(Dispatchers.IO) {
        db.exerciseQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun insertExercise(muscleGroupId: Long, name: String, description: String): Long =
        withContext(Dispatchers.IO) {
            db.exerciseQueries.transactionWithResult {
                db.exerciseQueries.insert(muscleGroupId, name, description, null)
                db.exerciseQueries.selectByGroup(muscleGroupId).executeAsList().last().id
            }
        }

    suspend fun updateExercise(exercise: Exercises) = withContext(Dispatchers.IO) {
        db.exerciseQueries.updateExercise(exercise.name, exercise.description, exercise.photoPath, exercise.id)
    }

    suspend fun updatePhotoPath(exerciseId: Long, path: String?) = withContext(Dispatchers.IO) {
        db.exerciseQueries.updatePhotoPath(path, exerciseId)
    }

    suspend fun insertMuscleGroup(name: String) = withContext(Dispatchers.IO) {
        db.muscleGroupQueries.insert(name)
    }

    suspend fun getGroupById(groupId: Long): Muscle_groups? = withContext(Dispatchers.IO) {
        db.muscleGroupQueries.selectById(groupId).executeAsOneOrNull()
    }

    suspend fun deleteExercise(exercise: Exercises) = withContext(Dispatchers.IO) {
        db.exerciseQueries.delete(exercise.id)
    }
}
