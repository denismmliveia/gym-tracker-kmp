package com.gymtracker.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Body_photos
import com.gymtracker.data.repository.BodyPhotoRepository
import com.gymtracker.platform.ImageProcessor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// CropRectPx, DragHandle, findNearestHandle, applyDrag are defined in CropGeometry.kt

class CropEditorViewModel(
    private val repo: BodyPhotoRepository,
    private val imageProcessor: ImageProcessor,
    private val photoId: Long,
) : ViewModel() {

    private val _photo = MutableStateFlow<Body_photos?>(null)
    val photo: StateFlow<Body_photos?> = _photo.asStateFlow()

    private val _cropRect = MutableStateFlow(CropRectPx(0f, 0f, 0f, 0f))
    val cropRect: StateFlow<CropRectPx> = _cropRect.asStateFlow()

    private val _imageBounds = MutableStateFlow<CropRectPx?>(null)
    val imageBounds: StateFlow<CropRectPx?> = _imageBounds.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            _photo.value = repo.getPhotoById(photoId)
        }
    }

    fun initCropRect(containerW: Float, containerH: Float, intrinsicW: Float, intrinsicH: Float) {
        if (_imageBounds.value != null || intrinsicW == 0f || intrinsicH == 0f) return
        val scale = minOf(containerW / intrinsicW, containerH / intrinsicH)
        val renderedW = intrinsicW * scale
        val renderedH = intrinsicH * scale
        val offsetX = (containerW - renderedW) / 2f
        val offsetY = (containerH - renderedH) / 2f
        val bounds = CropRectPx(offsetX, offsetY, offsetX + renderedW, offsetY + renderedH)
        _imageBounds.value = bounds
        val padX = renderedW * 0.2f
        val padY = renderedH * 0.2f
        _cropRect.value = CropRectPx(bounds.left + padX, bounds.top + padY, bounds.right - padX, bounds.bottom - padY)
    }

    fun updateRect(new: CropRectPx) { _cropRect.value = new }

    fun save() {
        val bounds = _imageBounds.value ?: return
        val rect = _cropRect.value
        val p = _photo.value ?: return
        val renderedW = bounds.right - bounds.left
        val renderedH = bounds.bottom - bounds.top
        if (renderedW == 0f || renderedH == 0f) return

        val relLeft   = (rect.left   - bounds.left) / renderedW
        val relTop    = (rect.top    - bounds.top)  / renderedH
        val relRight  = (rect.right  - bounds.left) / renderedW
        val relBottom = (rect.bottom - bounds.top)  / renderedH

        viewModelScope.launch {
            val newPath = imageProcessor.cropAndReplacePhoto(p.photoPath, relLeft, relTop, relRight, relBottom)
            if (newPath.isNotEmpty()) {
                repo.updatePhotoPath(photoId, newPath)
                _saved.value = true
            }
        }
    }
}
