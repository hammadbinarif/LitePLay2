package com.hamrah.liteplay

import android.os.Bundle
import androidx.media3.session.SessionCommand

object CustomCommands {
    const val CMD_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
    const val CMD_TOGGLE_REPEAT = "TOGGLE_REPEAT"
    const val CMD_TOGGLE_PLAY_PAUSE = "TOGGLE_PLAY_PAUSE"
    const val CMD_NEXT = "PLAY_NEXT"
    const val CMD_PREV = "PLAY_PREVIOUS"
    const val CMD_PLAY_AT_INDEX = "PLAY_AT_INDEX"

    val toggleShuffle = SessionCommand(CMD_TOGGLE_SHUFFLE, Bundle.EMPTY)
    val toggleRepeat = SessionCommand(CMD_TOGGLE_REPEAT, Bundle.EMPTY)
    val togglePlayPause = SessionCommand(CMD_TOGGLE_PLAY_PAUSE, Bundle.EMPTY)
    val playNext = SessionCommand(CMD_NEXT, Bundle.EMPTY)
    val playPrevious = SessionCommand(CMD_PREV, Bundle.EMPTY)
    val playAtIndex = SessionCommand(CMD_PLAY_AT_INDEX,  Bundle.EMPTY)
}
