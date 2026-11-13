package com.studyconnect.app.data

import android.content.Context

/** Remembers which user is logged in (survives app restarts) using SharedPreferences. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("studyconnect_session", Context.MODE_PRIVATE)

    fun saveUser(id: Long) = prefs.edit().putLong(KEY_USER_ID, id).apply()
    fun userId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)
    fun isLoggedIn(): Boolean = userId() != NO_USER
    fun clear() = prefs.edit().remove(KEY_USER_ID).apply()

    companion object {
        const val NO_USER = -1L
        private const val KEY_USER_ID = "user_id"
    }
}
