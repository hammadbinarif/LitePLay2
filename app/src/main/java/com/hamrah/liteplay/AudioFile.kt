package com.hamrah.liteplay

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioFile(
    val title: String,
    val artist: String,
    val path: String, // full path to the file
    val parentFolder: String // extracted folder name
) : Parcelable
