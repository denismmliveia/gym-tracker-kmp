package com.gymtracker.platform

import androidx.compose.runtime.Composable

@Composable
expect fun AudioPermissionEffect(requested: Boolean, onGranted: () -> Unit)
