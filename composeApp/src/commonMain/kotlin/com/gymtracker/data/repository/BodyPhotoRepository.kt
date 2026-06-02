package com.gymtracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gymtracker.data.db.Body_photos
import com.gymtracker.data.db.GymTrackerDatabase
import com.gymtracker.data.entity.BodyZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BodyPhotoRepository(private val db: GymTrackerDatabase) {

    fun getAllPhotos(): Flow<List<Body_photos>> =
        db.bodyPhotosQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getPhotosByZone(zone: BodyZone): Flow<List<Body_photos>> =
        db.bodyPhotosQueries.selectByZone(zone.name)
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun getPhotoById(id: Long): Body_photos? = withContext(Dispatchers.IO) {
        db.bodyPhotosQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun insertPhoto(date: Long, zone: BodyZone, photoPath: String): Long =
        withContext(Dispatchers.IO) {
            db.bodyPhotosQueries.transactionWithResult {
                db.bodyPhotosQueries.insert(date, zone.name, photoPath)
                db.bodyPhotosQueries.selectAll().executeAsList().first().id
            }
        }

    suspend fun updatePhotoPath(photoId: Long, path: String) = withContext(Dispatchers.IO) {
        db.bodyPhotosQueries.updatePhotoPath(path, photoId)
    }

    suspend fun deletePhoto(photo: Body_photos) = withContext(Dispatchers.IO) {
        db.bodyPhotosQueries.delete(photo.id)
    }
}
