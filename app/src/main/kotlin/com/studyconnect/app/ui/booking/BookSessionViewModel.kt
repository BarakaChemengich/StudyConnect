package com.studyconnect.app.ui.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.data.repository.TutorRepository
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.Validators
import com.studyconnect.app.work.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookSessionState(
    val tutor: Tutor? = null,
    val isReschedule: Boolean = false,
    val startTime: Long? = null,
    val notes: String = "",
    val timeError: String? = null,
    val notesError: String? = null,
    val generalError: String? = null,
    val saving: Boolean = false,
    val done: Boolean = false
)

/** CREATE a booking (bookingId = -1) or UPDATE one (reschedule, bookingId > 0). */
class BookSessionViewModel(
    savedStateHandle: SavedStateHandle,
    private val tutorRepository: TutorRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val tutorId: Long = savedStateHandle.get<Long>("tutorId") ?: -1L
    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: -1L

    private val _state = MutableStateFlow(BookSessionState(isReschedule = bookingId > 0))
    val state: StateFlow<BookSessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val tutor = tutorRepository.findById(tutorId)
            _state.update { it.copy(tutor = tutor) }
            if (bookingId > 0) {
                bookingRepository.findById(bookingId)?.let { b ->
                    _state.update { it.copy(startTime = b.startTime, notes = b.notes) }
                }
            }
        }
    }

    fun onTimePicked(millis: Long) = _state.update { it.copy(startTime = millis, timeError = null, generalError = null) }
    fun onNotes(value: String) = _state.update { it.copy(notes = value, notesError = null) }

    fun confirm() {
        val s = _state.value
        val timeError = Validators.sessionTime(s.startTime, System.currentTimeMillis())
        val notesError = Validators.notes(s.notes)
        if (timeError != null || notesError != null) {
            _state.update { it.copy(timeError = timeError, notesError = notesError) }
            return
        }
        val start = checkNotNull(s.startTime)
        _state.update { it.copy(saving = true, generalError = null) }

        viewModelScope.launch {
            if (bookingId > 0) {
                when (val r = bookingRepository.reschedule(bookingId, start, s.notes)) {
                    is AppResult.Success -> {
                        reminderScheduler.schedule(bookingId, start)
                        _state.update { it.copy(saving = false, done = true) }
                    }
                    is AppResult.Failure -> _state.update { it.copy(saving = false, generalError = r.message) }
                }
            } else {
                when (val r = bookingRepository.book(authRepository.currentUserId(), tutorId, start, s.notes)) {
                    is AppResult.Success -> {
                        reminderScheduler.schedule(r.data, start)
                        _state.update { it.copy(saving = false, done = true) }
                    }
                    is AppResult.Failure -> _state.update { it.copy(saving = false, generalError = r.message) }
                }
            }
        }
    }
}
