package com.studyconnect.app.util

/** Simple success/failure wrapper so the UI never has to catch exceptions itself. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val message: String) : AppResult<Nothing>
}
