package org.hyperskill.musicplayer

class Playlist {

    companion object {
        const val ALLSONGS = "All Songs"
        fun generatePlaylistAll(): List<Song> {
            val n = 10
            val list = mutableListOf<Song>()
            for (i in 1..n) {
                list.add(Song(i, "title$i", "artist$i", 215_000))
            }
            return list.toList()
        }
    }
}