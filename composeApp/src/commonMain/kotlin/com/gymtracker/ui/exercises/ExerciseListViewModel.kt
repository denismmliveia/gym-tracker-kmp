package com.gymtracker.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Exercises
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseUi(
    val exercise: Exercises,
    val lastSets: Long?,
    val lastReps: Long?,
    val lastWeightKg: Double?
)

class ExerciseListViewModel(
    private val groupId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _groupName = MutableStateFlow<String?>(null)
    val groupName: StateFlow<String?> = _groupName.asStateFlow()

    val exercises: StateFlow<List<ExerciseUi>> =
        exerciseRepo.getExercisesForGroup(groupId)
            .map { list ->
                list.map { exercise ->
                    val latest = sessionRepo.getLatestSession(exercise.id)
                    ExerciseUi(exercise, latest?.sets, latest?.reps, latest?.weightKg)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val group = exerciseRepo.getGroupById(groupId)
            _groupName.value = group?.name ?: "—"
        }
    }

    fun deleteExercise(exercise: Exercises) {
        viewModelScope.launch { exerciseRepo.deleteExercise(exercise) }
    }

    fun addExercise(name: String, description: String) {
        viewModelScope.launch {
            exerciseRepo.insertExercise(groupId, name, description)
        }
    }
}
