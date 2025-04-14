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
    onSeek: (Long) -> Unit
) {
    if (duration.value > 0L) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Slider(
                value = playbackPosition.value.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.value.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${formatTime(playbackPosition.value)} / ${formatTime(duration.value)}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
