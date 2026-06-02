package com.gymtracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.LocalAppContainer
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart

private fun metricLabel(metric: ProgressMetric): String = when (metric) {
    ProgressMetric.MAX_WEIGHT -> "Peso Máx."
    ProgressMetric.VOLUME     -> "Volumen"
    ProgressMetric.ONE_RM     -> "1RM Est."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(
    exerciseId: Long,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val vm: ExerciseProgressViewModel = viewModel(key = "exercise_progress_$exerciseId") {
        ExerciseProgressViewModel(
            sessionRepository = container.sessionRepository,
            exerciseRepository = container.exerciseRepository,
            exerciseId = exerciseId
        )
    }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exerciseName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Metric selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressMetric.entries.forEach { metric ->
                    FilterChip(
                        selected = state.metric == metric,
                        onClick = { vm.setMetric(metric) },
                        label = { Text(metricLabel(metric)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.dayScores.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay sesiones registradas")
                }
            } else {
                val modelProducer = remember { CartesianChartModelProducer() }
                LaunchedEffect(state.dayScores) {
                    modelProducer.runTransaction {
                        lineSeries { series(state.dayScores.map { it.value }) }
                    }
                }
                val dayScores = state.dayScores
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = CartesianValueFormatter { _, value, _ ->
                                val idx = value.toInt().coerceIn(dayScores.indices)
                                formatDayLabel(dayScores[idx].date)
                            }
                        )
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}
