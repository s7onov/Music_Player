package org.hyperskill.musicplayer

class Track(val song: Song) {
    var state = STOPPED
    companion object {
        const val PLAYED = 1
        const val PAUSED = 2
        const val STOPPED = 3
    }
}