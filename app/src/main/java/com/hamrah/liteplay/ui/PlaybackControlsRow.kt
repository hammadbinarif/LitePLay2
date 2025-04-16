import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackControlsRow(
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    isRepeatEnabled:Boolean,
    onTogglePlayPause: () -> Unit,
    isPlaying:Boolean


) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onToggleShuffle) {
            Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle")
        }
        IconButton(onClick = onPrevious) {
            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
        }
        IconButton(onClick = onRewind) {
            Icon(imageVector = Icons.Default.Replay10, contentDescription = "Rewind 10s")
        }

        IconButton(onClick = onTogglePlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }
        IconButton(onClick = onForward) {
            Icon(imageVector = Icons.Default.Forward10, contentDescription = "Forward 10s")
        }
        IconButton(onClick = onNext) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
        }
        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (isRepeatEnabled) Color.Green else Color.Black
            )
        }    }
}
