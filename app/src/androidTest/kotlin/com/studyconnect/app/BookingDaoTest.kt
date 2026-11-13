package com.studyconnect.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studyconnect.app.data.local.AppDatabase
import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.local.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a device/emulator against an in-memory Room database (no seed data). */
@RunWith(AndroidJUnit4::class)
class BookingDaoTest {
    private lateinit var db: AppDatabase
    private var studentId = 0L
    private var tutorId = 0L

    @Before fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val owner = db.userDao().insert(User(name = "Owner", admissionNo = "OWN1", email = "o@x.com", passwordHash = "h"))
        studentId = db.userDao().insert(User(name = "Student", admissionNo = "STU1", email = "s@x.com", passwordHash = "h"))
        tutorId = db.tutorDao().insert(Tutor(ownerId = owner, name = "Tina", subject = "Statistics", rating = 4.5))
    }

    @After fun tearDown() = db.close()

    @Test fun insertThenRead() = runBlocking {
        val id = db.bookingDao().insert(Booking(studentId = studentId, tutorId = tutorId, startTime = 10_000_000L))
        assertEquals("Tina", db.bookingDao().findDetail(id)?.tutorName)
    }

    @Test fun update_changesStatus() = runBlocking {
        val id = db.bookingDao().insert(Booking(studentId = studentId, tutorId = tutorId, startTime = 10_000_000L))
        val booking = db.bookingDao().findById(id)!!
        db.bookingDao().update(booking.copy(status = Booking.CANCELLED))
        assertEquals(Booking.CANCELLED, db.bookingDao().findById(id)?.status)
    }

    @Test fun delete_removesRow() = runBlocking {
        val id = db.bookingDao().insert(Booking(studentId = studentId, tutorId = tutorId, startTime = 10_000_000L))
        db.bookingDao().delete(db.bookingDao().findById(id)!!)
        assertNull(db.bookingDao().findById(id))
    }

    @Test fun overlappingSlot_isDetectedAsConflict() = runBlocking {
        val start = 10_000_000L
        db.bookingDao().insert(Booking(studentId = studentId, tutorId = tutorId, startTime = start))
        // 30 minutes later overlaps the 1-hour session; 2 hours later does not
        assertEquals(1, db.bookingDao().countTutorConflicts(tutorId, start + 1_800_000, start + 5_400_000, 0))
        assertEquals(0, db.bookingDao().countTutorConflicts(tutorId, start + 7_200_000, start + 10_800_000, 0))
    }

    @Test fun deletingTutor_cascadesToBookings() = runBlocking {
        val id = db.bookingDao().insert(Booking(studentId = studentId, tutorId = tutorId, startTime = 10_000_000L))
        db.tutorDao().delete(db.tutorDao().findById(tutorId)!!)
        assertNull(db.bookingDao().findById(id))
    }
}
