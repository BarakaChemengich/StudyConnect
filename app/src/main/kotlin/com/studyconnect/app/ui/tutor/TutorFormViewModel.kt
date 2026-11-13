package com.studyconnect.app.ui.tutor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.TutorRepository
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TutorFormState(
    val isEdit: Boolean = false,
    val name: String = "",
    val subject: String = "",
    val rating: String = "",
    val bio: String = "",
    val available: Boolean = true,
    val nameError: String? = null,
    val subjectError: String? = null,
    val ratingError: String? = null,
    val bioError: String? = null,
    val generalError: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false
)

/** Handles both CREATE (tutorId = -1) and UPDATE (tutorId > 0) of a tutor profile. */
class TutorFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TutorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val tutorId: Long = savedStateHandle.get<Long>("tutorId") ?: -1L
    private val _state = MutableStateFlow(TutorFormState(isEdit = tutorId > 0))
    val state: StateFlow<TutorFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (tutorId > 0) {
                repository.findById(tutorId)?.let { t ->
                    _state.update {
                        it.copy(name = t.name, subject = t.subject, rating = t.rating.toString(), bio = t.bio, available = t.available)
                    }
                }
            } else {
                authRepository.observeCurrentUser().first()?.let { user ->
                    _state.update { it.copy(name = user.name) }
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun onSubject(v: String) = _state.update { it.copy(subject = v, subjectError = null, generalError = null) }
    fun onRating(v: String) = _state.update { it.copy(rating = v, ratingError = null) }
    fun onBio(v: String) = _state.update { it.copy(bio = v, bioError = null) }
    fun onAvailable(v: Boolean) = _state.update { it.copy(available = v) }

    fun save() {
        val s = _state.value
        val nameError = Validators.name(s.name)
        val subjectError = Validators.subject(s.subject)
        val ratingError = Validators.rating(s.rating)
        val bioError = Validators.bio(s.bio)
        if (listOf(nameError, subjectError, ratingError, bioError).any { it != null }) {
            _state.update {
                it.copy(nameError = nameError, subjectError = subjectError, ratingError = ratingError, bioError = bioError)
            }
            return
        }

        _state.update { it.copy(saving = true, generalError = null) }
        viewModelScope.launch {
            val rating = (Math.round(s.rating.trim().toDouble() * 10) / 10.0)
            val result: AppResult<*> = if (tutorId > 0) {
                val existing = repository.findById(tutorId)
                if (existing == null) {
                    AppResult.Failure("This profile no longer exists")
                } else {
                    repository.update(
                        existing.copy(name = s.name.trim(), subject = s.subject, rating = rating, bio = s.bio.trim(), available = s.available)
                    )
                }
            } else {
                repository.add(
                    Tutor(
                        ownerId = authRepository.currentUserId(),
                        name = s.name.trim(),
                        subject = s.subject,
                        rating = rating,
                        bio = s.bio.trim(),
                        available = s.available
                    )
                )
            }
            when (result) {
                is AppResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is AppResult.Failure -> _state.update { it.copy(saving = false, generalError = result.message) }
            }
        }
    }
}
