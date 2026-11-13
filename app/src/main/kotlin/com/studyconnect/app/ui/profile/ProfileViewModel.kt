package com.studyconnect.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.local.User
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.data.repository.TutorRepository
import com.studyconnect.app.work.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val tutorRepository: TutorRepository,
    bookingRepository: BookingRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val userId = authRepository.currentUserId()

    val user: StateFlow<User?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val myTutors: StateFlow<List<Tutor>> = tutorRepository.observeByOwner(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionCount: StateFlow<Int> = bookingRepository.observeForStudent(userId)
        .map { list -> list.count { it.status == "BOOKED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setAvailability(tutor: Tutor, available: Boolean) {
        viewModelScope.launch { tutorRepository.setAvailability(tutor, available) }
    }

    fun deleteTutor(tutor: Tutor) {
        viewModelScope.launch { tutorRepository.delete(tutor) }
    }

    fun sendTestReminder() = reminderScheduler.sendTestReminder()

    fun logout() = authRepository.logout()
}
