package com.hamrah.liteplay.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hamrah.liteplay.AudioFile

@Composable
fun AudioListScreen(
    context: Context,
    audioList: List<AudioFile>,
    onSongSelected: (AudioFile) -> Unit,
    currentlyPlaying: AudioFile?
) {
    LazyColumn {
        items(audioList) { audio ->
            val isPlaying = audio.uri == currentlyPlaying?.uri
            val bgColor = if (isPlaying) Color.LightGray else Color.Transparent
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .clickable { onSongSelected(audio) }
                    .padding(16.dp)
            ) {
                Text(audio.title, style = MaterialTheme.typography.bodyLarge)
                Text(audio.artist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
