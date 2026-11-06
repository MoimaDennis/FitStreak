package com.example.fitstreak

import com.example.fitstreak.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test fun validEmailIsAccepted() = assertTrue(Validators.isValidEmail("dennis@example.com"))
    @Test fun emailWithoutAtIsRejected() = assertFalse(Validators.isValidEmail("dennis.example.com"))
    @Test fun emailWithoutDomainIsRejected() = assertFalse(Validators.isValidEmail("dennis@"))
    @Test fun emailIsTrimmed() = assertTrue(Validators.isValidEmail("  a@b.co  "))

    @Test fun passwordOfSixCharactersIsAccepted() = assertTrue(Validators.isValidPassword("abc123"))
    @Test fun passwordOfFiveCharactersIsRejected() = assertFalse(Validators.isValidPassword("abc12"))

    @Test fun usernameRules() {
        assertTrue(Validators.isValidUsername("user_1"))
        assertFalse(Validators.isValidUsername("ab"))
        assertFalse(Validators.isValidUsername("has space"))
        assertFalse(Validators.isValidUsername("a".repeat(21)))
    }

    @Test fun challengeResultParsing() {
        assertEquals(25, Validators.parseChallengeResult(" 25 "))
        assertEquals(100000, Validators.parseChallengeResult("100000"))
        assertNull(Validators.parseChallengeResult(""))
        assertNull(Validators.parseChallengeResult("abc"))
        assertNull(Validators.parseChallengeResult("0"))
        assertNull(Validators.parseChallengeResult("100001"))
    }
}