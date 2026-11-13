package com.studyconnect.app.data.repository

import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.data.local.BookingDao
import com.studyconnect.app.data.local.BookingDetail
import com.studyconnect.app.data.local.TutorDao
import com.studyconnect.app.util.AppResult
import kotlinx.coroutines.flow.Flow

class BookingRepository(private val bookingDao: BookingDao, private val tutorDao: TutorDao) {

    // READ
    fun observeForStudent(studentId: Long): Flow<List<BookingDetail>> = bookingDao.observeForStudent(studentId)
    fun observeForTutorOwner(ownerId: Long): Flow<List<BookingDetail>> = bookingDao.observeForTutorOwner(ownerId)
    suspend fun findById(id: Long): Booking? = bookingDao.findById(id)
    suspend fun findDetail(id: Long): BookingDetail? = bookingDao.findDetail(id)

    // CREATE: applies the business rules that stop double-booking
    suspend fun book(studentId: Long, tutorId: Long, startTime: Long, notes: String): AppResult<Long> {
        val tutor = tutorDao.findById(tutorId) ?: return AppResult.Failure("This tutor no longer exists")
        if (!tutor.available) return AppResult.Failure("${tutor.name} is not available right now")
        if (tutor.ownerId == studentId) return AppResult.Failure("You cannot book your own tutor profile")
        checkConflicts(studentId, tutorId, startTime, excludeId = 0)?.let { return AppResult.Failure(it) }
        return AppResult.Success(
            bookingDao.insert(Booking(studentId = studentId, tutorId = tutorId, startTime = startTime, notes = notes.trim()))
        )
    }

    // UPDATE: reschedule and/or edit notes
    suspend fun reschedule(bookingId: Long, newStart: Long, notes: String): AppResult<Unit> {
        val booking = bookingDao.findById(bookingId) ?: return AppResult.Failure("This session no longer exists")
        checkConflicts(booking.studentId, booking.tutorId, newStart, excludeId = bookingId)?.let {
            return AppResult.Failure(it)
        }
        bookingDao.update(booking.copy(startTime = newStart, notes = notes.trim()))
        return AppResult.Success(Unit)
    }

    // UPDATE: cancel keeps the record in history
    suspend fun cancel(bookingId: Long) {
        val booking = bookingDao.findById(bookingId) ?: return
        bookingDao.update(booking.copy(status = Booking.CANCELLED))
    }

    // DELETE: remove the record completely
    suspend fun delete(bookingId: Long) {
        val booking = bookingDao.findById(bookingId) ?: return
        bookingDao.delete(booking)
    }

    private suspend fun checkConflicts(studentId: Long, tutorId: Long, start: Long, excludeId: Long): String? {
        val end = start + Booking.DURATION_MS
        if (bookingDao.countTutorConflicts(tutorId, start, end, excludeId) > 0) {
            return "That tutor is already booked at this time. Pick another slot."
        }
        if (bookingDao.countStudentConflicts(studentId, start, end, excludeId) > 0) {
            return "You already have a session at this time."
        }
        return null
    }
}
