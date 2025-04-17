package com.hamrah.liteplay.player

import AudioManager
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.hamrah.liteplay.CustomCommands

private const val MEDIA_SESSION_TAG = "com.hamrah.liteplay.MEDIA_SESSION"

class MusicService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        mediaSession = MediaSession.Builder(this, player)
            .setId(MEDIA_SESSION_TAG)
            .setCallback(MediaSessionCallback())
            .build()

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }

    fun playAudio(index: Int) {
        AudioManager.getAudio(index)?.let { audio ->
            val mediaItems = AudioManager.getAudioList().map { MediaItem.fromUri(it.path) }
            player.setMediaItems(mediaItems)
            player.prepare()
            player.seekTo(index,0)
            player.play()
        }
    }
    fun playNext() {
        player.seekToNext()
    }

    fun playPrevious() {
        player.seekToPrevious()
    }

    fun toggleShuffle() {
        AudioManager.toggleShuffle()
    }

    fun toggleRepeat() {
        player.repeatMode = if (player.repeatMode == Player.REPEAT_MODE_ONE)
            Player.REPEAT_MODE_OFF
        else
            Player.REPEAT_MODE_ONE
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }


    inner class MediaSessionCallback : MediaSession.Callback {

@OptIn(UnstableApi::class)
override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {

    val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
        .add(CustomCommands.toggleShuffle)
        .add(CustomCommands.toggleRepeat)
        .add(CustomCommands.togglePlayPause)
        .add(CustomCommands.playNext)
        .add(CustomCommands.playPrevious)
        .add(CustomCommands.playAtIndex)
        .build()

        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS

        return MediaSession.ConnectionResult.accept(
            sessionCommands,
            playerCommands
        )
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            // Optional: perform actions once the controller has connected
            println("Controller connected: ${controller.packageName}")
        }

        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {

            when (customCommand.customAction) {
                CustomCommands.CMD_TOGGLE_SHUFFLE -> toggleShuffle()
                CustomCommands.CMD_TOGGLE_REPEAT -> toggleRepeat()
                CustomCommands.CMD_TOGGLE_PLAY_PAUSE -> togglePlayPause()
                CustomCommands.CMD_NEXT -> playNext()
                CustomCommands.CMD_PREV -> playPrevious()
                CustomCommands.CMD_PLAY_AT_INDEX -> {
                    val index = args.getInt("index", -1)
                    if (index in 0 until AudioManager.size()) {
                        playAudio(index)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    } else {
                        return Futures.immediateFuture(SessionResult(SessionError.ERROR_BAD_VALUE))
                    }
                }

                else -> return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}

