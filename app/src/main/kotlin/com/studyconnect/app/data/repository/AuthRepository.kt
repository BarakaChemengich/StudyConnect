package com.studyconnect.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.studyconnect.app.data.SessionManager
import com.studyconnect.app.data.local.User
import com.studyconnect.app.data.local.UserDao
import com.studyconnect.app.util.AppResult
import com.studyconnect.app.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val userDao: UserDao, private val session: SessionManager) {

    fun isLoggedIn(): Boolean = session.isLoggedIn()
    fun currentUserId(): Long = session.userId()
    fun observeCurrentUser(): Flow<User?> = userDao.observeById(session.userId())
    fun logout() = session.clear()

    suspend fun register(name: String, admissionNo: String, email: String, password: String): AppResult<User> {
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
                    name = name.trim(),
                    admissionNo = cleanAdmission,
                    email = cleanEmail,
                    passwordHash = PasswordHasher.hash(cleanEmail, password)
                )
            )
            session.saveUser(id)
            AppResult.Success(checkNotNull(userDao.findById(id)))
        } catch (_: SQLiteConstraintException) {
            AppResult.Failure("An account with these details already exists")
        }
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
