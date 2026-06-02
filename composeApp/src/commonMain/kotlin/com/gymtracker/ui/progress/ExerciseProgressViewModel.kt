package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*

data class ExerciseProgressState(
    val exerciseName: String,
    val dayScores: List<DayScore>,
    val metric: ProgressMetric,
    val isLoading: Boolean = false
)

class ExerciseProgressViewModel(
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseId: Long
) : ViewModel() {

    private val _metric = MutableStateFlow(ProgressMetric.MAX_WEIGHT)

    val state: StateFlow<ExerciseProgressState> = combine(
        _metric,
        sessionRepository.getSessionsForExercise(exerciseId)
    ) { metric, sessions ->
        ExerciseProgressState(
            exerciseName = exerciseRepository.getExerciseById(exerciseId)?.name ?: "",
            dayScores = sessions.toDayScores(metric),
            metric = metric
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseProgressState("", emptyList(), ProgressMetric.MAX_WEIGHT, isLoading = true)
    )

    fun setMetric(metric: ProgressMetric) {
        _metric.value = metric
    }
}
