import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FolderBrowserScreen(
    folders: List<String>,
    onFolderSelected: (String) -> Unit,
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
    onTogglePlayPause: () -> Unit,
    isPlaying: Boolean


) {
    Column (modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(folders) { folder ->
                Text(
                    text = folder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderSelected(folder) }
                        .padding(16.dp)
                )
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

        // Place slider after list
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
