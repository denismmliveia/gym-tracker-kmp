package com.gymtracker.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Body_photos
import com.gymtracker.data.entity.BodyZone
import com.gymtracker.data.repository.BodyPhotoRepository
import com.gymtracker.platform.ImageProcessor
import com.gymtracker.platform.deleteFile
import com.gymtracker.platform.getCurrentTimeMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModel(
    private val repo: BodyPhotoRepository,
    private val imageProcessor: ImageProcessor,
) : ViewModel() {

    private val _selectedZone = MutableStateFlow<BodyZone?>(null)

    val photos: StateFlow<List<Body_photos>> = _selectedZone.flatMapLatest { zone ->
        if (zone == null) repo.getAllPhotos() else repo.getPhotosByZone(zone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedZone: StateFlow<BodyZone?> = _selectedZone.asStateFlow()

    private val _navigateToCrop = MutableStateFlow<Long?>(null)
    val navigateToCrop: StateFlow<Long?> = _navigateToCrop.asStateFlow()

    fun selectZone(zone: BodyZone?) { _selectedZone.value = zone }

    fun savePhoto(bytes: ByteArray, zone: BodyZone) {
        viewModelScope.launch {
            val filename = "body_${getCurrentTimeMillis()}.jpg"
            val path = imageProcessor.saveRawBytes(bytes, filename)
            if (path.isNotEmpty()) {
                val photoId = repo.insertPhoto(getCurrentTimeMillis(), zone, path)
                _navigateToCrop.value = photoId
            }
        }
    }

    fun cropNavigationHandled() { _navigateToCrop.value = null }

    fun deletePhoto(photo: Body_photos) {
        viewModelScope.launch {
            deleteFile(photo.photoPath)
            repo.deletePhoto(photo)
        }
    }
}
