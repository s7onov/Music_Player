package org.hyperskill.musicplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectorAdapter(data: List<SongSelector>, private val recyclerViewDataPass: RecyclerViewDataPass) : RecyclerView.Adapter<SelectorAdapter.MainViewHolder>() {

    private var data: List<SongSelector> = data
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_song_selector, parent, false))
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val songSelector = data[position]
        val song = songSelector.song
        holder.songTvArt.text = song.artist
        holder.songTvTit.text = song.title
        holder.songTvDur.text = String.format("%02d:%02d", song.duration/1000/60, song.duration/1000%60)
        holder.songChkBox.isChecked = songSelector.isSelected
        if (songSelector.isSelected) holder.itemView.setBackgroundColor(Color.LTGRAY)
        else holder.itemView.setBackgroundColor(Color.WHITE)
        holder.itemView.setOnClickListener {
            recyclerViewDataPass.pass(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songChkBox: CheckBox = view.findViewById(R.id.songSelectorItemCheckBox)
        val songTvArt: TextView = view.findViewById(R.id.songSelectorItemTvArtist)
        val songTvTit: TextView = view.findViewById(R.id.songSelectorItemTvTitle)
        val songTvDur: TextView = view.findViewById(R.id.songSelectorItemTvDuration)
    }

}