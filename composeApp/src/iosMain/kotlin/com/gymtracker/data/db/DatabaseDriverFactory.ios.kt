package com.gymtracker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory(context: PlatformContext) {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(GymTrackerDatabase.Schema, "gymtracker.db")
}
