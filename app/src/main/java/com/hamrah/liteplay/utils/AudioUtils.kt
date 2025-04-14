package com.hamrah.liteplay.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.hamrah.liteplay.AudioFile
import java.io.File

@SuppressLint("Range")
fun loadAudioFiles(): List<AudioFile> {
    val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    val audioList = mutableListOf<AudioFile>()

    if (musicDir.exists() && musicDir.isDirectory) {
        musicDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension.lowercase() in listOf("mp3", "wav", "m4a", "flac", "ogg")) {
                val title = file.nameWithoutExtension
                val artist = "Unknown Artist" // You could extract ID3 tags later if needed
                val path = file.absolutePath
                val parentFolder = file.parentFile?.name ?: "Unknown Folder"

                audioList.add(AudioFile(title, artist, path, parentFolder))
            }
        }
    }

    return audioList
}
