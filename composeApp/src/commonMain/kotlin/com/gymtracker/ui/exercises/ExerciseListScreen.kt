package com.gymtracker.ui.exercises

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.Exercises
import com.gymtracker.LocalAppContainer
import com.gymtracker.ui.components.SportCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    bottomPadding: Dp,
    groupId: Long,
    onBack: () -> Unit,
    onExerciseClick: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val vm: ExerciseListViewModel = viewModel(key = "group_$groupId") {
        ExerciseListViewModel(groupId, container.exerciseRepository, container.sessionRepository)
    }
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val groupName by vm.groupName.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseUi?>(null) }

    val fabClearance = 56.dp + 16.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { groupName?.let { Text(it.uppercase()) } },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding + fabClearance)
            ) {
                items(exercises, key = { it.exercise.id }) { item ->
                    ExerciseRow(
                        item = item,
                        onClick = { onExerciseClick(item.exercise.id) },
                        onLongClick = { exerciseToDelete = item }
                    )
                }
            }
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = bottomPadding + 16.dp)
            ) { Icon(Icons.Default.Add, "Añadir ejercicio") }
        }
    }

    exerciseToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("¿Eliminar ejercicio?") },
            text = { Text("Se eliminará \"${item.exercise.name}\" y todo su historial.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteExercise(item.exercise); exerciseToDelete = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { exerciseToDelete = null }) { Text("Cancelar") } }
        )
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onConfirm = { name, desc -> vm.addExercise(name, desc); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseRow(item: ExerciseUi, onClick: () -> Unit, onLongClick: () -> Unit) {
    SportCard(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.exercise.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (item.lastWeightKg != null) {
                Text(
                    "${item.lastSets}×${item.lastReps} · ${"%.1f".format(item.lastWeightKg)}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ejercicio") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Descripción") }, minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, description) }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
