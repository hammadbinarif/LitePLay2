package com.hamrah.liteplay.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.hamrah.liteplay.AudioFile
import java.io.File


fun loadLocalMusicFiles(context: Context): List<AudioFile> {
    val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    val audioList = mutableListOf<AudioFile>()

    if (musicDir.exists() && musicDir.isDirectory) {
        musicDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension in listOf("mp3", "wav", "ogg", "m4a")) {
                val title = file.nameWithoutExtension
                val artist = "Unknown" // You could try to extract metadata if needed
                val uri = Uri.fromFile(file)
                audioList.add(AudioFile(title, artist, uri))
            }
        }
    }

    return audioList
}

