package com.studyconnect.app.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Schedules a reminder 30 minutes before a session. WorkManager is used because the reminder
 * must still fire after the app is closed or the phone restarts, and it needs no exact-alarm permission.
 */
class ReminderScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun schedule(bookingId: Long, startTime: Long, now: Long = System.currentTimeMillis()) {
        val delay = (startTime - LEAD_TIME_MS - now).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_BOOKING_ID to bookingId))
            .build()
        // REPLACE: rescheduling a session overrides its old reminder
        workManager.enqueueUniqueWork(nameFor(bookingId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(bookingId: Long) {
        workManager.cancelUniqueWork(nameFor(bookingId))
    }

    /** Fires straight away, so the demo can show the notification without waiting. */
    fun sendTestReminder() {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workDataOf(ReminderWorker.KEY_BOOKING_ID to ReminderWorker.TEST_ID))
            .build()
        workManager.enqueue(request)
    }

    private fun nameFor(bookingId: Long) = "reminder_$bookingId"

    companion object {
        val LEAD_TIME_MS = TimeUnit.MINUTES.toMillis(30)
    }
}
