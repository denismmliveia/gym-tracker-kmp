package com.gymtracker.ui.exercises

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.gymtracker.LocalAppContainer
import com.gymtracker.Sessions
import com.gymtracker.platform.AudioPermissionEffect
import com.gymtracker.ui.components.SportCard
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.CameraMode
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    bottomPadding: Dp = 0.dp,
) {
    val container = LocalAppContainer.current
    val vm: ExerciseDetailViewModel = viewModel(key = "detail_$exerciseId") {
        ExerciseDetailViewModel(
            exerciseId,
            container.exerciseRepository,
            container.sessionRepository,
            container.voiceRecognizer,
            container.imageProcessor,
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Peekaboo gallery launcher
    val galleryLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { bytes -> bytes.firstOrNull()?.let { vm.setPendingPhoto(it) } },
    )

    // Peekaboo camera state
    var showCamera by remember { mutableStateOf(false) }
    val cameraState = rememberPeekabooCameraState(
        CameraMode.Back,
    ) { bytes ->
        if (bytes != null) vm.setPendingPhoto(bytes)
        showCamera = false
    }

    var showPhotoSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Audio permission
    AudioPermissionEffect(requested = state.needsAudioPermission) {
        vm.clearNeedsAudioPermission()
        vm.startVoice()
    }

    // Camera overlay
    if (showCamera) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            PeekabooCamera(state = cameraState, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = { showCamera = false },
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }
        }
        return
    }

    // Crop frame overlay
    val pendingBytes = state.pendingPhotoBytes
    if (pendingBytes != null) {
        var panX by remember { mutableStateOf(0f) }
        var panY by remember { mutableStateOf(0f) }
        var userScale by remember { mutableStateOf(1f) }
        var viewWidthPx by remember { mutableStateOf(0f) }
        var viewHeightPx by remember { mutableStateOf(0f) }
        val bannerHeightPx = with(LocalDensity.current) { 240.dp.toPx() }

        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { coords ->
                        viewWidthPx = coords.size.width.toFloat()
                        viewHeightPx = coords.size.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            userScale = (userScale * zoom).coerceIn(0.5f, 4f)
                            panX += pan.x
                            panY += pan.y
                        }
                    },
            ) {
                AsyncImage(
                    model = pendingBytes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = userScale
                            scaleY = userScale
                            translationX = panX
                            translationY = panY
                        },
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cropH = bannerHeightPx.coerceAtMost(size.height)
                    val cropTop = (size.height - cropH) / 2f
                    val cropBottom = cropTop + cropH
                    val scrim = Color.Black.copy(alpha = 0.55f)
                    if (cropTop > 0f) {
                        drawRect(scrim, topLeft = Offset.Zero, size = Size(size.width, cropTop))
                        drawRect(scrim, topLeft = Offset(0f, cropBottom), size = Size(size.width, size.height - cropBottom))
                    }
                    drawRect(Color.White, topLeft = Offset(0f, cropTop), size = Size(size.width, cropH), style = Stroke(2.5f))
                    val lineColor = Color.White.copy(alpha = 0.3f)
                    drawLine(lineColor, Offset(size.width / 3f, cropTop), Offset(size.width / 3f, cropBottom))
                    drawLine(lineColor, Offset(size.width * 2f / 3f, cropTop), Offset(size.width * 2f / 3f, cropBottom))
                    drawLine(lineColor, Offset(0f, cropTop + cropH / 3f), Offset(size.width, cropTop + cropH / 3f))
                    drawLine(lineColor, Offset(0f, cropTop + cropH * 2f / 3f), Offset(size.width, cropTop + cropH * 2f / 3f))
                    val m = 16f; val len = 28f
                    drawLine(Color.White, Offset(m, cropTop + m), Offset(m + len, cropTop + m), 2.5f)
                    drawLine(Color.White, Offset(m, cropTop + m), Offset(m, cropTop + m + len), 2.5f)
                    drawLine(Color.White, Offset(size.width - m, cropTop + m), Offset(size.width - m - len, cropTop + m), 2.5f)
                    drawLine(Color.White, Offset(size.width - m, cropTop + m), Offset(size.width - m, cropTop + m + len), 2.5f)
                    drawLine(Color.White, Offset(m, cropBottom - m), Offset(m + len, cropBottom - m), 2.5f)
                    drawLine(Color.White, Offset(m, cropBottom - m), Offset(m, cropBottom - m - len), 2.5f)
                    drawLine(Color.White, Offset(size.width - m, cropBottom - m), Offset(size.width - m - len, cropBottom - m), 2.5f)
                    drawLine(Color.White, Offset(size.width - m, cropBottom - m), Offset(size.width - m, cropBottom - m - len), 2.5f)
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Black.copy(alpha = 0.6f),
                ) {
                    Text(
                        "El recuadro es lo que se guardará · Pellizca y arrastra",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { vm.cancelPhotoFrame() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSavingPhoto,
                ) { Text("Cancelar") }
                Button(
                    onClick = { vm.savePhotoWithFrame(pendingBytes, panX, panY, userScale, viewWidthPx, viewHeightPx) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSavingPhoto,
                ) {
                    if (state.isSavingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar encuadre")
                    }
                }
            }
        }
        return
    }

    // Normal screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((state.exercise?.name ?: "").uppercase()) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        ) {
            item {
                val photoPath = state.exercise?.photoPath
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .clickable { showPhotoSheet = true },
                    contentAlignment = Alignment.Center,
                ) {
                    if (photoPath != null) {
                        AsyncImage(
                            model = photoPath,
                            contentDescription = state.exercise?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Icon(
                            Icons.Default.CameraAlt, null,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                                .padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("Añadir foto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            state.exercise?.description?.let { desc ->
                item {
                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StepperField("SERIES", state.sets.toString(), { vm.setSets(state.sets - 1) }, { vm.setSets(state.sets + 1) })
                        StepperField("REPS", state.reps.toString(), { vm.setReps(state.reps - 1) }, { vm.setReps(state.reps + 1) })
                        StepperField("KG", "%.1f".format(state.weightKg), { vm.setWeight(state.weightKg - 0.5f) }, { vm.setWeight(state.weightKg + 0.5f) })
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { vm.onVoiceButtonClick() }, modifier = Modifier.weight(1f), enabled = !state.isListening) {
                        Icon(Icons.Default.Mic, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isListening) "Escuchando..." else "Voz")
                    }
                    OutlinedButton(onClick = { vm.saveSession() }, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }
            }
            if (state.sessions.isNotEmpty()) {
                item {
                    Text("HISTORIAL", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            items(state.sessions, key = { it.id }) { session ->
                SessionHistoryRow(session = session, onDelete = { vm.deleteSession(session) })
            }
        }
    }

    // Voice dialog
    state.pendingParsed?.let { parsed ->
        var sets by rememberSaveable { mutableStateOf(parsed.sets?.toString() ?: "") }
        var reps by rememberSaveable { mutableStateOf(parsed.reps?.toString() ?: "") }
        var weight by rememberSaveable { mutableStateOf(parsed.weightKg?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { vm.dismissVoiceDialog() },
            title = { Text("Confirmar sesión") },
            text = {
                Column {
                    Text("\"${state.voiceRawText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Series") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Kg") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.confirmVoiceSession(sets.toIntOrNull() ?: 0, reps.toIntOrNull() ?: 0, weight.toFloatOrNull() ?: 0f) }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissVoiceDialog() }) { Text("Cancelar") } },
        )
    }

    // Personal record dialog
    if (state.isPersonalRecord && state.justSaved) {
        AlertDialog(
            onDismissRequest = { vm.dismissRecord() },
            title = { Text("Nuevo record personal") },
            text = { Text("${state.weightKg}kg — tu mejor marca en este ejercicio.") },
            confirmButton = { TextButton(onClick = { vm.dismissRecord() }) { Text("Genial") } },
        )
    }

    // Photo source sheet
    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, sheetState = bottomSheetState) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Cámara") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable { showPhotoSheet = false; showCamera = true },
                )
                ListItem(
                    headlineContent = { Text("Galería") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable { showPhotoSheet = false; galleryLauncher.launch() },
                )
                if (state.exercise?.photoPath != null) {
                    ListItem(
                        headlineContent = { Text("Eliminar foto", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { showPhotoSheet = false; vm.deletePhoto() },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperField(label: String, displayValue: String, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))
        Text(displayValue, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }
            FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionHistoryRow(session: Sessions, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) dismissState.reset()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) {
        SportCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatSessionDate(session.date).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Text(
                    "${session.sets}×${session.reps} · ${"%.1f".format(session.weightKg)}kg".uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun formatSessionDate(epochMs: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
}
