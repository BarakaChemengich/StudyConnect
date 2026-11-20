package com.studyconnect.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyconnect.app.StudyConnectApp
import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.util.DateUtils

/**
 * Runs in the background (even if the app is closed) and shows a reminder notification
 * for one booked session. It re-reads the booking so a cancelled session never notifies.
 */
class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val bookingId = inputData.getLong(KEY_BOOKING_ID, -1L)

        if (bookingId == TEST_ID) {
            NotificationHelper.show(
                applicationContext, 0, "StudyConnect reminders work",
                "You will get a reminder like this before each booked session."
            )
            return Result.success()
        }

        val app = applicationContext as StudyConnectApp
        val booking = app.container.bookingRepository.findDetail(bookingId) ?: return Result.success()

        if (booking.status != Booking.BOOKED) return Result.success()
        if (booking.startTime <= System.currentTimeMillis()) return Result.success()

        NotificationHelper.show(
            applicationContext,
            bookingId.toInt(),
            "Upcoming session: ${booking.subject}",
            "With ${booking.tutorName} at ${DateUtils.formatTime(booking.startTime)}"
        )
        return Result.success()
    }

    companion object {
        const val KEY_BOOKING_ID = "booking_id"
        const val TEST_ID = -99L
    }
}
