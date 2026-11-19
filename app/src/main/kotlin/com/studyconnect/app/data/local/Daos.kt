package com.studyconnect.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE admissionNo = :admissionNo LIMIT 1")
    suspend fun findByAdmission(admissionNo: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR UPPER(admissionNo) = UPPER(:identifier) LIMIT 1")
    suspend fun findByEmailOrAdmission(identifier: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: Long): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): Flow<User?>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun observeAllUsers(): Flow<List<User>>
}

@Dao
interface TutorDao {
    @Insert
    suspend fun insert(tutor: Tutor): Long

    @Update
    suspend fun update(tutor: Tutor)

    @Delete
    suspend fun delete(tutor: Tutor)

    @Query("UPDATE tutors SET name = :newName WHERE ownerId = :ownerId")
    suspend fun updateTutorNameByOwner(ownerId: Long, newName: String)

    @Query("SELECT * FROM tutors WHERE id = :id")
    suspend fun findById(id: Long): Tutor?

    @Query("SELECT * FROM tutors WHERE id = :id")
    fun observeById(id: Long): Flow<Tutor?>

    @Query(
        "SELECT * FROM tutors WHERE name LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' " +
                "ORDER BY available DESC, rating DESC, name ASC"
    )
    fun search(query: String): Flow<List<Tutor>>

    @Query("SELECT * FROM tutors WHERE ownerId = :ownerId ORDER BY subject ASC")
    fun observeByOwner(ownerId: Long): Flow<List<Tutor>>

    @Query(
        "SELECT COUNT(*) FROM tutors WHERE ownerId = :ownerId " +
                "AND LOWER(subject) = LOWER(:subject) AND id != :excludeId"
    )
    suspend fun countDuplicate(ownerId: Long, subject: String, excludeId: Long): Int
}

private const val DETAIL_SELECT =
    "SELECT b.id AS id, b.studentId AS studentId, b.tutorId AS tutorId, t.name AS tutorName, " +
            "t.subject AS subject, b.startTime AS startTime, b.notes AS notes, b.status AS status, " +
            "s.name AS studentName FROM bookings b " +
            "INNER JOIN tutors t ON t.id = b.tutorId " +
            "INNER JOIN users s ON s.id = b.studentId "

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: Booking): Long

    @Update
    suspend fun update(booking: Booking)

    @Delete
    suspend fun delete(booking: Booking)

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun findById(id: Long): Booking?

    @Query(DETAIL_SELECT + "WHERE b.id = :id")
    suspend fun findDetail(id: Long): BookingDetail?

    @Query(DETAIL_SELECT + "WHERE b.studentId = :studentId ORDER BY b.startTime ASC")
    fun observeForStudent(studentId: Long): Flow<List<BookingDetail>>

    @Query(DETAIL_SELECT + "WHERE t.ownerId = :ownerId ORDER BY b.startTime ASC")
    fun observeForTutorOwner(ownerId: Long): Flow<List<BookingDetail>>

    /** Counts BOOKED sessions of this tutor that overlap [start, end). */
    @Query(
        "SELECT COUNT(*) FROM bookings WHERE tutorId = :tutorId AND status = 'BOOKED' " +
                "AND startTime < :end AND (startTime + 3600000) > :start AND id != :excludeId"
    )
    suspend fun countTutorConflicts(tutorId: Long, start: Long, end: Long, excludeId: Long): Int

    /** Counts BOOKED sessions of this student that overlap [start, end). */
    @Query(
        "SELECT COUNT(*) FROM bookings WHERE studentId = :studentId AND status = 'BOOKED' " +
                "AND startTime < :end AND (startTime + 3600000) > :start AND id != :excludeId"
    )
    suspend fun countStudentConflicts(studentId: Long, start: Long, end: Long, excludeId: Long): Int
}