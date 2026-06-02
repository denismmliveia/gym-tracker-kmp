package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Exercises
import com.gymtracker.Muscle_groups
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*

data class ExerciseProgressSummary(
    val exercise: Exercises,
    val dayScores: List<DayScore>,
    val improvementPct: Double?
)

data class GroupProgressUi(
    val group: Muscle_groups,
    val exercises: List<ExerciseProgressSummary>
)

class ProgressViewModel(
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val groupProgress: StateFlow<List<GroupProgressUi>> =
        exerciseRepo.getAllGroups()
            .map { groups ->
                groups.map { group ->
                    val exercises = exerciseRepo.getExercisesForGroup(group.id).first()
                    val summaries = exercises.map { exercise ->
                        val dayScores = sessionRepo.getSessionsForExercise(exercise.id)
                            .first()
                            .toDayScores(ProgressMetric.VOLUME)
                        val improvementPct = if (dayScores.size >= 2) {
                            val first = dayScores.first().value
                            val last = dayScores.last().value
                            if (first > 0.0) ((last - first) / first) * 100.0 else null
                        } else null
                        ExerciseProgressSummary(exercise, dayScores.takeLast(8), improvementPct)
                    }.filter { it.dayScores.isNotEmpty() }
                    GroupProgressUi(group, summaries)
                }.filter { it.exercises.isNotEmpty() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
