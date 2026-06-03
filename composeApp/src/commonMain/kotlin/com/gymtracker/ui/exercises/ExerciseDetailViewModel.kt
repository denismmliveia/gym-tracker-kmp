package com.gymtracker.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Exercises
import com.gymtracker.Sessions
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.domain.voice.ParsedSession
import com.gymtracker.domain.voice.SessionParser
import com.gymtracker.domain.voice.VoiceResult
import com.gymtracker.platform.ImageProcessor
import com.gymtracker.platform.VoiceRecognizer
import com.gymtracker.platform.deleteFile
import com.gymtracker.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val exercise: Exercises? = null,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isPersonalRecord: Boolean = false,
    val isListening: Boolean = false,
    val needsAudioPermission: Boolean = false,
    val pendingParsed: ParsedSession? = null,
    val voiceRawText: String = "",
    val justSaved: Boolean = false,
    val isSaving: Boolean = false,
    val photoVersion: Long = 0L,
    val pendingPhotoBytes: ByteArray? = null,
    val isSavingPhoto: Boolean = false,
    val sessions: List<Sessions> = emptyList(),
)

class ExerciseDetailViewModel(
    private val exerciseId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository,
    private val voiceRecognizer: VoiceRecognizer,
    private val imageProcessor: ImageProcessor,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val exercise = exerciseRepo.getExerciseById(exerciseId)
                val latest = sessionRepo.getLatestSession(exerciseId)
                _state.update {
                    it.copy(
                        exercise = exercise,
                        sets = latest?.sets?.toInt() ?: 3,
                        reps = latest?.reps?.toInt() ?: 10,
                        weightKg = latest?.weightKg?.toFloat() ?: 0f
                    )
                }
                sessionRepo.getSessionsForExercise(exerciseId).collect { list ->
                    _state.update { it.copy(sessions = list) }
                }
            } catch (_: Exception) {}
        }
    }

    fun deleteSession(session: Sessions) {
        viewModelScope.launch { sessionRepo.deleteSession(session) }
    }

    fun setSets(value: Int) = _state.update { it.copy(sets = maxOf(1, value)) }
    fun setReps(value: Int) = _state.update { it.copy(reps = maxOf(1, value)) }
    fun setWeight(value: Float) = _state.update { it.copy(weightKg = maxOf(0f, value)) }

    fun saveSession() {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val maxWeight = sessionRepo.getMaxWeight(exerciseId) ?: 0.0
            val isRecord = s.weightKg.toDouble() > maxWeight
            sessionRepo.insertSession(
                exerciseId = exerciseId,
                date = getCurrentTimeMillis(),
                sets = s.sets.toLong(),
                reps = s.reps.toLong(),
                weightKg = s.weightKg.toDouble()
            )
            _state.update { it.copy(isSaving = false, isPersonalRecord = isRecord, justSaved = isRecord) }
        }
    }

    fun onVoiceButtonClick() {
        _state.update { it.copy(needsAudioPermission = true) }
    }

    fun clearNeedsAudioPermission() {
        _state.update { it.copy(needsAudioPermission = false) }
    }

    fun startVoice() {
        viewModelScope.launch {
            _state.update { it.copy(isListening = true) }
            voiceRecognizer.listen().collect { result ->
                when (result) {
                    is VoiceResult.Success -> {
                        val parsed = SessionParser().parse(result.text)
                        _state.update {
                            it.copy(isListening = false, pendingParsed = parsed, voiceRawText = result.text)
                        }
                    }
                    is VoiceResult.Error -> _state.update { it.copy(isListening = false) }
                }
            }
        }
    }

    fun confirmVoiceSession(sets: Int, reps: Int, weightKg: Float) {
        setSets(sets); setReps(reps); setWeight(weightKg)
        _state.update { it.copy(pendingParsed = null) }
        saveSession()
    }

    fun dismissVoiceDialog() = _state.update { it.copy(pendingParsed = null) }
    fun dismissRecord() = _state.update { it.copy(isPersonalRecord = false, justSaved = false) }

    fun setPendingPhoto(bytes: ByteArray) {
        _state.update { it.copy(pendingPhotoBytes = bytes) }
    }

    fun cancelPhotoFrame() {
        _state.update { it.copy(pendingPhotoBytes = null) }
    }

    fun deletePhoto() {
        viewModelScope.launch {
            val exercise = _state.value.exercise ?: return@launch
            exercise.photoPath?.let { deleteFile(it) }
            exerciseRepo.updatePhotoPath(exerciseId, null)
            val updated = exerciseRepo.getExerciseById(exerciseId)
            _state.update { it.copy(exercise = updated, photoVersion = getCurrentTimeMillis()) }
        }
    }

    fun savePhotoWithFrame(
        bytes: ByteArray,
        panX: Float, panY: Float, userScale: Float,
        viewW: Float, viewH: Float
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSavingPhoto = true) }
            val path = imageProcessor.saveFramedPhoto(
                bytes, panX, panY, userScale, viewW, viewH,
                filename = "exercise_${exerciseId}.jpg"
            )
            if (path.isEmpty()) {
                _state.update { it.copy(isSavingPhoto = false) }
                return@launch
            }
            exerciseRepo.updatePhotoPath(exerciseId, path)
            val updated = exerciseRepo.getExerciseById(exerciseId)
            _state.update {
                it.copy(
                    exercise = updated,
                    photoVersion = getCurrentTimeMillis(),
                    pendingPhotoBytes = null,
                    isSavingPhoto = false
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(isListening = false) }
    }
}
