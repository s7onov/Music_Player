package org.hyperskill.musicplayer

//import androidx.lifecycle.repeatOnLifecycle
import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.hyperskill.musicplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val vm: ActivityViewModel by viewModels()
    private val recyclerViewDataPassMain: RecyclerViewDataPass = object : RecyclerViewDataPass {
        override fun pass(trackNum: Int) {
            //we get data from adapter here
            //assign parameters to activity variables or do the needed operations
            if (trackNum == vm.currentTrackPara.value.index) {
                vm.playPause()
            } else {
                vm.stop()
                vm.setCurrentTrackIndex(trackNum)
                vm.setCurrentTrackState(Track.PLAYED)
            }
        }
        override fun passLongClick(position: Int) {
            vm.loadSelectorList(Playlist.ALLSONGS)
            vm.selectorList.forEach { it.isSelected = (vm.currentPlaylist[position].song == it.song) }
            vm.setState(ActivityViewModel.ADD_PLAYLIST)
        }
    }
    private val recyclerViewDataPassSelector: RecyclerViewDataPass = object : RecyclerViewDataPass {
        override fun pass(trackNum: Int) {
            vm.selectorList[trackNum].isSelected = !vm.selectorList[trackNum].isSelected
            binding.mainSongList.adapter?.notifyItemChanged(trackNum)
        }
        override fun passLongClick(position: Int) { }
    }
    private val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val readPermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainSongList.layoutManager = LinearLayoutManager(this)

        binding.mainButtonSearch.setOnClickListener {
            //vm.genAllSongsPlaylist()
            loadFilesFromDevice()
        }
        lifecycleScope.launch {
            //repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    var fragment: Fragment? = null
                    when (state) {
                        ActivityViewModel.PLAY_MUSIC -> {
                            fragment = MainPlayerControllerFragment()
                            updateAdapter()
                        }
                        ActivityViewModel.ADD_PLAYLIST -> {
                            fragment = MainAddPlaylistFragment()
                            updateAdapter()
                        }
                    }
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainFragmentContainer, fragment!!)
                        .addToBackStack(null)
                        .commit()
                }
            //}
        }
        lifecycleScope.launch {
            //repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.currentTrackPara.collect { para ->
                    if (vm.currentPlaylist.isEmpty()) return@collect

                    if (vm.mediaPlayer == null || vm.currentPlaylist[vm.currentTrackPara.value.index].song.id != vm.currentTrack.song.id) {
                        vm.currentTrack = vm.currentPlaylist[vm.currentTrackPara.value.index]
                        val songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, vm.currentTrack.song.id.toLong())
                        //Toast.makeText(applicationContext, "load $songUri", Toast.LENGTH_SHORT).show()
                        vm.mediaPlayer = MediaPlayer.create(applicationContext, songUri)
                        //vm.mediaPlayer = MediaPlayer.create(applicationContext, R.raw.wisdom) //load current track
                        vm.mediaPlayer?.setOnCompletionListener {
                            vm.setCurrentTrackState(Track.STOPPED)
                        }
                    }

                    when (para.trackState) {
                        Track.PLAYED -> {
                            if (!vm.mediaPlayer?.isPlaying!!) vm.mediaPlayer?.start()
                        }
                        Track.PAUSED -> {
                            vm.mediaPlayer?.pause()
                        }
                        Track.STOPPED -> {
                            vm.mediaPlayer?.stop()
                            vm.mediaPlayer?.prepare()
                            vm.mediaPlayer?.seekTo(0)
                        }
                    }

                    vm.currentPlaylist[vm.currentTrackPara.value.index].state = para.trackState
                    binding.mainSongList.adapter?.notifyItemChanged(vm.currentTrackPara.value.index)
                }
            //}
        }
        vm.playerDbHelper = PlayerDbHelper(applicationContext)
    }

    private fun loadFilesFromDevice() {
        if ( hasPermission(readPermission) ) {
            loadAllFiles()
            loadAllSongsPlaylist()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    readPermission)) {
                /*showExplanation(
                    "Permission Needed",
                    "Rationale",
                    readPermission,
                    readPermissionRequestCode
                )*/
                requestPermission(readPermission, readPermissionRequestCode)
            } else {
                requestPermission(readPermission, readPermissionRequestCode)
            }
        }
    }

    private fun loadAllFiles() {
        val values = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION)

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, null, null, null
        )

        val list = mutableListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            var id: Int
            var artist: String
            var title: String
            var duration: Long
            val idColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val titleColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val durationColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            do {
                id = cursor.getInt(idColumn)
                artist = cursor.getString(artistColumn)
                title = cursor.getString(titleColumn)
                duration = cursor.getLong(durationColumn)
                list.add(Song(id, title, artist, duration))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        if (list.isNotEmpty()) {
            vm.playlists[Playlist.ALLSONGS] = list.toList()
        }
    }

    fun loadAllSongsPlaylist() {
        if (vm.playlists[Playlist.ALLSONGS] != null && vm.playlists[Playlist.ALLSONGS]!!.isNotEmpty()) {
            vm.loadPlaylist(Playlist.ALLSONGS)
            updateAdapter()
        } else {
            Toast.makeText(this, "no songs found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(manifestPermission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_GRANTED
        } else {
            PermissionChecker.checkSelfPermission(this, manifestPermission) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private fun showExplanation(
        title: String,
        message: String,
        permission: String,
        permissionRequestCode: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    requestPermission(
                        permission,
                        permissionRequestCode
                    )
                })
        builder.create().show()
    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        when (requestCode) {
            readPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If user presses allow
                    loadAllFiles()
                    loadAllSongsPlaylist()
                } else {
                    //If user presses deny
                    Toast.makeText(this, "Songs cannot be loaded without permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private fun updateAdapter() {
        if (vm.state.value == ActivityViewModel.PLAY_MUSIC) {
            binding.mainSongList.adapter = MainAdapter(vm.currentPlaylist, recyclerViewDataPassMain)
        } else if (vm.state.value == ActivityViewModel.ADD_PLAYLIST) {
            binding.mainSongList.adapter = SelectorAdapter(vm.selectorList, recyclerViewDataPassSelector)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.mainMenuAddPlaylist -> {
                if (vm.playlists[Playlist.ALLSONGS] == null) {
                    Toast.makeText(this, "no songs loaded, click search to load songs", Toast.LENGTH_SHORT).show()
                    return false
                }
                vm.loadSelectorList(Playlist.ALLSONGS)
                vm.setState(ActivityViewModel.ADD_PLAYLIST)
                true
            }
            R.id.mainMenuLoadPlaylist -> {
                val lists = mutableListOf<String>()
                lists.add(Playlist.ALLSONGS)
                lists.addAll(vm.playerDbHelper.queryPlaylistNames())
                val plList = /*vm.playlists.keys.*/lists.toTypedArray()
                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle("choose playlist to load")
                    .setNegativeButton("Cancel") { dialog, id ->
                        dialog.cancel()
                    }
                    .setItems(plList)  { dialog, which ->
                        if (vm.playlists[Playlist.ALLSONGS] == null) loadAllFiles()
                        if (plList[which] == Playlist.ALLSONGS) loadAllSongsPlaylist()
                        else {
                            vm.playlists[plList[which]] = createPlaylist(vm.playerDbHelper.queryPlaylist(plList[which]))
                            vm.loadPlaylist(plList[which])
                        }
                        updateAdapter()
                    }
                val alertDialog = builder.create()
                alertDialog?.show()
                true
            }
            R.id.mainMenuDeletePlaylist -> {
                val builder = AlertDialog.Builder(this)
                val plList = /*vm.playlists.keys.*/vm.playerDbHelper.queryPlaylistNames().filter { it != Playlist.ALLSONGS }.toTypedArray()
                builder
                    .setTitle("choose playlist to delete")
                    .setNegativeButton("Cancel") { dialog, id ->
                        dialog.cancel()
                    }
                    .setItems(plList)  { dialog, which ->
                        if (vm.playlistName.value == plList[which]) {
                            vm.loadPlaylistMusic(Playlist.ALLSONGS)
                        }
                        if (vm.selectorListName.value == plList[which]) {
                            vm.loadSelectorList(Playlist.ALLSONGS)
                        }
                        vm.playlists.remove(plList[which])
                        vm.playerDbHelper.deletePlaylist(plList[which])
                        updateAdapter()
                    }
                val alertDialog = builder.create()
                alertDialog?.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createPlaylist(queryPlaylist: List<Int>): List<Song> {
        val allSongs = vm.playlists[Playlist.ALLSONGS]
        val list = mutableListOf<Song>()
        queryPlaylist.forEach {
            list.addAll(allSongs!!.filter { song: Song -> song.id == it })
        }
        return list.toList()
    }

}
