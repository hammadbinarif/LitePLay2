package com.hamrah.liteplay.ui

import PlaybackControlsRow
import PlaybackSlider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hamrah.liteplay.AudioFile

@Composable
fun AudioListScreen(
    audioFiles: List<AudioFile>,
    onAudioSelected: (AudioFile, Int) -> Unit,
    currentAudio: AudioFile?,
    playbackPosition: State<Long>,
    duration: State<Long>,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    isRepeatEnabled:Boolean,
    isShuffleEnabled: Boolean,
    onTogglePlayPause: () -> Unit,
    isPlaying: Boolean
)
{
     Column (modifier = Modifier.fillMaxSize()) {
         LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(audioFiles) { index, audio ->
                val isPlayingThisAudio = currentAudio?.path == audio.path
                val backgroundColor = if (isPlayingThisAudio) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                val textColor = if (isPlayingThisAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAudioSelected(audio,index) }
                        .background(backgroundColor)
                        .padding(16.dp)
                ) {
                    if (isPlayingThisAudio) {
                        Text("🎵", modifier = Modifier.padding(end = 8.dp), color = textColor)
                    }
                    Text(text = audio.title, color = textColor)
                }
            }
        }

         PlaybackControlsRow(
             onToggleShuffle = onToggleShuffle,
             onPrevious = onPrevious,
             onRewind = onRewind,
             onForward = onForward,
             onNext = onNext,
             onRepeat = onRepeat,
             isRepeatEnabled = isRepeatEnabled,
             onTogglePlayPause = onTogglePlayPause,
             isPlaying = isPlaying

         )

         PlaybackSlider(
             playbackPosition = playbackPosition,
             duration = duration,
             onSeek = onSeek,
                 modifier = Modifier
                 .fillMaxWidth()
                 .padding(horizontal = 16.dp, vertical = 8.dp)
         )

     }
}

