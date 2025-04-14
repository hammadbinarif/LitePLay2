// MainActivity.kt
package com.hamrah.liteplay

import FolderBrowserScreen
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.IOException
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.hamrah.liteplay.ui.AudioListScreen
import com.hamrah.liteplay.utils.loadAudioFiles
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
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
    private var isPlaying = false
    private val visibleAudioList = mutableStateListOf<AudioFile>()
    val playbackPosition = mutableStateOf(0L)     // current position in ms
    val duration = mutableStateOf(0L)             // total duration in ms
    private lateinit var mediaSession: MediaSessionCompat

    //private val AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().path + "/Music/TestFirstSong.mp3"
//    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            initializeMediaPlayer()
//        } else {
//            Toast.makeText(this, "Permission to access storage denied", Toast.LENGTH_SHORT).show()
//            playPauseButton.isEnabled = false
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioPermission()
        mediaSession = MediaSessionCompat(this, "LitePlaySession").apply {
            isActive = true
        }

        // Pass this session to your player notification later


        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState(isPlaying)
                showMediaNotification(isPlaying)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_playback_channel",
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        //val audioList = loadLocalMusicFiles(this)

        audioFiles = loadAudioFiles()
        audioByFolder = groupAudioByFolder(audioFiles)
        shuffledList = audioFiles // initialize to original


// Optional: auto-play first
//        if (audioFiles.isNotEmpty()) {
//            playAudio(0)
//        }

        //val currentAudio = mutableStateOf<AudioFile?>(null)

        setContent {
            MaterialTheme {
                if (selectedFolder.value == null) {
                    FolderBrowserScreen(
                        folders = audioByFolder.keys.toList(),
                        onFolderSelected = { folder ->
                            selectedFolder.value = folder
                            visibleAudioList.clear()
                            visibleAudioList.addAll(audioByFolder[selectedFolder.value] ?: emptyList())

                        },
                        playbackPosition = playbackPosition,
                        duration = duration,
                        onSeek = { pos -> exoPlayer.seekTo(pos)}

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
                            onNext = { playNextAudio() },
                            onToggleShuffle = { toggleShuffle() },
                            isShuffleEnabled = isShuffleEnabled.value,
                            playbackPosition = playbackPosition,
                            duration = duration,
                            onSeek = { pos -> exoPlayer.seekTo(pos) }

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



//        setContentView(R.layout.activity_main)

//        playPauseButton = findViewById(R.id.playPauseButton)

        // Check and request permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//            initializeMediaPlayer()
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
//        }
//
//        playPauseButton.setOnClickListener {
//                if (isPlaying) {
//                    exoPlayer.pause()
//                    playPauseButton.text = "Play"
//                    isPlaying = false
//                } else {
//                    exoPlayer.play()
//                    playPauseButton.text = "Pause"
//                    isPlaying = true
//            }
//        }
    }

//    private fun initializeMediaPlayer() {
//        try{
//            val mediaItem = MediaItem.fromUri(AUDIO_FILE_PATH)
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.prepare()
//        }
//        catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(this@MainActivity, "Error loading audio file", Toast.LENGTH_SHORT).show()
//            playPauseButton.isEnabled = false
//        }
//    }

//    private fun playAudio(audio: AudioFile) {
//        try {
//            val mediaItem = MediaItem.fromUri(Uri.parse(audio.path))
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.prepare()
//            exoPlayer.play()
//        }
//        catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(this@MainActivity, "Error loading audio file", Toast.LENGTH_SHORT).show()
//        }
//    }
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

    fun playAudio(index: Int) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        val selectedList = if (isShuffleEnabled.value) shuffledList else visibleAudioList

        if (index < selectedList.size) {
            val audio = selectedList[index]
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(audio.path)))
            exoPlayer.prepare()
            exoPlayer.play()
            currentAudioIndex.value = index
            currentAudio.value = audio
        }
    }

    fun playNextAudio() {
        val list = if (isShuffleEnabled.value) shuffledList else visibleAudioList
        val nextIndex = (currentAudioIndex.value + 1) % list.size
        playAudio(nextIndex)
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

}

