package com.gymtracker

import android.app.Application
import com.gymtracker.platform.PlatformContext

class GymTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(PlatformContext(this))
    }
}
