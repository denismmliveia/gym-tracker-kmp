package com.gymtracker.platform

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString
