package com.studyconnect.app.data.repository

import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.data.local.TutorDao
import com.studyconnect.app.util.AppResult
import kotlinx.coroutines.flow.Flow

class TutorRepository(private val tutorDao: TutorDao) {

    // READ
    fun search(query: String): Flow<List<Tutor>> = tutorDao.search(query.trim())
    fun observeById(id: Long): Flow<Tutor?> = tutorDao.observeById(id)
    fun observeByOwner(ownerId: Long): Flow<List<Tutor>> = tutorDao.observeByOwner(ownerId)
    suspend fun findById(id: Long): Tutor? = tutorDao.findById(id)

    // CREATE
    suspend fun add(tutor: Tutor): AppResult<Long> {
        if (tutorDao.countDuplicate(tutor.ownerId, tutor.subject, excludeId = 0) > 0) {
            return AppResult.Failure("You already have a tutor profile for ${tutor.subject}")
        }
        return AppResult.Success(tutorDao.insert(tutor))
    }

    // UPDATE
    suspend fun update(tutor: Tutor): AppResult<Unit> {
        if (tutorDao.countDuplicate(tutor.ownerId, tutor.subject, excludeId = tutor.id) > 0) {
            return AppResult.Failure("You already have a tutor profile for ${tutor.subject}")
        }
        tutorDao.update(tutor)
        return AppResult.Success(Unit)
    }

    suspend fun setAvailability(tutor: Tutor, available: Boolean) {
        tutorDao.update(tutor.copy(available = available))
    }

    // DELETE
    suspend fun delete(tutor: Tutor) = tutorDao.delete(tutor)
}
