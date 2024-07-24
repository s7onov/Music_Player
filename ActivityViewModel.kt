package org.hyperskill.musicplayer

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ActivityViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _state = MutableStateFlow(savedStateHandle.get<Int>("state") ?: PLAY_MUSIC)
    val state: StateFlow<Int> get() = _state
    fun setState(newState: Int) {
        if (_state.value != newState) {
            _state.value = newState
            savedStateHandle["state"] = _state.value
        }
    }

    class Para(var index: Int, var trackState: Int)
    private var _currentTrackPara = MutableStateFlow(
        Para(savedStateHandle.get<Int>("currentTrackIndex") ?: 0,
            savedStateHandle.get<Int>("currentTrackState") ?: Track.STOPPED)
    )
    val currentTrackPara: StateFlow<Para> get() = _currentTrackPara
    fun setCurrentTrackIndex(newIndex: Int) {
        _currentTrackPara.value =
            Para(newIndex, _currentTrackPara.value.trackState)
        savePara()
    }
    fun setCurrentTrackState(newState: Int) {
        _currentTrackPara.value = Para(_currentTrackPara.value.index, newState)
        savePara()
    }
    private fun setCurrentTrackIndexAndState(newIndex: Int, newState: Int) {
        _currentTrackPara.value = Para(newIndex, newState)
        savePara()
    }
    private fun savePara() {
        savedStateHandle["currentTrackIndex"] = _currentTrackPara.value.index
        savedStateHandle["currentTrackState"] = _currentTrackPara.value.trackState
    }

    private val _playlistName = MutableStateFlow(savedStateHandle.get<String>("playlistName") ?: Playlist.ALLSONGS)
    val playlistName: StateFlow<String> get() = _playlistName
    private fun setPlaylistName(newName: String) {
        _playlistName.value = newName
        savedStateHandle["playlistName"] = _playlistName.value
    }
    private val _selectorListName = MutableStateFlow(savedStateHandle.get<String>("selectorListName") ?: Playlist.ALLSONGS)
    val selectorListName: StateFlow<String> get() = _selectorListName
    private fun setSelectorListName(newName: String) {
        _selectorListName.value = newName
        savedStateHandle["selectorListName"] = _selectorListName.value
    }

    fun playPause() {
        if (currentPlaylist.isEmpty()) return
        if (currentTrackPara.value.trackState == Track.PLAYED) {
            setCurrentTrackState(Track.PAUSED)
        } else {
            setCurrentTrackState(Track.PLAYED)
        }
    }

    fun stop() {
        if (currentPlaylist.isEmpty()) return
        setCurrentTrackState(Track.STOPPED)
    }

    fun genAllSongsPlaylist() {
        if (!playlists.containsKey(Playlist.ALLSONGS))
            playlists[Playlist.ALLSONGS] = Playlist.generatePlaylistAll()
    }

    fun loadPlaylist(key: String) {
        if (state.value == PLAY_MUSIC) loadPlaylistMusic(key)
        else if (state.value == ADD_PLAYLIST) loadSelectorList(key)
    }

    fun loadPlaylistMusic(key: String) {
        setPlaylistName(key)
        val saveFlag = currentPlaylist.isNotEmpty()
        if (saveFlag) {
            currentTrack = currentPlaylist[currentTrackPara.value.index]
        }
        val tempList = playlists[key]!!.map { Track(it) }
        var index = -1
        var founded = false
        if (saveFlag) {
            for (trackIndex in tempList.indices) {
                if (tempList[trackIndex].song == currentTrack.song) {
                    founded = true
                    index = trackIndex
                    break
                }
            }
        }
        if (founded) {
            currentPlaylist = tempList
            setCurrentTrackIndexAndState(index, currentTrack.state)
            currentTrack = currentPlaylist[index]
        } else {
            setCurrentTrackState(Track.STOPPED)
            currentPlaylist = tempList
            setCurrentTrackIndex(0)
        }
    }

    fun loadSelectorList(key: String) {
        setSelectorListName(key)
        val selected =  selectorList.filter { it.isSelected }.map { it.song }
        selectorList = playlists[key]!!.map { SongSelector(it) }
        if (state.value == ADD_PLAYLIST) selectorList.forEach { if (selected.contains(it.song)) it.isSelected = true }
    }

    companion object {
        const val PLAY_MUSIC = 1
        const val ADD_PLAYLIST = 2
    }

    val playlists = mutableMapOf<String, List<Song>>()
    var currentPlaylist = listOf<Track>()
    var selectorList = listOf<SongSelector>()
    var currentTrack: Track = Track(Song(-1, "", "", 0L))
    val handler = Handler(Looper.getMainLooper())
    var mediaPlayer: MediaPlayer? = null
    var seekBarTouch: Boolean = false
    lateinit var playerDbHelper: PlayerDbHelper
}