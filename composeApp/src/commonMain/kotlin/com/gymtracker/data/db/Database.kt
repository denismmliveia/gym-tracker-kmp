package com.gymtracker.data.db

import com.gymtracker.platform.PlatformContext

fun createDatabase(context: PlatformContext): GymTrackerDatabase {
    val driver = DatabaseDriverFactory(context).createDriver()
    return GymTrackerDatabase(driver)
}
