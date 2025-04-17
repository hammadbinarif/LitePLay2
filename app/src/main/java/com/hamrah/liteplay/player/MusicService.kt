package com.hamrah.liteplay.player

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
// import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.hamrah.liteplay.AudioFile

private const val MEDIA_SESSION_TAG = "com.hamrah.liteplay.MEDIA_SESSION"

class MusicService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer

    private var audioFiles: List<AudioFile> = emptyList()
    private var currentIndex: Int = 0
    private var isShuffleEnabled: Boolean = false
    private var shuffledList: List<AudioFile> = emptyList()

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

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        player.release()
//        mediaSession.release()
//        stopSelf()
//    }

    fun setAudioList(list: List<AudioFile>) {
        audioFiles = list
        shuffledList = audioFiles
    }

    fun playAudio(index: Int) {
        val selectedList = if (isShuffleEnabled) shuffledList else audioFiles
        if (index < selectedList.size) {
            val audio = selectedList[index]
            currentIndex = index

            player.setMediaItem(MediaItem.fromUri(audio.path))
            player.prepare()
            player.play()
        }
    }

    fun playNext() {
        val list = if (isShuffleEnabled) shuffledList else audioFiles
        val nextIndex = (currentIndex + 1) % list.size
        playAudio(nextIndex)
    }

    fun playPrevious() {
        val list = if (isShuffleEnabled) shuffledList else audioFiles
        val prevIndex = if (currentIndex == 0) list.size - 1 else currentIndex - 1
        playAudio(prevIndex)
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        if (isShuffleEnabled) {
            shuffledList = audioFiles.shuffled()
        }
    }

    fun toggleRepeat() {
        player.repeatMode = if (player.repeatMode == Player.REPEAT_MODE_ONE)
            Player.REPEAT_MODE_OFF
        else
            Player.REPEAT_MODE_ONE
    }

    inner class MediaSessionCallback : MediaSession.Callback {

//         @OptIn(UnstableApi::class)
//        override fun onConnect(
//            session: MediaSession,
//            controller: MediaSession.ControllerInfo,
//        ): MediaSession.ConnectionResult {
//            // You can limit features here if you want (e.g., deny connection or restrict commands)
//            return MediaSession.ConnectionResult.accept(
//                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS,
//                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
//            )
//        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            // Optional: perform actions once the controller has connected
            println("Controller connected: ${controller.packageName}")
        }
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                "TOGGLE_SHUFFLE" -> toggleShuffle()
                "TOGGLE_REPEAT" -> toggleRepeat()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}

