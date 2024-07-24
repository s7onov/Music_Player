package org.hyperskill.musicplayer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyperskill.musicplayer.databinding.FragmentMainPlayerControllerBinding

class MainPlayerControllerFragment : Fragment(R.layout.fragment_main_player_controller) {
    private val vm: ActivityViewModel by activityViewModels<ActivityViewModel>()
    private lateinit var binding: FragmentMainPlayerControllerBinding
    private val updateCount: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            val curr = if (vm.mediaPlayer != null) vm.mediaPlayer?.currentPosition?.div(1000) else 0
            val seconds = curr?.rem(60)
            val minutes = curr?.div(60)
            val total = vm.currentTrack.song.duration.div(1000).toInt()
            vm.handler.post {
                if (!vm.seekBarTouch) {
                    binding.controllerTvCurrentTime.text = String.format("%02d:%02d", minutes, seconds)
                    if (curr != null) binding.controllerSeekBar.progress = curr
                }
                binding.controllerTvTotalTime.text = String.format("%02d:%02d", total / 60, total % 60)
                binding.controllerSeekBar.max = total
            }
            vm.handler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPlayerControllerBinding.inflate(inflater, container, false)
        return binding.root //super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.controllerBtnPlayPause.setOnClickListener {
            vm.playPause()
        }
        binding.controllerBtnStop.setOnClickListener {
            vm.stop()
        }
        binding.controllerSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.controllerTvCurrentTime.text = String.format("%02d:%02d", progress / 60, progress % 60)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                vm.seekBarTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) vm.mediaPlayer?.seekTo(seekBar.progress.times(1000))
                vm.seekBarTouch = false
            }
        })
        vm.handler.post(updateCount)
    }
}