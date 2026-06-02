package com.gymtracker.platform

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun AudioPermissionEffect(requested: Boolean, onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onGranted() }
    LaunchedEffect(requested) {
        if (requested) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
