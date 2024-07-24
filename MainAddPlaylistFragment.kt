package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class MainAddPlaylistFragment : Fragment(R.layout.fragment_main_add_playlist) {
    private val vm: ActivityViewModel by activityViewModels<ActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.addPlaylistBtnCancel).setOnClickListener {
            vm.setState(ActivityViewModel.PLAY_MUSIC)
        }
        view.findViewById<Button>(R.id.addPlaylistBtnOk).setOnClickListener {
            val playlistName = view.findViewById<EditText>(R.id.addPlaylistEtPlaylistName).text.toString()
            val num = vm.selectorList.count { it.isSelected }
            if (num == 0) Toast.makeText(view.context, "Add at least one song to your playlist", Toast.LENGTH_SHORT).show()
            else if (playlistName.isEmpty()) Toast.makeText(view.context, "Add a name to your playlist", Toast.LENGTH_SHORT).show()
            else if (playlistName == Playlist.ALLSONGS) Toast.makeText(view.context, "All Songs is a reserved name choose another playlist name", Toast.LENGTH_SHORT).show()
            else {
                vm.playlists[playlistName] = vm.selectorList.filter { it.isSelected }.map { it.song }
                vm.playerDbHelper.savePlaylist(playlistName, vm.playlists[playlistName]!!)
                vm.setState(ActivityViewModel.PLAY_MUSIC)
            }
        }
    }
}