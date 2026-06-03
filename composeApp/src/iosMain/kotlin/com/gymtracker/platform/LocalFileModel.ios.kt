package com.gymtracker.platform

import platform.Foundation.NSURL

actual fun localFileModel(path: String): Any = NSURL.fileURLWithPath(path)
