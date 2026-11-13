package com.studyconnect.app.ui.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyconnect.app.data.repository.Resource
import com.studyconnect.app.data.repository.ResourceRepository
import com.studyconnect.app.util.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ResourcesUiState {
    data object Loading : ResourcesUiState
    data class Success(val items: List<Resource>) : ResourcesUiState
    data object Empty : ResourcesUiState
    data class Error(val message: String) : ResourcesUiState
}

class ResourcesViewModel(private val repository: ResourceRepository) : ViewModel() {

    private val _subject = MutableStateFlow("Statistics")
    val subject: StateFlow<String> = _subject.asStateFlow()

    private val _state = MutableStateFlow<ResourcesUiState>(ResourcesUiState.Loading)
    val state: StateFlow<ResourcesUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init { load() }

    fun select(subject: String) {
        _subject.value = subject
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = ResourcesUiState.Loading
            _state.value = when (val result = repository.search(_subject.value)) {
                is AppResult.Success ->
                    if (result.data.isEmpty()) ResourcesUiState.Empty else ResourcesUiState.Success(result.data)
                is AppResult.Failure -> ResourcesUiState.Error(result.message)
            }
        }
    }
}
