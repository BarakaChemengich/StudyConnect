package com.studyconnect.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A registered student. Used for login and as the owner of tutor profiles. */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true), Index(value = ["admissionNo"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val admissionNo: String,
    val email: String,
    val passwordHash: String
)

/** A tutor profile: one student offering help in one subject. */
@Entity(
    tableName = "tutors",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class Tutor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val subject: String,
    val rating: Double,
    val bio: String = "",
    val available: Boolean = true
)

/** A one-hour tutoring session booked by a student with a tutor. */
@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(entity = Tutor::class, parentColumns = ["id"], childColumns = ["tutorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("tutorId"), Index("studentId")]
)
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val tutorId: Long,
    val startTime: Long,
    val notes: String = "",
    val status: String = BOOKED
) {
    companion object {
        const val BOOKED = "BOOKED"
        const val CANCELLED = "CANCELLED"
        const val DURATION_MS = 60 * 60 * 1000L
    }
}

/** Result of a JOIN query: a booking together with the tutor and student names. Not a table. */
data class BookingDetail(
    val id: Long,
    val studentId: Long,
    val tutorId: Long,
    val tutorName: String,
    val subject: String,
    val startTime: Long,
    val notes: String,
    val status: String,
    val studentName: String
)
