package com.gymtracker.platform

import platform.Foundation.NSDate

actual fun getCurrentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
