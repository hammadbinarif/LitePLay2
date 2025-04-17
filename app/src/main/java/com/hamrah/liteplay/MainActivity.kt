    // MainActivity.kt
package com.hamrah.liteplay

import FolderBrowserScreen
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
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
                        onSeek = { pos -> mediaController?.seekTo(pos) },
                        onForward = { mediaController?.seekTo(playbackPosition.value + 10000) },
                        onPrevious = { playPreviousAudio() },
                        onRepeat = { toggleRepeat() },
                        onRewind = { mediaController?.seekTo(playbackPosition.value - 10000) },
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
                            onSeek = { pos -> mediaController?.seekTo(pos) },
                            onForward = { mediaController?.seekTo(playbackPosition.value + 10000) },
                            onPrevious = { playPreviousAudio() },
                            onRepeat = { toggleRepeat() },
                            onRewind = { mediaController?.seekTo(playbackPosition.value - 10000) },
                            isRepeatEnabled = isRepeatEnabled.value,
                            onTogglePlayPause = {togglePlayPause()},
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

    fun playAudio(index: Int) {
        mediaController?.stop()
        mediaController?.clearMediaItems()
        isPlaying.value=false

        val selectedList = if (isShuffleEnabled.value) shuffledList else visibleAudioList

        if (index < selectedList.size) {
            val audio = selectedList[index]
            mediaController?.setMediaItem(MediaItem.fromUri(Uri.parse(audio.path)))
            mediaController?.prepare()
            mediaController?.play()
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


    private fun toggleRepeat() {
        isRepeatEnabled.value = !isRepeatEnabled.value
        mediaController?.repeatMode = if (isRepeatEnabled.value) {
            Player.REPEAT_MODE_ONE // Repeats current song
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying==true) {
            mediaController!!.pause()
        } else {
            mediaController!!.play()
        }
        isPlaying.value = mediaController?.isPlaying ==true
    }

}

