package com.gymtracker.platform

import java.io.File

actual fun localFileModel(path: String): Any = File(path)
