    // MainActivity.kt
package com.hamrah.liteplay

import AudioManager
import FolderBrowserScreen
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import com.hamrah.liteplay.player.MusicService
import com.hamrah.liteplay.ui.AudioListScreen
import com.hamrah.liteplay.utils.loadAudioFiles
import kotlinx.coroutines.delay


    private const val CHANNEL_ID = "LitePlay"
private val MEDIA_SESSION_TAG = "com.hamrah.liteplay.MEDIA_SESSION"

class MainActivity : ComponentActivity() {

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
    private var isRepeatEnabled = mutableStateOf(false)
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAudioPermission()

        requestForgroundServicePermission()

        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))

    val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

    Futures.addCallback(controllerFuture, object : FutureCallback<MediaController> {
        override fun onSuccess(controller: MediaController?) {
            controller?.let {
                mediaController = it
            }
        }

        override fun onFailure(t: Throwable) {
            Log.e("MainActivity", "MediaController connection failed", t)
        }
    }, MoreExecutors.directExecutor())

        val serviceIntent = Intent(this, MusicService::class.java)

        startForegroundService(serviceIntent)



        audioFiles = loadAudioFiles()
        AudioManager.setAudioFiles(audioFiles)
        audioByFolder = AudioManager.groupByFolder()


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
                        onSeek = { pos -> mediaController?.seekTo(pos) },
                        onForward = { mediaController?.seekTo(playbackPosition.value + 10000) },
                        onPrevious = { mediaController?.sendCustomCommand(CustomCommands.playPrevious,Bundle.EMPTY) },
                        onRepeat = { mediaController?.sendCustomCommand(CustomCommands.toggleRepeat,Bundle.EMPTY) },
                        onRewind = { mediaController?.seekTo(playbackPosition.value - 10000) },
                        isRepeatEnabled = isRepeatEnabled.value,
                        onToggleShuffle = { mediaController?.sendCustomCommand(CustomCommands.toggleShuffle,Bundle.EMPTY) },
                        onNext = { mediaController?.sendCustomCommand(CustomCommands.playNext,Bundle.EMPTY) },
                        onTogglePlayPause = {mediaController?.sendCustomCommand(CustomCommands.togglePlayPause,Bundle.EMPTY)},
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
                            onAudioSelected = { audio, index ->
                                AudioManager.setAudioFiles(audioByFolder[selectedFolder.value] ?: emptyList())
                                playAudioAtIndex(index)
                            },
                            currentAudio = currentAudio.value,
                            onNext = { mediaController?.sendCustomCommand(CustomCommands.playNext,Bundle.EMPTY) },
                            onToggleShuffle = { mediaController?.sendCustomCommand(CustomCommands.toggleShuffle,Bundle.EMPTY) },
                            isShuffleEnabled = isShuffleEnabled.value,
                            playbackPosition = playbackPosition,
                            duration = duration,
                            onSeek = { pos -> mediaController?.seekTo(pos) },
                            onForward = { mediaController?.seekTo(playbackPosition.value + 10000) },
                            onPrevious = { mediaController?.sendCustomCommand(CustomCommands.playPrevious,Bundle.EMPTY) },
                            onRepeat = { mediaController?.sendCustomCommand(CustomCommands.toggleRepeat,Bundle.EMPTY) },
                            onRewind = { mediaController?.seekTo(playbackPosition.value - 10000) },
                            isRepeatEnabled = isRepeatEnabled.value,
                            onTogglePlayPause = {mediaController?.sendCustomCommand(CustomCommands.togglePlayPause,Bundle.EMPTY)},
                            isPlaying = isPlaying.value

                        )
                    }
                }
                LaunchedEffect(mediaController) {
                    while (true) {
                        if (mediaController?.isPlaying==true) {
                            playbackPosition.value = mediaController!!.currentPosition
                            duration.value = mediaController!!.duration
                        }
                        delay(500)
                    }
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        //exoPlayer.release()

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

    fun playAudioAtIndex(index: Int) {
        val args = Bundle().apply {
            putInt("index", index)
        }

        mediaController?.sendCustomCommand(
            SessionCommand(CustomCommands.CMD_PLAY_AT_INDEX, Bundle.EMPTY),
            args
        )
    }

}

