package com.hamrah.liteplay.player

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media3.exoplayer.ExoPlayer
import com.hamrah.liteplay.R

class MusicService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, "LitePlaySession")
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
        startForeground(1, createNotification())
        // TODO: Start playback here if needed
        return START_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "music_channel")
            .setContentTitle("LitePlay")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_play)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
