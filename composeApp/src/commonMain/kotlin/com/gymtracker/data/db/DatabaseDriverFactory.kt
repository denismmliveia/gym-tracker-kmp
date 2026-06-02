package com.gymtracker.data.db

import app.cash.sqldelight.db.SqlDriver
import com.gymtracker.platform.PlatformContext

expect class DatabaseDriverFactory(context: PlatformContext) {
    fun createDriver(): SqlDriver
}
