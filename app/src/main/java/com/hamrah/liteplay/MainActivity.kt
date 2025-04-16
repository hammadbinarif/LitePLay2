// MainActivity.kt
package com.hamrah.liteplay

import FolderBrowserScreen
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionErrors
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hamrah.liteplay.player.MusicService
import com.hamrah.liteplay.ui.AudioListScreen
import com.hamrah.liteplay.utils.loadAudioFiles
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private var audioFiles = listOf<AudioFile>()
    private var currentAudioIndex = mutableStateOf(0)
    private var isShuffleEnabled = mutableStateOf(false)
    private var shuffledList = listOf<AudioFile>()
    private val currentAudio = mutableStateOf<AudioFile?>(null)
    private var audioByFolder = mapOf<String, List<AudioFile>>()
    private var selectedFolder = mutableStateOf<String?>(null)
    private var isPlaying = mutableStateOf(false)
    private val visibleAudioList = mutableStateListOf<AudioFile>()
    val playbackPosition = mutableStateOf(0L)     // current position in ms
    val duration = mutableStateOf(0L)             // total duration in ms
    private lateinit var mediaSession: MediaSessionCompat
    private var isRepeatEnabled = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioPermission()
        requestForgroundServicePermission()
        mediaSession = MediaSessionCompat(this, "LitePlaySession").apply {
            isActive = true
        }

        // Pass this session to your player notification later

        val serviceIntent = Intent(this, MusicService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState(isPlaying)
                showMediaNotification(isPlaying)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }


        audioFiles = loadAudioFiles()
        audioByFolder = groupAudioByFolder(audioFiles)
        shuffledList = audioFiles // initialize to original


        setContent {
            MaterialTheme {
                if (selectedFolder.value == null) {
                    FolderBrowserScreen(
                        folders = audioByFolder.keys.toList(),
                        onFolderSelected = { folder ->
                            selectedFolder.value = folder
                            visibleAudioList.clear()
                            visibleAudioList.addAll(
                                audioByFolder[selectedFolder.value] ?: emptyList()
                            )

                        },
                        playbackPosition = playbackPosition,
                        duration = duration,
                        onSeek = { pos -> exoPlayer.seekTo(pos) },
                        onForward = { exoPlayer.seekTo(playbackPosition.value + 10000) },
                        onPrevious = { playPreviousAudio() },
                        onRepeat = { toggleRepeat() },
                        onRewind = { exoPlayer.seekTo(playbackPosition.value - 10000) },
                        isRepeatEnabled = isRepeatEnabled.value,
                        onToggleShuffle = { toggleShuffle() },
                        onNext = { playNextAudio() },
                        onTogglePlayPause = {togglePlayPause()},
                        isPlaying = isPlaying.value


                        )
                } else {
                    Column {
                        Button(
                            onClick = { selectedFolder.value = null },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("⬅ Back to folders")
                        }

                        AudioListScreen(
                            audioFiles = audioByFolder[selectedFolder.value] ?: emptyList(),
                            onAudioSelected = { audio, index -> playAudio(index) },
                            currentAudio = currentAudio.value,
                            onNext = { playNextAudio() },
                            onToggleShuffle = { toggleShuffle() },
                            isShuffleEnabled = isShuffleEnabled.value,
                            playbackPosition = playbackPosition,
                            duration = duration,
                            onSeek = { pos -> exoPlayer.seekTo(pos) },
                            onForward = { exoPlayer.seekTo(playbackPosition.value + 10000) },
                            onPrevious = { playPreviousAudio() },
                            onRepeat = { toggleRepeat() },
                            onRewind = { exoPlayer.seekTo(playbackPosition.value - 10000) },
                            isRepeatEnabled = isRepeatEnabled.value,
                            onTogglePlayPause = {togglePlayPause()},
                            isPlaying = isPlaying.value

                        )
                    }
                }
                LaunchedEffect(exoPlayer) {
                    while (true) {
                        if (exoPlayer.isPlaying) {
                            playbackPosition.value = exoPlayer.currentPosition
                            duration.value = exoPlayer.duration
                        }
                        delay(500)
                    }
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()

    }

    private fun requestAudioPermission() {
        // Android 13+ (API 33+)
        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                123 // requestCode
            )
        }
        // TODO: Add code to handle android version
    }

    private fun requestForgroundServicePermission() {
        // Android 13+ (API 33+)
        if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK),
                123 // requestCode
            )
        }
        // TODO: Add code to handle android version
    }

    fun playAudio(index: Int) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        isPlaying.value=false

        val selectedList = if (isShuffleEnabled.value) shuffledList else visibleAudioList

        if (index < selectedList.size) {
            val audio = selectedList[index]
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(audio.path)))
            exoPlayer.prepare()
            exoPlayer.play()
            currentAudioIndex.value = index
            currentAudio.value = audio
            isPlaying.value=true
        }
    }

    fun playNextAudio() {
        val list = if (isShuffleEnabled.value) shuffledList else visibleAudioList
        val nextIndex = (currentAudioIndex.value + 1) % list.size
        playAudio(nextIndex)
    }

    fun playPreviousAudio() {
        val list = if (isShuffleEnabled.value) shuffledList else visibleAudioList
        val previousIndex = if (currentAudioIndex.value ==0) list.size-1 else currentAudioIndex.value-1
        playAudio(previousIndex)
    }

    fun toggleShuffle() {
        isShuffleEnabled.value = !isShuffleEnabled.value
        if (isShuffleEnabled.value) {
            shuffledList = visibleAudioList.shuffled()
        }
    }

    fun groupAudioByFolder(audioList: List<AudioFile>): Map<String, List<AudioFile>> {
        return audioList.groupBy { it.parentFolder }
    }

    private fun updatePlaybackState(isPlaying: Boolean) {
        val state = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                exoPlayer.currentPosition,
                1f
            )
            .build()
        mediaSession.setPlaybackState(state)
    }

    private fun showMediaNotification(isPlaying: Boolean) {
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        val description = mediaMetadata?.description

        val builder = NotificationCompat.Builder(this, "media_playback_channel")
            .setContentTitle(description?.title ?: "Playing audio")
            .setContentText(description?.subtitle ?: "")
            .setSmallIcon(R.drawable.ic_music_note) // Replace with your own icon
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_prev, "Prev", null // Add real intent if needed
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    if (isPlaying) "Pause" else "Play",
                    null // Add real intent if needed
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next, "Next", null // Add real intent if needed
                )
            )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
            return  // Don't try to show notification without permission
        }

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun toggleRepeat() {
        isRepeatEnabled.value = !isRepeatEnabled.value
        exoPlayer.repeatMode = if (isRepeatEnabled.value) {
            Player.REPEAT_MODE_ONE // Repeats current song
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        isPlaying.value = exoPlayer.isPlaying
    }

}

