package com.gymtracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.Muscle_groups
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MuscleGroupUi(val group: Muscle_groups, val isStale: Boolean)

class HomeViewModel(
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val groups: StateFlow<List<MuscleGroupUi>> = exerciseRepo.getAllGroups()
        .map { groups ->
            groups.map { group ->
                val latest = sessionRepo.getLatestSessionForGroup(group.id)
                val isStale = latest == null ||
                    (getCurrentTimeMillis() - latest.date) > 7L * 24 * 60 * 60 * 1000
                MuscleGroupUi(group, isStale)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGroup(name: String) {
        viewModelScope.launch {
            exerciseRepo.insertMuscleGroup(name)
        }
    }
}
