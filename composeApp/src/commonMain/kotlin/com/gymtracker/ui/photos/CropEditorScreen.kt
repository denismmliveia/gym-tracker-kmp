package com.gymtracker.ui.photos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.gymtracker.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(photoId: Long, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: CropEditorViewModel = viewModel(key = "crop_$photoId") {
        CropEditorViewModel(container.bodyPhotoRepository, container.imageProcessor, photoId)
    }
    val photo by vm.photo.collectAsStateWithLifecycle()
    val cropRect by vm.cropRect.collectAsStateWithLifecycle()
    val imageBounds by vm.imageBounds.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()

    LaunchedEffect(saved) { if (saved) onBack() }

    val primary = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val touchTargetPx = with(density) { 32.dp.toPx() }
    val minSizePx = with(density) { 80.dp.toPx() }
    var activeHandle by remember { mutableStateOf<DragHandle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar encuadre", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val containerW = constraints.maxWidth.toFloat()
                val containerH = constraints.maxHeight.toFloat()

                photo?.let { p ->
                    AsyncImage(
                        model = p.photoPath,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        onSuccess = { state: AsyncImagePainter.State.Success ->
                            val intrinsic = state.painter.intrinsicSize
                            vm.initCropRect(containerW, containerH, intrinsic.width, intrinsic.height)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                if (imageBounds != null) {
                    Canvas(
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    activeHandle = findNearestHandle(offset.x, offset.y, cropRect, touchTargetPx)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val handle = activeHandle ?: return@detectDragGestures
                                    val bounds = imageBounds ?: return@detectDragGestures
                                    vm.updateRect(applyDrag(cropRect, handle, dragAmount.x, dragAmount.y, bounds, minSizePx))
                                },
                                onDragEnd = { activeHandle = null },
                                onDragCancel = { activeHandle = null },
                            )
                        },
                    ) {
                        val scrim = Color.Black.copy(alpha = 0.55f)
                        val w = size.width; val h = size.height
                        val l = cropRect.left; val t = cropRect.top
                        val r = cropRect.right; val b = cropRect.bottom
                        val cx = (l + r) / 2f; val cy = (t + b) / 2f

                        drawRect(scrim, topLeft = Offset.Zero, size = Size(w, t))
                        drawRect(scrim, topLeft = Offset(0f, b), size = Size(w, h - b))
                        drawRect(scrim, topLeft = Offset(0f, t), size = Size(l, b - t))
                        drawRect(scrim, topLeft = Offset(r, t), size = Size(w - r, b - t))
                        drawRect(primary, topLeft = Offset(l, t), size = Size(r - l, b - t), style = Stroke(width = 2.dp.toPx()))

                        val hs = 10.dp.toPx()
                        listOf(Offset(l, t), Offset(r, t), Offset(l, b), Offset(r, b)).forEach { pos ->
                            drawRect(primary, topLeft = Offset(pos.x - hs / 2, pos.y - hs / 2), size = Size(hs, hs))
                        }
                        val barL = 20.dp.toPx(); val barW2 = 4.dp.toPx()
                        listOf(t, b).forEach { y ->
                            drawRect(primary, topLeft = Offset(cx - barL / 2, y - barW2 / 2), size = Size(barL, barW2))
                        }
                        listOf(l, r).forEach { x ->
                            drawRect(primary, topLeft = Offset(x - barW2 / 2, cy - barL / 2), size = Size(barW2, barL))
                        }
                    }
                }
            }
            Button(
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text("GUARDAR ENCUADRE")
            }
        }
    }
}
