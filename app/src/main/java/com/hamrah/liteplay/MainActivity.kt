// MainActivity.kt
package com.hamrah.liteplay

import android.Manifest
import android.content.pm.PackageManager
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
        exoPlayer = ExoPlayer.Builder(this).build()
        val audioList = loadLocalMusicFiles(this)
        val currentAudio = mutableStateOf<AudioFile?>(null)

        setContent {
            MaterialTheme {
                AudioListScreen(
                    context = this,
                    audioList = audioList,
                    currentlyPlaying = currentAudio.value,
                    onSongSelected = { audio ->
                        currentAudio.value = audio
                        playAudio(audio)
                    }
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
}