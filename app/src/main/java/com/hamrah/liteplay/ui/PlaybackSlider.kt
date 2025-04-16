import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackSlider(
    playbackPosition: State<Long>,
    duration: State<Long>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val sliderPosition = remember(playbackPosition.value) {
        if (duration.value > 0L) {
            playbackPosition.value.toFloat() / duration.value.toFloat()
        } else 0f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(playbackPosition.value),
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.labelSmall
        )
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                val newPosition = (newValue * duration.value).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatTime(duration.value),
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
