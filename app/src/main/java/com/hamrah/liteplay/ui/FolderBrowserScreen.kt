import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.State

@Composable
fun FolderBrowserScreen(
    folders: List<String>,
    onFolderSelected: (String) -> Unit,
    playbackPosition: State<Long>,
    duration: State<Long>,
    onSeek: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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

        // Place slider after list
        PlaybackSlider(
            playbackPosition = playbackPosition,
            duration = duration,
            onSeek = onSeek
        )
    }
}
