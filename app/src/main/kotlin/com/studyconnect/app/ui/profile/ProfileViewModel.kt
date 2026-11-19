package com.studyconnect.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.local.User
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.data.repository.TutorRepository
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.Validators
import com.studyconnect.app.work.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val tutorRepository: TutorRepository,
    bookingRepository: BookingRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val user: StateFlow<User?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val myTutors: StateFlow<List<Tutor>> = user
        .flatMapLatest { currentUser ->
            if (currentUser == null) flowOf(emptyList())
            else tutorRepository.observeByOwner(currentUser.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionCount: StateFlow<Int> = user
        .flatMapLatest { currentUser ->
            if (currentUser == null) flowOf(0)
            else bookingRepository.observeForStudent(currentUser.id)
                .map { list -> list.count { it.status == "BOOKED" } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _profileMessage = MutableStateFlow<String?>(null)
    val profileMessage: StateFlow<String?> = _profileMessage.asStateFlow()

    fun clearProfileMessage() {
        _profileMessage.value = null
    }

    fun updateProfile(newName: String, newEmail: String) {
        val current = user.value ?: return
        val nameErr = Validators.name(newName)
        val emailErr = Validators.email(newEmail)
        if (nameErr != null || emailErr != null) {
            _profileMessage.value = nameErr ?: emailErr
            return
        }
        viewModelScope.launch {
            _profileMessage.value = when (val res = authRepository.updateProfile(current.id, newName, newEmail)) {
                is AppResult.Success -> "Profile updated successfully"
                is AppResult.Failure -> res.message
            }
        }
    }

    fun setAvailability(tutor: Tutor, available: Boolean) {
        viewModelScope.launch { tutorRepository.setAvailability(tutor, available) }
    }

    fun deleteTutor(tutor: Tutor) {
        viewModelScope.launch { tutorRepository.delete(tutor) }
    }

    fun sendTestReminder() = reminderScheduler.sendTestReminder()

    fun logout() = authRepository.logout()
}