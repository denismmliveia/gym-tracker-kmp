package com.gymtracker.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import gymtracker.composeapp.generated.resources.Res
import gymtracker.composeapp.generated.resources.ic_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onTimeout()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = "GymTracker",
                modifier = Modifier.size(220.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "GYMTRACKER",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.12.em
            )
        }
    }
}
