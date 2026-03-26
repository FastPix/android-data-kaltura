
package com.example.kaltura_player_data_sdk

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kaltura_player_data_sdk.databinding.ActivityMainBinding
import com.kaltura.androidx.media3.common.util.UnstableApi
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.PlayerEvent
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import io.fastpix.data.domain.model.CustomDataDetails
import io.fastpix.data.domain.model.PlayerDataDetails
import io.fastpix.data.domain.model.VideoDataDetails
import io.fastpix.data.kaltura_player_data.src.FastPixKalturaPlayer

import kotlinx.coroutines.*
import java.util.UUID


@UnstableApi
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SEEK_INCREMENT = 10000L
        const val EXTRA_VIDEO_MODEL = "video_model"
        const val EXTRA_PLAYLIST = "playlist"
        const val EXTRA_PLAYLIST_INDEX = "playlist_index"
    }
    private lateinit var binding: ActivityMainBinding
    private var kalturaPlayer: KalturaBasicPlayer? = null
    private var fastPixKalturaPlayer: FastPixKalturaPlayer? = null

    private lateinit var playPauseButton: ImageButton
    private lateinit var rewindButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var centerControls: LinearLayout
    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var fullscreenButton: ImageButton
    private lateinit var exitFullscreenButton: ImageButton
    private var progressJob: Job? = null
    private var hideControlsJob: Job? = null
    private var isUserSeeking = false
    private var mediaDuration: Long = 0L
    private var isDurationReady = false

    private var playlist: ArrayList<DummyData>? = null
    private var currentIndex: Int = 0
    private var currentVideo: DummyData? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentVideo = intent.getParcelableExtra(EXTRA_VIDEO_MODEL, DummyData::class.java)
        playlist = intent.getParcelableArrayListExtra(EXTRA_PLAYLIST, DummyData::class.java)
        currentIndex = intent.getIntExtra(EXTRA_PLAYLIST_INDEX, 0)

        initViews()
        initKalturaPlayer()
        setupControlListeners()
        setupPlayerTouchListener()
        updateNextButton()
    }

    private fun initViews() {
        playPauseButton = findViewById(R.id.play_pause_button)
        rewindButton = findViewById(R.id.rewind_button)
        forwardButton = findViewById(R.id.forward_button)
        nextButton = findViewById(R.id.next_button)
        centerControls = findViewById(R.id.center_controls)
        seekBar = findViewById(R.id.seek_bar)
        currentTimeText = findViewById(R.id.current_time)
        totalTimeText = findViewById(R.id.total_time)
        fullscreenButton = findViewById(R.id.fullscreen_button)
        exitFullscreenButton = findViewById(R.id.exitFullScreen_button)
    }

    private fun initKalturaPlayer() {
        val playerInitOptions = PlayerInitOptions()
        playerInitOptions.setAutoPlay(true)

        kalturaPlayer = KalturaBasicPlayer.create(this, playerInitOptions)
        kalturaPlayer?.setPlayerView(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        (binding.playerContainer as ViewGroup).addView(kalturaPlayer?.playerView)
        loadVideo(currentVideo)

        kalturaPlayer?.addListener(this, PlayerEvent.durationChanged) { event ->
            val duration = event.duration
            if (duration > 0) {
                mediaDuration = duration
                isDurationReady = true
                seekBar.max = duration.toInt()
                totalTimeText.text = formatTime(duration)
                Log.d(TAG, "Duration changed: ${formatTime(duration)}")
            }
        }
        kalturaPlayer?.addListener(this, PlayerEvent.playing) {
            updatePlayPauseButton(true)
            scheduleHideControls()
        }
        kalturaPlayer?.addListener(this, PlayerEvent.pause) {
            updatePlayPauseButton(false)
            showControls()
        }
        kalturaPlayer?.addListener(this, PlayerEvent.ended) {
            playNextVideo()
        }
        initializeAnalyticsSDKs()
    }

    private fun loadVideo(video: DummyData?) {
        video?.let {

            isDurationReady = false
            mediaDuration = 0L
            totalTimeText.text = "--:--"
            seekBar.progress = 0

            val entry = PKMediaEntry().apply {
                id = it.id
                mediaType = PKMediaEntry.MediaEntryType.Vod

                val source = PKMediaSource().apply {
                    id = it.id
                    url = it.url
                }
                sources = listOf(source)
            }

            kalturaPlayer?.setMedia(entry)
            Log.d(TAG, "Loaded video: ${it.id}")
        }
    }

    private fun initializeAnalyticsSDKs() {
        initializeFastPixSDK()
    }

    private fun initializeFastPixSDK() {
        kalturaPlayer?.let { player ->
            val videoDataDetails = VideoDataDetails(
                videoId = currentVideo?.id ?: UUID.randomUUID().toString(),
                videoTitle = currentVideo?.id ?: "Unknown Title"
            ).apply {
                videoSeries = "Kaltura Video Series"
                videoProducer = "Kaltura Producer"
                videoContentType = "HLS Stream"
                videoVariant = "HLS"
                videoLanguage = "en"
                videoCDN = "cloudflare"
            }

            val customDataDetails = CustomDataDetails().apply {
                customField1 = "Kaltura Player"
                customField2 = "Custom 2"
            }

            val playerDataDetails = PlayerDataDetails(
                playerName = "kaltura_basic_player",
                playerVersion = "5.0.3"
            )

            fastPixKalturaPlayer = FastPixKalturaPlayer(
                context = this,
                playerView = binding.playerContainer,
                kalturaPlayer = player,
                workspaceId = "your-workspace-id",
                videoDataDetails = videoDataDetails,
                playerDataDetails = playerDataDetails,
                customDataDetails = customDataDetails
            )

            Log.d(TAG, "✓ FastPix SDK initialized for video: ${currentVideo?.id}")
        }
    }

private fun playNextVideo() {
    Log.d(TAG, "playNextVideo() called")

    playlist?.let { list ->
        if (currentIndex < list.size - 1) {
                currentIndex++
                currentVideo = list[currentIndex]

                Log.d(TAG, "Playing next video: ${currentVideo?.id} (${currentIndex + 1}/${list.size})")

                releaseFastPixSDK()
                loadVideo(currentVideo)
                initializeFastPixSDK()
                updateNextButton()

        } else {
            Log.d(TAG, "End of playlist")
        }
    }
}


    private fun releaseFastPixSDK() {
        try {
            fastPixKalturaPlayer?.release()
            fastPixKalturaPlayer = null
            Log.d(TAG, "✓ FastPix SDK released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing FastPix SDK: ${e.message}")
        }
    }

    private fun updateNextButton() {
        playlist?.let { list ->
            nextButton.isEnabled = currentIndex < list.size - 1
            nextButton.alpha = if (currentIndex < list.size - 1) 1.0f else 0.5f
        } ?: run {
            nextButton.visibility = View.GONE
        }
    }

    private fun setupControlListeners() {
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        rewindButton.setOnClickListener {
            seekRelative(-SEEK_INCREMENT)
            showControls()
            scheduleHideControls()
        }

        forwardButton.setOnClickListener {
            seekRelative(SEEK_INCREMENT)
            showControls()
            scheduleHideControls()
        }

        nextButton.setOnClickListener {
            playNextVideo()
            showControls()
            scheduleHideControls()
        }

        fullscreenButton.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        exitFullscreenButton.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentTimeText.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                showControls()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                kalturaPlayer?.seekTo(seekBar?.progress?.toLong() ?: 0)
                scheduleHideControls()
            }
        })

        startProgressUpdate()
    }

    private fun setupPlayerTouchListener() {
        binding.playerContainer.setOnClickListener {
            if (centerControls.visibility == View.VISIBLE) {
                hideControls()
            } else {
                showControls()
                scheduleHideControls()
            }
        }
    }

    private fun togglePlayPause() {
        kalturaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    private fun seekRelative(milliseconds: Long) {
        kalturaPlayer?.let { player ->
            val currentPos = player.currentPosition
            val newPos = (currentPos + milliseconds).coerceIn(0, mediaDuration)
            player.seekTo(newPos)
        }
    }

    private fun showControls() {
        centerControls.visibility = View.VISIBLE
        nextButton.visibility = View.VISIBLE
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            exitFullscreenButton.visibility = View.VISIBLE
            fullscreenButton.visibility = View.GONE
        } else {
            fullscreenButton.visibility = View.VISIBLE
            exitFullscreenButton.visibility = View.GONE
        }
        hideControlsJob?.cancel()
    }

    private fun hideControls() {
        centerControls.visibility = View.GONE
        nextButton.visibility = View.GONE
    }

    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = lifecycleScope.launch {
            delay(3000)
            if (kalturaPlayer?.isPlaying == true) {
                centerControls.visibility = View.GONE
            }
        }
    }
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    kalturaPlayer?.let { player ->
                        val currentPos = player.currentPosition
                        if (isDurationReady && !isUserSeeking) {
                            withContext(Dispatchers.Main) {
                                seekBar.progress = currentPos.toInt()
                                currentTimeText.text = formatTime(currentPos)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating progress: ${e.message}")
                }
                delay(1000)
            }
        }
    }

    private fun formatTime(millis: Long): String {

        if (millis < 0) return "--:--"

        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullscreenButton.visibility = View.GONE
            exitFullscreenButton.visibility = View.VISIBLE
        } else {
            fullscreenButton.visibility = View.VISIBLE
            exitFullscreenButton.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        progressJob?.cancel()
        hideControlsJob?.cancel()
        kalturaPlayer?.onApplicationPaused()
    }

    override fun onResume() {
        super.onResume()
        kalturaPlayer?.onApplicationResumed()
        startProgressUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying player and SDKs")

        progressJob?.cancel()
        hideControlsJob?.cancel()


        kalturaPlayer?.destroy()
        kalturaPlayer = null
        fastPixKalturaPlayer?.release()
    }
}









