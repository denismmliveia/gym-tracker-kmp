package com.gymtracker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.gymtracker.platform.PlatformContext

actual class DatabaseDriverFactory actual constructor(context: PlatformContext) {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(GymTrackerDatabase.Schema, "gymtracker.db")
}
