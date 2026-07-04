package com.englishfriendai.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.englishfriendai.app.core.security.EncryptedPrefsManager
import com.englishfriendai.app.core.util.Constants
import com.englishfriendai.app.data.local.db.AppDatabase
import com.englishfriendai.app.data.local.db.ConversationDao
import com.englishfriendai.app.data.local.db.CorrectionDao
import com.englishfriendai.app.data.local.db.MessageDao
import com.englishfriendai.app.data.local.db.VocabularyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        encryptedPrefsManager: EncryptedPrefsManager
    ): AppDatabase {
        // SQLCipher-backed SupportFactory: the passphrase is generated once and stored inside
        // EncryptedSharedPreferences (see EncryptedPrefsManager.getOrCreateDatabasePassphrase).
        // TODO: revisit passphrase lifecycle before release — e.g. what happens on app
        // reinstall/restore, and whether the passphrase should be tied to biometric unlock.
        val passphrase = encryptedPrefsManager.getOrCreateDatabasePassphrase()
        val factory = SupportFactory(
            net.sqlcipher.database.SQLiteDatabase.getBytes(passphrase)
        )

        return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideVocabularyDao(database: AppDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    fun provideCorrectionDao(database: AppDatabase): CorrectionDao = database.correctionDao()

    /**
     * The Hilt WorkerFactory itself is wired in EnglishFriendApp (Configuration.Provider), so
     * DailyReminderWorker's @AssistedInject dependencies are resolved automatically. This just
     * exposes the WorkManager singleton to ViewModels/repositories that need to enqueue work.
     */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
