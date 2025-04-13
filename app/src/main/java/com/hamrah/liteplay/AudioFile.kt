package com.hamrah.liteplay

import android.net.Uri

data class AudioFile(
    val title: String,
    val artist: String,
    val uri: Uri
)