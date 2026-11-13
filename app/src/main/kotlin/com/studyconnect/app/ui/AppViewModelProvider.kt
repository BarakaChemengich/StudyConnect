package com.studyconnect.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.studyconnect.app.StudyConnectApp
import com.studyconnect.app.ui.auth.AuthViewModel
import com.studyconnect.app.ui.booking.BookSessionViewModel
import com.studyconnect.app.ui.booking.SessionsViewModel
import com.studyconnect.app.ui.home.HomeViewModel
import com.studyconnect.app.ui.profile.ProfileViewModel
import com.studyconnect.app.ui.resources.ResourcesViewModel
import com.studyconnect.app.ui.tutor.TutorDetailViewModel
import com.studyconnect.app.ui.tutor.TutorFormViewModel

private fun CreationExtras.container() =
    (this[APPLICATION_KEY] as StudyConnectApp).container

/** One place that knows how to build every ViewModel and which repositories it receives. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AuthViewModel(container().authRepository) }
        initializer {
            HomeViewModel(container().tutorRepository, container().bookingRepository, container().authRepository)
        }
        initializer {
            TutorDetailViewModel(createSavedStateHandle(), container().tutorRepository, container().authRepository)
        }
        initializer {
            TutorFormViewModel(createSavedStateHandle(), container().tutorRepository, container().authRepository)
        }
        initializer {
            BookSessionViewModel(
                createSavedStateHandle(),
                container().tutorRepository,
                container().bookingRepository,
                container().authRepository,
                container().reminderScheduler
            )
        }
        initializer {
            SessionsViewModel(container().bookingRepository, container().authRepository, container().reminderScheduler)
        }
        initializer { ResourcesViewModel(container().resourceRepository) }
        initializer {
            ProfileViewModel(
                container().authRepository,
                container().tutorRepository,
                container().bookingRepository,
                container().reminderScheduler
            )
        }
    }
}
