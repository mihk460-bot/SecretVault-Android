package com.example.secretvault

import android.content.Context
import android.content.SharedPreferences

object LockoutManager {

    private const val PREFS_NAME = "lockout_prefs"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private const val KEY_LOCKOUT_TIME = "lockout_until"

    const val MAX_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLockedOut(context: Context): Boolean {
        val lockoutUntil = getPrefs(context).getLong(KEY_LOCKOUT_TIME, 0L)
        return System.currentTimeMillis() < lockoutUntil
    }

    fun remainingLockoutSeconds(context: Context): Long {
        val lockoutUntil = getPrefs(context).getLong(KEY_LOCKOUT_TIME, 0L)
        val remaining = lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 1000) + 1 else 0
    }

    fun getFailedAttempts(context: Context): Int =
        getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)

    fun recordFailedAttempt(context: Context): Boolean {
        val prefs = getPrefs(context)
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)

        if (attempts >= MAX_ATTEMPTS) {
            val lockUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            editor.putLong(KEY_LOCKOUT_TIME, lockUntil)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
            editor.apply()
            return true
        }

        editor.apply()
        return false
    }

    fun resetAttempts(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_TIME, 0L)
            .apply()
    }
}
