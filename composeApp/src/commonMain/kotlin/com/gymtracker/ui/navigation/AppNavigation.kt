package com.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gymtracker.ui.exercises.ExerciseDetailScreen
import com.gymtracker.ui.exercises.ExerciseListScreen
import com.gymtracker.ui.home.HomeScreen
import com.gymtracker.ui.photos.CropEditorScreen
import com.gymtracker.ui.photos.PhotosScreen
import com.gymtracker.ui.progress.ExerciseProgressScreen
import com.gymtracker.ui.progress.ProgressScreen
import com.gymtracker.ui.settings.SettingsScreen
import com.gymtracker.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object ExerciseList : Screen("exercises/{groupId}") {
        fun route(groupId: Long) = "exercises/$groupId"
    }
    object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun route(exerciseId: Long) = "exercise/$exerciseId"
    }
    object Progress : Screen("progress")
    object ExerciseProgress : Screen("progress/{exerciseId}") {
        fun route(exerciseId: Long) = "progress/$exerciseId"
    }
    object Photos : Screen("photos")
    object Settings : Screen("settings")
    object CropEditor : Screen("crop/{photoId}") {
        fun route(photoId: Long) = "crop/$photoId"
    }
}

@Composable
fun AppNavigation() {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    val navController = rememberNavController()
    val topLevelRoutes = listOf(Screen.Home, Screen.Progress, Screen.Photos, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = topLevelRoutes.any {
        currentDestination?.hierarchy?.any { d -> d.route == it.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color(0xFF111111)) {
                    topLevelRoutes.forEach { screen ->
                        val (label, icon) = when (screen) {
                            Screen.Home     -> "Ejercicios" to Icons.Default.FitnessCenter
                            Screen.Progress -> "Progresión" to Icons.Default.BarChart
                            Screen.Photos   -> "Fotos"      to Icons.Default.PhotoLibrary
                            Screen.Settings -> "Ajustes"    to Icons.Default.Settings
                            else -> return@forEach
                        }
                        NavigationBarItem(
                            icon = { Icon(icon, label) },
                            label = { Text(label.uppercase(), style = MaterialTheme.typography.labelSmall) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = MaterialTheme.colorScheme.primary,
                                selectedTextColor   = MaterialTheme.colorScheme.primary,
                                indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    padding = innerPadding,
                    onGroupClick = { navController.navigate(Screen.ExerciseList.route(it)) },
                )
            }
            composable(
                Screen.ExerciseList.route,
                arguments = listOf(navArgument("groupId") { type = NavType.LongType }),
            ) { entry ->
                ExerciseListScreen(
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    groupId = entry.arguments!!.getLong("groupId"),
                    onBack = { navController.popBackStack() },
                    onExerciseClick = { navController.navigate(Screen.ExerciseDetail.route(it)) },
                )
            }
            composable(
                Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
            ) { entry ->
                ExerciseDetailScreen(
                    exerciseId = entry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen(
                    padding = innerPadding,
                    onExerciseClick = { navController.navigate(Screen.ExerciseProgress.route(it)) },
                )
            }
            composable(
                Screen.ExerciseProgress.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
            ) { entry ->
                ExerciseProgressScreen(
                    exerciseId = entry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Photos.route) {
                PhotosScreen(
                    padding = innerPadding,
                    onNavigateToCrop = { navController.navigate(Screen.CropEditor.route(it)) },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(padding = innerPadding)
            }
            composable(
                Screen.CropEditor.route,
                arguments = listOf(navArgument("photoId") { type = NavType.LongType }),
            ) { entry ->
                CropEditorScreen(
                    photoId = entry.arguments!!.getLong("photoId"),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
