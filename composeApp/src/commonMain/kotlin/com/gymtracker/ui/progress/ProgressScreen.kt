package com.gymtracker.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.LocalAppContainer
import com.gymtracker.ui.components.SportCard
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer

@Composable
fun ProgressScreen(padding: PaddingValues, onExerciseClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val vm: ProgressViewModel = viewModel {
        ProgressViewModel(container.exerciseRepository, container.sessionRepository)
    }
    val groups by vm.groupProgress.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groups.forEach { groupUi ->
            item {
                Text(groupUi.group.name.uppercase(), style = MaterialTheme.typography.titleLarge)
            }
            items(groupUi.exercises) { summary ->
                ExerciseProgressRow(
                    summary = summary,
                    onClick = { onExerciseClick(summary.exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressRow(summary: ExerciseProgressSummary, onClick: () -> Unit) {
    SportCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.exercise.name.uppercase(), style = MaterialTheme.typography.titleSmall)
                summary.improvementPct?.let {
                    val color = if (it >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
                    Text(
                        "${if (it >= 0) "+" else ""}${"%.1f".format(it)}%",
                        fontSize = 12.sp,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (summary.dayScores.size >= 2) {
                MiniSparklineChart(
                    dayScores = summary.dayScores,
                    modifier = Modifier.size(width = 80.dp, height = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniSparklineChart(dayScores: List<DayScore>, modifier: Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(dayScores) {
        modelProducer.runTransaction {
            lineSeries { series(dayScores.map { it.value }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer()),
        modelProducer = modelProducer,
        modifier = modifier
    )
}
