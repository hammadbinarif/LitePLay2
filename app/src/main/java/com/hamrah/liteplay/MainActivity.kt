// MainActivity.kt
package com.hamrah.liteplay

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.IOException
import android.util.Log

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playPauseButton: Button
    private var isPlaying = false
    private val AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().path + "/Music/TestFirstSong.mp3"
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initializeMediaPlayer()
        } else {
            Toast.makeText(this, "Permission to access storage denied", Toast.LENGTH_SHORT).show()
            playPauseButton.isEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.playPauseButton)

        // Check and request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            initializeMediaPlayer()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }

        playPauseButton.setOnClickListener {
            mediaPlayer?.let {
                if (isPlaying) {
                    it.pause()
                    playPauseButton.text = "Play"
                    isPlaying = false
                } else {
                    it.start()
                    playPauseButton.text = "Pause"
                    isPlaying = true
                }
            } ?: run {
                Toast.makeText(this, "Media player not initialized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(AUDIO_FILE_PATH)
                prepare()
                setOnCompletionListener {
                    this@MainActivity.isPlaying = false
                    playPauseButton.text = "Play"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error loading audio file", Toast.LENGTH_SHORT).show()
                playPauseButton.isEnabled = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}