package com.studyconnect.app

import com.studyconnect.app.util.PasswordHasher
import com.studyconnect.app.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun emptyEmail_isRejected() = assertNotNull(Validators.email(""))
    @Test fun malformedEmail_isRejected() = assertNotNull(Validators.email("student@"))
    @Test fun validEmail_isAccepted() = assertNull(Validators.email("jane.doe@strathmore.edu"))

    @Test fun loginIdentifier_acceptsEmailOrAdmission() {
        assertNotNull(Validators.loginIdentifier(""))
        assertNull(Validators.loginIdentifier("jane.doe@strathmore.edu"))
        assertNull(Validators.loginIdentifier("ADMIN000"))
        assertNull(Validators.loginIdentifier("123456"))
    }

    @Test fun shortPassword_isRejected() = assertNotNull(Validators.password("abc"))
    @Test fun sixCharPassword_isAccepted() = assertNull(Validators.password("abc123"))
    @Test fun mismatchedConfirm_isRejected() = assertEquals("Passwords do not match", Validators.confirmPassword("abc123", "abc124"))

    @Test fun admission_rejectsSymbols() = assertNotNull(Validators.admission("12#45"))
    @Test fun admission_acceptsDigits() = assertNull(Validators.admission("123456"))

    @Test fun rating_rejectsText() = assertEquals("Enter a valid number", Validators.rating("five"))
    @Test fun rating_rejectsOutOfRange() = assertNotNull(Validators.rating("5.5"))
    @Test fun rating_rejectsNaN() = assertNotNull(Validators.rating("NaN"))
    @Test fun rating_acceptsBoundaries() {
        assertNull(Validators.rating("0"))
        assertNull(Validators.rating("5"))
        assertNull(Validators.rating("4.7"))
    }

    @Test fun sessionTime_mustBeInFuture() {
        val now = 1_000_000L
        assertNotNull(Validators.sessionTime(null, now))
        assertNotNull(Validators.sessionTime(now - 1, now))
        assertNull(Validators.sessionTime(now + 60_000, now))
    }

    @Test fun passwordHash_isDeterministicAndNotPlainText() {
        val a = PasswordHasher.hash("a@b.com", "secret1")
        assertEquals(a, PasswordHasher.hash("A@B.com ", "secret1"))
        assertNotEquals("secret1", a)
        assertNotEquals(a, PasswordHasher.hash("a@b.com", "secret2"))
    }
}
