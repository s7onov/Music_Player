package org.hyperskill.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(data: List<Track>, private val recyclerViewDataPass: RecyclerViewDataPass) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private var data: List<Track> = data
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false))
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val track = data[position]
        val song = track.song
        holder.songTvArt.text = song.artist
        holder.songTvTit.text = song.title
        holder.songTvDur.text = String.format("%02d:%02d", song.duration/1000/60, song.duration/1000%60)
        if (track.state == Track.PLAYED) holder.songImgBtn.setImageResource(R.drawable.ic_pause)
        else holder.songImgBtn.setImageResource(R.drawable.ic_play)
        holder.songImgBtn.setOnClickListener {
            recyclerViewDataPass.pass(position)
        }
        holder.itemView.setOnLongClickListener {
            recyclerViewDataPass.passLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songImgBtn: ImageButton = view.findViewById(R.id.songItemImgBtnPlayPause)
        val songTvArt: TextView = view.findViewById(R.id.songItemTvArtist)
        val songTvTit: TextView = view.findViewById(R.id.songItemTvTitle)
        val songTvDur: TextView = view.findViewById(R.id.songItemTvDuration)
    }

}