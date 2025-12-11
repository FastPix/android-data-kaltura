package com.example.kaltura_player_data_sdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaltura_player_data_sdk.databinding.ActivityVideoListScreenBinding
import com.kaltura.androidx.media3.common.util.UnstableApi

class VideoListScreen : AppCompatActivity() {
    private lateinit var binding: ActivityVideoListScreenBinding
    private val videoAdapter by lazy {
        VideoAdapter()
    }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter.passDataToAdapter(dummyData)
        binding.recyclerView.adapter = videoAdapter

        videoAdapter.onVideoClick = { video ->
            val index = dummyData.indexOf(video)

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_VIDEO_MODEL, video)
            intent.putParcelableArrayListExtra(
                MainActivity.EXTRA_PLAYLIST,
                ArrayList(dummyData)
            )
            intent.putExtra(MainActivity.EXTRA_PLAYLIST_INDEX, index)

            startActivity(intent)
        }

    }}
