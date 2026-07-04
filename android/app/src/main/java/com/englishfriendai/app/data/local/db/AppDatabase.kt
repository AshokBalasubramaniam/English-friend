package com.englishfriendai.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        VocabularyEntity::class,
        CorrectionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun correctionDao(): CorrectionDao

    companion object {
        const val DATABASE_NAME = com.englishfriendai.app.core.util.Constants.DATABASE_NAME
    }
}

// -----------------------------------------------------------------------------------------
// SQLCipher passphrase wiring — TODO
// -----------------------------------------------------------------------------------------
// This database is opened via net.zetetic:sqlcipher-android's SupportFactory (see
// di/DatabaseModule.kt) which requires a passphrase byte array. For this scaffold the
// passphrase is a locally generated random value persisted only via EncryptedPrefsManager
// (itself backed by an AndroidKeystore-derived MasterKey). Before shipping:
//   1. Confirm the passphrase is generated once per install with a secure RNG
//      (e.g. javax.crypto.KeyGenerator or SecureRandom), never hardcoded.
//   2. Confirm it is never logged, backed up, or transmitted.
//   3. Consider rotating/re-encrypting the DB if the Keystore key is ever invalidated
//      (e.g. after biometric enrollment changes on some OEM devices).
