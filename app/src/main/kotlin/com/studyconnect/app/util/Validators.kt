package com.studyconnect.app.util

/** Pure Kotlin input validation. Each function returns an error message, or null when the input is valid. */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val ADMISSION = Regex("^[A-Za-z0-9]{4,10}$")

    fun name(value: String): String? = when {
        value.isBlank() -> "Name is required"
        value.trim().length < 2 -> "Name is too short"
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !EMAIL.matches(value.trim()) -> "That email doesn't look right"
        else -> null
    }

    fun loginIdentifier(value: String): String? = when {
        value.isBlank() -> "Email or admission number is required"
        value.trim().length < 3 -> "Enter a valid email or admission number"
        else -> null
    }

    fun admission(value: String): String? = when {
        value.isBlank() -> "Admission number is required"
        !ADMISSION.matches(value.trim()) -> "Use 4-10 letters or digits"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < 6 -> "Use at least 6 characters"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Please confirm your password"
        password != confirm -> "Passwords do not match"
        else -> null
    }

    fun subject(value: String): String? = if (value.isBlank()) "Choose a subject" else null

    fun rating(value: String): String? {
        if (value.isBlank()) return "Rating is required"
        val number = value.trim().toDoubleOrNull() ?: return "Enter a valid number"
        return if (number.isNaN() || number < 0.0 || number > 5.0) "Rating must be between 0 and 5" else null
    }

    fun bio(value: String): String? = if (value.length > 200) "Keep it under 200 characters" else null

    fun notes(value: String): String? = if (value.length > 200) "Keep it under 200 characters" else null

    fun sessionTime(startMillis: Long?, now: Long): String? = when {
        startMillis == null -> "Pick a date and time"
        startMillis <= now -> "Choose a time in the future"
        else -> null
    }
}
