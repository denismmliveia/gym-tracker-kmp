package com.gymtracker.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun AudioPermissionEffect(requested: Boolean, onGranted: () -> Unit) {
    LaunchedEffect(requested) {
        if (requested) onGranted()
    }
}
