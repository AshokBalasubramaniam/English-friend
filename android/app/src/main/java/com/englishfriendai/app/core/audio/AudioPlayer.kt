package com.englishfriendai.app.core.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Playback state surfaced to the Chat screen so the UI can show a mini "AI is speaking" affordance. */
enum class PlaybackState { IDLE, LOADING, PLAYING, PAUSED, ENDED, ERROR }

/**
 * Minimal Media3/ExoPlayer wrapper for playing back AI voice replies (TTS audio URLs returned
 * by the backend, or the on-device [TextToSpeechManager] as a fallback).
 */
@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var exoPlayer: ExoPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private fun playerOrCreate(): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(context).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playbackState.value = when (playbackState) {
                        Player.STATE_BUFFERING -> PlaybackState.LOADING
                        Player.STATE_READY -> if (player.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
                        Player.STATE_ENDED -> PlaybackState.ENDED
                        else -> PlaybackState.IDLE
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _playbackState.value = PlaybackState.ERROR
                }
            })
            exoPlayer = player
        }
    }

    fun play(audioUrl: String) {
        val player = playerOrCreate()
        player.setMediaItem(MediaItem.fromUri(audioUrl))
        player.prepare()
        player.playWhenReady = true
        _playbackState.value = PlaybackState.LOADING
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.IDLE
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
