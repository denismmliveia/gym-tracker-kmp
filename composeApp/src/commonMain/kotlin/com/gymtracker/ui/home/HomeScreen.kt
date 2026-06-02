package com.gymtracker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(padding: PaddingValues, onGroupClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val vm: HomeViewModel = viewModel {
        HomeViewModel(container.exerciseRepository, container.sessionRepository)
    }
    val groups by vm.groups.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GYMTRACKER", fontWeight = FontWeight.Black, letterSpacing = 0.08.em) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir grupo muscular")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups, key = { it.group.id }) { item ->
                MuscleGroupCard(item = item, onClick = { onGroupClick(item.group.id) })
            }
        }
    }

    if (showAddDialog) {
        AddMuscleGroupDialog(
            onConfirm = { name -> vm.addGroup(name); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun MuscleGroupCard(item: MuscleGroupUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💪", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(item.group.name.uppercase(), style = MaterialTheme.typography.titleMedium)
                if (item.isStale) {
                    Spacer(Modifier.height(4.dp))
                    Text("Sin registros recientes", fontSize = 10.sp, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
private fun AddMuscleGroupDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo grupo muscular") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
