package com.example.secretvault

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurityManager {

    private const val PREFS_NAME = "secure_vault_prefs"
    private const val KEY_PASSCODE = "vault_pin"
    const val MIN_PIN_LENGTH = 6

    private fun getEncryptedPrefs(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun isPasscodeSet(context: Context): Boolean =
        getEncryptedPrefs(context).contains(KEY_PASSCODE)

    fun setPasscode(context: Context, code: String) {
        getEncryptedPrefs(context).edit().putString(KEY_PASSCODE, code).apply()
    }

    fun verifyPasscode(context: Context, input: String): Boolean =
        getEncryptedPrefs(context).getString(KEY_PASSCODE, null) == input

    fun clearPasscode(context: Context) {
        getEncryptedPrefs(context).edit().remove(KEY_PASSCODE).apply()
    }
}
