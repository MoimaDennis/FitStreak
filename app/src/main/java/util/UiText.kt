package com.example.fitstreak.util

import android.content.Context
import com.example.fitstreak.R
import java.util.Locale

/** Turns an HTTP status code into a message in the user's language (see strings.xml). */
fun Context.apiErrorText(code: Int): String = when (code) {
    400 -> getString(R.string.err_bad_input)
    401 -> getString(R.string.msg_session_expired)
    409 -> getString(R.string.err_exists)
    in 500..599 -> getString(R.string.err_server)
    else -> getString(R.string.err_generic)
}

/**
 * Workout data comes from the server in English ("Core", "High", "None"...).
 * This shows the translated version when the app language is Afrikaans or isiZulu.
 */
fun Context.localizedTerm(value: String): String = when (value.lowercase(Locale.ROOT)) {
    "full body" -> getString(R.string.bp_full_body)
    "core" -> getString(R.string.bp_core)
    "upper" -> getString(R.string.bp_upper)
    "lower" -> getString(R.string.bp_lower)
    "low" -> getString(R.string.int_low)
    "medium" -> getString(R.string.int_medium)
    "high" -> getString(R.string.int_high)
    "none" -> getString(R.string.eq_none)
    "dumbbells" -> getString(R.string.eq_dumbbells)
    "resistance band" -> getString(R.string.eq_band)
    else -> value
}