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

@Composable
fun FolderBrowserScreen(
    folders: List<String>,
    onFolderSelected: (String) -> Unit
) {
    LazyColumn {
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
}
