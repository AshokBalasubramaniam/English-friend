package com.englishfriendai.app.domain.repository

import com.englishfriendai.app.domain.model.Progress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {

    /** Offline-first: last known snapshot from Room, refreshed from the backend. */
    fun getProgress(): Flow<Progress>
}
