package com.gymtracker.platform

import platform.Foundation.NSFileManager

actual fun deleteFile(path: String): Boolean =
    NSFileManager.defaultManager.removeItemAtPath(path, null)
