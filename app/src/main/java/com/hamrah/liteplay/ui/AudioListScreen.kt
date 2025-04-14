package com.hamrah.liteplay.ui
import PlaybackSlider
import androidx.compose.runtime.State

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hamrah.liteplay.AudioFile

@Composable
fun AudioListScreen(
    audioFiles: List<AudioFile>,
    onAudioSelected: (AudioFile, Int) -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    isShuffleEnabled: Boolean,
    playbackPosition: State<Long>,
    duration: State<Long>,
    onSeek: (Long) -> Unit)
 {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(audioFiles) { index, audio ->
                Text(
                    text = audio.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAudioSelected(audio, index) }
                        .padding(16.dp)
                )
            }
        }
        PlaybackSlider(
            playbackPosition = playbackPosition,
            duration = duration,
            onSeek = onSeek
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(onClick = onNext) {
                Text("Next ▶")
            }

            Button(onClick = onToggleShuffle) {
                Text(if (isShuffleEnabled) "Shuffle: ON" else "Shuffle: OFF")
            }
        }
    }
}

