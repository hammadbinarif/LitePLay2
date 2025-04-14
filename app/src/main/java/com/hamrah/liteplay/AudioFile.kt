package com.hamrah.liteplay

import android.net.Uri

data class AudioFile(
    val title: String,
    val artist: String,
    val path: String, // full path to the file
    val parentFolder: String // extracted folder name
)
