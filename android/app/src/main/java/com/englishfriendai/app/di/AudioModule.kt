package com.englishfriendai.app.di

import com.englishfriendai.app.core.audio.SpeechRecognizerManager
import com.englishfriendai.app.core.audio.SpeechRecognizerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindSpeechRecognizerManager(impl: SpeechRecognizerManagerImpl): SpeechRecognizerManager
}
