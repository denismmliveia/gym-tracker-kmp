package com.gymtracker.platform

actual fun generateUuid(): String = java.util.UUID.randomUUID().toString()
