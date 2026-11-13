package com.studyconnect.app.util

import java.security.MessageDigest

/**
 * SHA-256 of "email:password". Passwords are never stored in plain text.
 * (A real production app would use a server and a slow hash such as bcrypt/Argon2.)
 */
object PasswordHasher {
    fun hash(email: String, password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("${email.trim().lowercase()}:$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
