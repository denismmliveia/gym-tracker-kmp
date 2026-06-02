package com.gymtracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gymtracker.App
import com.gymtracker.GymTrackerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as GymTrackerApp).container
        setContent { App(container) }
    }
}
