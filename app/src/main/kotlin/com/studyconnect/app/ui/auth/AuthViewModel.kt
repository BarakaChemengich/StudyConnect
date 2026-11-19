package com.studyconnect.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthFormState(
    val loading: Boolean = false,
    val nameError: String? = null,
    val admissionError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val subjectError: String? = null,
    val generalError: String? = null,
    val success: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun login(identifier: String, password: String) {
        val identifierError = Validators.loginIdentifier(identifier)
        val passwordError = if (password.isBlank()) "Password is required" else null
        if ((identifierError != null) || (passwordError != null)) {
            _state.value = AuthFormState(emailError = identifierError, passwordError = passwordError)
            return
        }
        _state.value = AuthFormState(loading = true)
        viewModelScope.launch {
            _state.value = when (val result = repository.login(identifier, password)) {
                is AppResult.Success -> AuthFormState(success = true)
                is AppResult.Failure -> AuthFormState(generalError = result.message)
            }
        }
    }

    fun register(
        name: String,
        admissionNo: String,
        email: String,
        password: String,
        confirm: String,
        registerAsTutor: Boolean = false,
        tutorSubject: String = "",
        tutorBio: String = ""
    ) {
        val subjectErr = if (registerAsTutor) Validators.subject(tutorSubject) else null
        val form = AuthFormState(
            nameError = Validators.name(name),
            admissionError = Validators.admission(admissionNo),
            emailError = Validators.email(email),
            passwordError = Validators.password(password),
            confirmError = Validators.confirmPassword(password, confirm),
            subjectError = subjectErr
        )
        val hasError = listOf(
            form.nameError,
            form.admissionError,
            form.emailError,
            form.passwordError,
            form.confirmError,
            form.subjectError
        ).any { it != null }

        if (hasError) {
            _state.value = form
            return
        }
        _state.value = AuthFormState(loading = true)
        viewModelScope.launch {
            val subjectToSave = if (registerAsTutor) tutorSubject else ""
            val bioToSave = if (registerAsTutor) tutorBio else ""
            _state.value = when (
                val result = repository.register(name, admissionNo, email, password, subjectToSave, bioToSave)
            ) {
                is AppResult.Success -> AuthFormState(success = true)
                is AppResult.Failure -> AuthFormState(generalError = result.message)
            }
        }
    }
}