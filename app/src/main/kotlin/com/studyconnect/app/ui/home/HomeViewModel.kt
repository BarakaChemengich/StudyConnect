package com.studyconnect.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.data.local.BookingDetail
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.data.repository.TutorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val tutors: List<Tutor>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    tutorRepository: TutorRepository,
    bookingRepository: BookingRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val query = MutableStateFlow("")
    val subject = MutableStateFlow<String?>(null)      // null means "All"
    val availableOnly = MutableStateFlow(false)

    fun onQueryChange(value: String) { query.value = value }
    fun onSubjectSelected(value: String?) { subject.value = value }
    fun onAvailableOnlyChange(value: Boolean) { availableOnly.value = value }

    val userName: StateFlow<String> = authRepository.observeCurrentUser()
        .map { it?.name?.trim()?.substringBefore(' ').orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /** The student's soonest booked session, shown in the banner. */
    val nextSession: StateFlow<BookingDetail?> = bookingRepository
        .observeForStudent(authRepository.currentUserId())
        .map { list ->
            val now = System.currentTimeMillis()
            list.firstOrNull { it.status == Booking.BOOKED && it.startTime + Booking.DURATION_MS > now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val retryTick = MutableStateFlow(0)
    fun retry() { retryTick.value += 1 }

    /** Re-queries Room whenever the search text changes, then applies the chip filters. */
    val uiState: StateFlow<HomeUiState> = combine(query, subject, availableOnly, retryTick) { q, s, a, _ ->
        Triple(q, s, a)
    }
        .flatMapLatest { (q, s, a) ->
            tutorRepository.search(q)
                .map { list ->
                    list.filter { tutor ->
                        (s == null || tutor.subject.equals(s, ignoreCase = true)) && (!a || tutor.available)
                    }
                }
                .map<List<Tutor>, HomeUiState> { HomeUiState.Success(it) }
                .catch { emit(HomeUiState.Error(it.message ?: "Could not load tutors")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
