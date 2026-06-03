package com.gymtracker.ui.photos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.gymtracker.Body_photos
import com.gymtracker.LocalAppContainer
import com.gymtracker.data.entity.BodyZone
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun PhotosScreen(padding: PaddingValues, onNavigateToCrop: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val vm: PhotosViewModel = viewModel {
        PhotosViewModel(container.bodyPhotoRepository, container.imageProcessor)
    }
    val photos by vm.photos.collectAsStateWithLifecycle()
    val selectedZone by vm.selectedZone.collectAsStateWithLifecycle()
    val navigateToCrop by vm.navigateToCrop.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showZonePicker by remember { mutableStateOf(false) }
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    var photoToDelete by remember { mutableStateOf<Body_photos?>(null) }
    var fullScreenPhoto by remember { mutableStateOf<Body_photos?>(null) }

    LaunchedEffect(navigateToCrop) {
        navigateToCrop?.let { vm.cropNavigationHandled(); onNavigateToCrop(it) }
    }

    val photoPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { bytes -> bytes.firstOrNull()?.let { pendingBytes = it; showZonePicker = true } },
    )

    // Full-screen photo viewer
    fullScreenPhoto?.let { photo ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = photo.photoPath,
                contentDescription = "${photo.zone} ${formatPhotoDate(photo.date)}",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Text(
                    "${BodyZone.fromString(photo.zone).displayName()} · ${formatPhotoDate(photo.date)}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
            IconButton(
                onClick = { onNavigateToCrop(photo.id) },
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(top = 8.dp, start = 8.dp),
            ) {
                Icon(Icons.Default.Edit, "Editar encuadre", tint = Color.White)
            }
            IconButton(
                onClick = { fullScreenPhoto = null },
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 8.dp, end = 8.dp),
            ) {
                Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = { photoPicker.launch() }) {
                Icon(Icons.Default.Add, "Añadir foto")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(selected = selectedZone == null, onClick = { vm.selectZone(null) }, label = { Text("TODO") })
                BodyZone.entries.forEach { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick = { vm.selectZone(if (selectedZone == zone) null else zone) },
                        label = { Text(zone.displayName().uppercase()) },
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(photos, key = { it.id }) { photo ->
                    @OptIn(ExperimentalFoundationApi::class)
                    AsyncImage(
                        model = photo.photoPath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f).combinedClickable(
                            onClick = { fullScreenPhoto = photo },
                            onLongClick = { photoToDelete = photo },
                        ),
                    )
                }
            }
        }
    }

    photoToDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("¿Eliminar foto?") },
            text = { Text("Esta foto se eliminará permanentemente.") },
            confirmButton = { TextButton(onClick = { vm.deletePhoto(photo); photoToDelete = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { photoToDelete = null }) { Text("Cancelar") } },
        )
    }

    if (showZonePicker && pendingBytes != null) {
        var chosenZone by remember { mutableStateOf(BodyZone.FULL_BODY) }
        AlertDialog(
            onDismissRequest = { showZonePicker = false },
            title = { Text("Zona corporal") },
            text = {
                Column {
                    BodyZone.entries.forEach { zone ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = chosenZone == zone, onClick = { chosenZone = zone })
                            Text(zone.displayName())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingBytes?.let { vm.savePhoto(it, chosenZone) }
                    showZonePicker = false; pendingBytes = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showZonePicker = false }) { Text("Cancelar") } },
        )
    }
}

private fun formatPhotoDate(epochMs: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
}
