package com.studyconnect.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.data.local.BookingDetail
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.work.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionsUiState(
    val loading: Boolean = true,
    val upcoming: List<BookingDetail> = emptyList(),
    val past: List<BookingDetail> = emptyList(),
    val teaching: List<BookingDetail> = emptyList()
)

class SessionsViewModel(
    private val bookingRepository: BookingRepository,
    authRepository: AuthRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val userId = authRepository.currentUserId()

    val uiState: StateFlow<SessionsUiState> = combine(
        bookingRepository.observeForStudent(userId),
        bookingRepository.observeForTutorOwner(userId)
    ) { mine, teaching ->
        val now = System.currentTimeMillis()
        fun isUpcoming(b: BookingDetail) = b.status == Booking.BOOKED && b.startTime + Booking.DURATION_MS > now
        SessionsUiState(
            loading = false,
            upcoming = mine.filter { isUpcoming(it) },
            past = mine.filterNot { isUpcoming(it) }.sortedByDescending { it.startTime },
            teaching = teaching.filter { isUpcoming(it) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionsUiState())

    /** UPDATE: marks the booking as cancelled and removes its reminder. */
    fun cancel(booking: BookingDetail) {
        viewModelScope.launch {
            bookingRepository.cancel(booking.id)
            reminderScheduler.cancel(booking.id)
        }
    }

    /** DELETE: removes a finished or cancelled session from history. */
    fun delete(booking: BookingDetail) {
        viewModelScope.launch {
            bookingRepository.delete(booking.id)
            reminderScheduler.cancel(booking.id)
        }
    }
}
