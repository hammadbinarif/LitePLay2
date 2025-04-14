// MainActivity.kt
package com.hamrah.liteplay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.IOException
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.hamrah.liteplay.ui.AudioListScreen
import com.hamrah.liteplay.utils.loadLocalMusicFiles

class MainActivity : ComponentActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private var audioFiles = listOf<AudioFile>()
    private var currentIndex = mutableStateOf(0)
    private var isShuffleEnabled = mutableStateOf(false)
    private var shuffledList = listOf<AudioFile>()
    private val currentAudio = mutableStateOf<AudioFile?>(null)
    private var isPlaying = false
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
        exoPlayer = ExoPlayer.Builder(this).build()
        val audioList = loadLocalMusicFiles(this)

        audioFiles = loadLocalMusicFiles(this)
        shuffledList = audioFiles // initialize to original

// Optional: auto-play first
        if (audioFiles.isNotEmpty()) {
            playAudio(0)
        }

        //val currentAudio = mutableStateOf<AudioFile?>(null)

        setContent {
            MaterialTheme {
                AudioListScreen(
                    audioFiles = audioFiles,
                    onAudioSelected = { audio, index -> playAudio(index) },
                    onNext = { playNextAudio() },
                    onToggleShuffle = { toggleShuffle() },
                    isShuffleEnabled = isShuffleEnabled.value
                )
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

    private fun playAudio(audio: AudioFile) {
        try {
            val mediaItem = MediaItem.fromUri(audio.uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
        catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@MainActivity, "Error loading audio file", Toast.LENGTH_SHORT).show()
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
        // TODO: Add code to hand android version
    }

    private fun playAudio(index: Int) {
        val listToUse = if (isShuffleEnabled.value) shuffledList else audioFiles
        val audio = listToUse[index]

        currentAudio.value = audio
        currentIndex.value = index

        val mediaItem = MediaItem.fromUri(audio.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun playNextAudio() {
        val list = if (isShuffleEnabled.value) shuffledList else audioFiles
        val nextIndex = (currentIndex.value + 1) % list.size
        playAudio(nextIndex)
    }

    private fun toggleShuffle() {
        isShuffleEnabled.value = !isShuffleEnabled.value
        shuffledList = if (isShuffleEnabled.value) audioFiles.shuffled() else audioFiles
        // Start from current song in shuffled list if shuffle is enabled
        if (isShuffleEnabled.value) {
            val current = currentAudio.value
            currentIndex.value = shuffledList.indexOfFirst { it.uri == current?.uri }
        }
    }

}