package com.englishfriendai.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.englishfriendai.app.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [EncryptedSharedPreferences] (AES-256-GCM, key material in AndroidKeystore) for the
 * handful of secrets that must be readable synchronously off the main-thread network stack
 * (e.g. by [com.englishfriendai.app.core.network.AuthInterceptor]), where DataStore's
 * asynchronous Flow API would be awkward.
 *
 * TODO: reuse the same AndroidKeystore-backed [MasterKey] to derive/unseal the SQLCipher
 * database passphrase (see data/local/db/AppDatabase.kt) instead of hardcoding one.
 */
@Singleton
class EncryptedPrefsManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Returns the SQLCipher passphrase for [com.englishfriendai.app.data.local.db.AppDatabase],
     * generating and persisting a new random one on first run. Because this value lives inside
     * EncryptedSharedPreferences, it is itself protected by the AndroidKeystore-backed
     * [MasterKey] above.
     */
    fun getOrCreateDatabasePassphrase(): CharArray {
        val existing = sharedPreferences.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) return existing.toCharArray()

        val generated = java.security.SecureRandom().let { random ->
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
        sharedPreferences.edit().putString(KEY_DB_PASSPHRASE, generated).apply()
        return generated.toCharArray()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_DB_PASSPHRASE = "key_db_passphrase"
    }
}
