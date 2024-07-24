package org.hyperskill.musicplayer

interface RecyclerViewDataPass {
    fun pass(trackNum: Int)
    fun passLongClick(position: Int)
}