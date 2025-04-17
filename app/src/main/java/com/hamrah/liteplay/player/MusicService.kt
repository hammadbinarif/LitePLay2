package com.hamrah.liteplay.player

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.hamrah.liteplay.AudioFile
import com.hamrah.liteplay.MainActivity
import com.hamrah.liteplay.R

private const val CHANNEL_ID = "LitePlay"

class MusicService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private val NOTIFICATION_ID = 1981 // Unique ID for the notification
    private val ACTION_PLAY_PAUSE = "com.hamrah.liteplay.ACTION_PLAY_PAUSE" // Unique action string
    private val ACTION_SKIP_NEXT = "com.hamrah.liteplay.ACTION_SKIP_NEXT"   // Unique action string
    private val MEDIA_SESSION_TAG = "com.hamrah.liteplay.MEDIA_SESSION"


    private var audioList: List<AudioFile> = emptyList()
    private var currentIndex = 0


    fun setAudioList(list: List<AudioFile>) {
        audioList = list
    }

    fun playAudioAt(index: Int) {
        if (index in audioList.indices) {
            val audio = audioList[index]
            currentIndex = index
            exoPlayer.setMediaItem(MediaItem.fromUri(audio.path))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun playNextAudio() {
        val nextIndex = (currentIndex + 1) % audioList.size
        playAudioAt(nextIndex)
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
                .build()
        )
        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PLAY_PAUSE -> {
                    // Handle play/pause action here (e.g., toggle ExoPlayer state)
                    if (exoPlayer?.isPlaying == true) {
                        exoPlayer?.pause()
                    } else {
                        exoPlayer?.play()
                    }
                    // Update the notification to reflect the new play/pause state
                    val updatedNotification = createMusicNotification()
                    startForeground(NOTIFICATION_ID, updatedNotification)
                }

                ACTION_SKIP_NEXT -> {
                    // Handle skip next action here (e.g., call exoPlayer.next())
                    exoPlayer?.seekToNextMediaItem()
                    // Update the notification with the new track information
                    val updatedNotification = createMusicNotification()
                    startForeground(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
        // Create the notification
        val notification = createMusicNotification() // Your function to build the notification

        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createMusicNotification(): Notification {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP // Optional flags
        }

        // Create a PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code (can be any integer)
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )


        val playPauseIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            playPauseIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent for Skip Next action
        val skipNextIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_SKIP_NEXT
        }
        val skipNextPendingIntent: PendingIntent = PendingIntent.getService(
            this,
            1, // Use a different requestCode for each action
            skipNextIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Build your notification here using NotificationCompat.Builder or Notification.Builder
        // Make sure to include media controls and a content intent
        val builder = NotificationCompat.Builder(this, CHANNEL_ID) // Replace with your channel ID
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("Now Playing")
            .setContentText("Artist - Song Title")
            .addAction(R.drawable.ic_play, "Play", playPausePendingIntent) // Action at index 0
            .addAction(R.drawable.ic_next, "Next", skipNextPendingIntent) // Action at index 1
            // ... add media control actions using PendingIntent ...
            .setContentIntent(pendingIntent) // Set the content intent here
            .setOngoing(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                // If you have a MediaSession, set the token here
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0 ,1)) // Show play/pause in compact view

        return builder.build()
    }


//    private fun createNotification(): Notification {
//
//        val playPauseIntent = Intent(this, MusicService::class.java).apply {
//            action = ACTION_PLAY_PAUSE
//        }
//        val playPausePendingIntent = PendingIntent.getService(
//            this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val nextIntent = Intent(this, MusicService::class.java).apply {
//            action = ACTION_NEXT
//        }
//        val nextPendingIntent = PendingIntent.getService(
//            this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_music_note) // your icon here
//            .setContentTitle("Now Playing")
//            .setContentText("Lite Play") // or whatever you're displaying
//            //.setLargeIcon(albumArtBitmap)
//            .setContentIntent(null)
//            .setOnlyAlertOnce(true)
//            .setShowWhen(false)
//            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
//            .addAction(
//                if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
//                if (exoPlayer.isPlaying) "Pause" else "Play",
//                playPausePendingIntent
//            )
//            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
//            .build()
//    }

    override fun onBind(intent: Intent?): IBinder? = null

}
