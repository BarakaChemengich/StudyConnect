package com.studyconnect.app.ui.tutor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.TutorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TutorDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TutorRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val tutorId: Long = savedStateHandle.get<Long>("tutorId") ?: -1L
    val currentUserId: Long = authRepository.currentUserId()

    val tutor: StateFlow<Tutor?> = repository.observeById(tutorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDone: () -> Unit) {
        val current = tutor.value ?: return
        viewModelScope.launch {
            repository.delete(current)
            onDone()
        }
    }
}
