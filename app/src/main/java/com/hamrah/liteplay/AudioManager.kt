import com.hamrah.liteplay.AudioFile

object AudioManager {

    private var audioFiles: List<AudioFile> = emptyList()
    var currentIndex = 0
    private var shuffledList: List<AudioFile> = emptyList()
    var isShuffleEnabled: Boolean = false
        private set

    fun setAudioFiles(files: List<AudioFile>) {
        audioFiles = files
        shuffledList = files
    }

    fun getCurrentList(): List<AudioFile> {
        return if (isShuffleEnabled) shuffledList else audioFiles
    }

    fun shuffle() {
        isShuffleEnabled = true
        shuffledList = audioFiles.shuffled()
    }

    fun unshuffle() {
        isShuffleEnabled = false
    }

    fun toggleShuffle() {
        if (isShuffleEnabled) unshuffle() else shuffle()
    }

    fun getAudio(index: Int): AudioFile? {
        val list = getCurrentList()
        return if (index in list.indices) list[index] else null
    }

    fun groupByFolder(): Map<String, List<AudioFile>> {
        return audioFiles.groupBy { it.parentFolder }
    }

    fun size(): Int = getCurrentList().size
}
