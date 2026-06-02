package com.gymtracker

import com.gymtracker.data.db.DatabaseDriverFactory
import com.gymtracker.data.db.DatabaseSeeder
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.data.repository.BodyPhotoRepository
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.platform.ImageProcessor
import com.gymtracker.platform.PlatformContext
import com.gymtracker.platform.VoiceRecognizer
import com.gymtracker.ui.theme.ThemePreferences

class AppContainer(val context: PlatformContext) {
    private val db: GymTrackerDatabase

    val exerciseRepository: ExerciseRepository
    val sessionRepository: SessionRepository
    val bodyPhotoRepository: BodyPhotoRepository
    val themePreferences: ThemePreferences
    val voiceRecognizer: VoiceRecognizer
    val imageProcessor: ImageProcessor

    init {
        val driver = DatabaseDriverFactory(context).createDriver()
        db = GymTrackerDatabase(driver)
        DatabaseSeeder.seed(db)

        exerciseRepository  = ExerciseRepository(db)
        sessionRepository   = SessionRepository(db)
        bodyPhotoRepository = BodyPhotoRepository(db)
        themePreferences    = ThemePreferences(context)
        voiceRecognizer     = VoiceRecognizer(context)
        imageProcessor      = ImageProcessor(context)
    }
}
