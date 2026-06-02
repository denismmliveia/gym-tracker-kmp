package com.gymtracker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gymtracker.platform.PlatformContext

actual class DatabaseDriverFactory actual constructor(context: PlatformContext) {
    private val ctx = context.androidContext
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(GymTrackerDatabase.Schema, ctx, "gymtracker.db")
}
