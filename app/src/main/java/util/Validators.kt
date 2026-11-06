package com.example.fitstreak.util

/**
 * Input validation used by Login, Register and Challenge screens.
 * Pure Kotlin (no Android classes) so it can be unit tested - see ValidatorsTest.kt.
 */
object Validators {
    const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** 3-20 characters: letters, digits or underscore. */
    fun isValidUsername(username: String): Boolean {
        val t = username.trim()
        return t.length in 3..20 && t.all { it.isLetterOrDigit() || it == '_' }
    }

    /** Returns the challenge result as a whole number 1..100000, or null when the text is invalid. */
    fun parseChallengeResult(text: String): Int? {
        val value = text.trim().toIntOrNull() ?: return null
        return if (value in 1..100000) value else null
    }
}