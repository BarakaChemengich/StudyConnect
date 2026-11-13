package com.studyconnect.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(millis: Long): String =
        SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(millis))

    fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

    fun formatDateTime(millis: Long): String = "${formatDate(millis)} \u00B7 ${formatTime(millis)}"
}
