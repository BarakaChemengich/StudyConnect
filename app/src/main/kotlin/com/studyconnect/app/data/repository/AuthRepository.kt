package com.studyconnect.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.studyconnect.app.data.SessionManager
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.local.TutorDao
import com.studyconnect.app.data.local.User
import com.studyconnect.app.data.local.UserDao
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val userDao: UserDao,
    private val tutorDao: TutorDao,
    private val session: SessionManager
) {

    fun isLoggedIn(): Boolean = session.isLoggedIn()
    fun currentUserId(): Long = session.userId()
    fun observeCurrentUser(): Flow<User?> = userDao.observeById(session.userId())
    fun logout() = session.clear()

    /**
     * Registers a new student. If [tutorSubject] is provided (non-blank),
     * also creates their initial Tutor profile so they appear dynamically in the tutor list.
     */
    suspend fun register(
        name: String,
        admissionNo: String,
        email: String,
        password: String,
        tutorSubject: String = "",
        tutorBio: String = ""
    ): AppResult<User> {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanAdmission = admissionNo.trim().uppercase()

        if (userDao.findByEmail(cleanEmail) != null) {
            return AppResult.Failure("An account with this email already exists")
        }
        if (userDao.findByAdmission(cleanAdmission) != null) {
            return AppResult.Failure("This admission number is already registered")
        }
        return try {
            val id = userDao.insert(
                User(
                    name = cleanName,
                    admissionNo = cleanAdmission,
                    email = cleanEmail,
                    passwordHash = PasswordHasher.hash(cleanEmail, password)
                )
            )
            // Automatically list the new user as a tutor if they entered a subject during signup
            if (tutorSubject.isNotBlank()) {
                tutorDao.insert(
                    Tutor(
                        ownerId = id,
                        name = cleanName,
                        subject = tutorSubject.trim(),
                        rating = 5.0,
                        bio = tutorBio.trim().ifBlank { "Peer tutor for ${tutorSubject.trim()}" },
                        available = true
                    )
                )
            }
            session.saveUser(id)
            AppResult.Success(checkNotNull(userDao.findById(id)))
        } catch (_: SQLiteConstraintException) {
            AppResult.Failure("An account with these details already exists")
        }
    }

    /** Updates the logged-in user's profile and syncs their name on any Tutor profiles they own. */
    suspend fun updateProfile(userId: Long, newName: String, newEmail: String): AppResult<Unit> {
        val existing = userDao.findById(userId) ?: return AppResult.Failure("User not found")
        val cleanName = newName.trim()
        val cleanEmail = newEmail.trim().lowercase()

        val emailOwner = userDao.findByEmail(cleanEmail)
        if (emailOwner != null && emailOwner.id != userId) {
            return AppResult.Failure("Another account already uses this email")
        }

        userDao.update(existing.copy(name = cleanName, email = cleanEmail))
        tutorDao.updateTutorNameByOwner(userId, cleanName)
        return AppResult.Success(Unit)
    }

    suspend fun login(identifier: String, password: String): AppResult<User> {
        val cleanIdentifier = identifier.trim()
        val user = userDao.findByEmailOrAdmission(cleanIdentifier)
        if (user == null || (user.passwordHash != PasswordHasher.hash(user.email, password))) {
            return AppResult.Failure("Incorrect email/admission number or password")
        }
        session.saveUser(user.id)
        return AppResult.Success(user)
    }
}