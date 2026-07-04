package com.englishfriendai.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CorrectionDao {

    @Query("SELECT * FROM corrections WHERE messageId = :messageId")
    suspend fun getByMessageId(messageId: String): CorrectionEntity?

    @Query("SELECT * FROM corrections WHERE messageId IN (:messageIds)")
    suspend fun getByMessageIds(messageIds: List<String>): List<CorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(correction: CorrectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(corrections: List<CorrectionEntity>)
}
