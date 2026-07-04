package com.englishfriendai.app.di

import com.englishfriendai.app.data.repository.AuthRepositoryImpl
import com.englishfriendai.app.data.repository.ConversationRepositoryImpl
import com.englishfriendai.app.data.repository.ProgressRepositoryImpl
import com.englishfriendai.app.data.repository.VocabularyRepositoryImpl
import com.englishfriendai.app.domain.repository.AuthRepository
import com.englishfriendai.app.domain.repository.ConversationRepository
import com.englishfriendai.app.domain.repository.ProgressRepository
import com.englishfriendai.app.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository
}
