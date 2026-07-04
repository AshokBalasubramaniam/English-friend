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
import java.io.File
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
        fun build(): AppDatabase {
            val passphrase = encryptedPrefsManager.getOrCreateDatabasePassphrase()
            val factory = SupportFactory(net.sqlcipher.database.SQLiteDatabase.getBytes(passphrase))
            return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }

        var database = build()
        try {
            // Room's .build() is lazy and doesn't actually open the underlying file - force
            // that now so a wrong-passphrase/corrupted-file failure ("file is not a database")
            // surfaces here, not on some arbitrary later query deep inside a repository.
            database.openHelper.readableDatabase.query("SELECT count(*) FROM sqlite_master").use {}
        } catch (e: Exception) {
            // The on-disk file was encrypted with a passphrase we no longer have (e.g. a past
            // bug that cleared the stored passphrase without clearing the database it
            // protected). That data is unrecoverable without the original key - self-heal by
            // discarding the orphaned file and starting fresh rather than crashing forever.
            database.close()
            context.getDatabasePath(Constants.DATABASE_NAME).let { dbFile ->
                dbFile.delete()
                File(dbFile.path + "-wal").delete()
                File(dbFile.path + "-shm").delete()
            }
            database = build()
        }
        return database
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
