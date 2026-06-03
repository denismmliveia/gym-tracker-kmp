package com.gymtracker.platform

import java.io.File

actual fun deleteFile(path: String): Boolean = File(path).delete()
