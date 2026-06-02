package com.gymtracker

import com.gymtracker.data.db.DatabaseSeeder
import com.gymtracker.data.db.createDatabase
import com.gymtracker.data.repository.BodyPhotoRepository
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.platform.PlatformContext

class AppContainer(context: PlatformContext) {
    private val db = createDatabase(context).also { DatabaseSeeder.seed(it) }
    val exerciseRepository = ExerciseRepository(db)
    val sessionRepository = SessionRepository(db)
    val bodyPhotoRepository = BodyPhotoRepository(db)
}
