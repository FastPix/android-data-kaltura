package io.fastpix.data.kaltura_player_data.src

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.View

import com.kaltura.androidx.media3.common.util.UnstableApi
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.tvplayer.KalturaBasicPlayer
import io.fastpix.data.FastPixDataSDK
import io.fastpix.data.domain.SDKConfiguration
import io.fastpix.data.domain.enums.PlayerEventType
import io.fastpix.data.domain.listeners.PlayerListener
import io.fastpix.data.domain.model.BandwidthModel
import io.fastpix.data.domain.model.CustomDataDetails
import io.fastpix.data.domain.model.ErrorModel
import io.fastpix.data.domain.model.PlayerDataDetails
import io.fastpix.data.domain.model.VideoDataDetails
import io.fastpix.data.kaltura_player_data.src.info.FastPixKalturaLibraryInfo
import kotlinx.coroutines.*
import java.io.IOException

/**
 * FastPix Kaltura Player wrapper that automatically integrates with FastPixDataSDK
 * This wrapper provides seamless integration between Kaltura Player and FastPix analytics
 */
@UnstableApi
class FastPixKalturaPlayer(
    private val context: Context,
    private val playerView: View,
    private val kalturaPlayer: KalturaBasicPlayer,
    private val workspaceId: String,
    private val videoDataDetails: VideoDataDetails? = null,
    private val enableLogging: Boolean = true,
    private val beaconUrl: String? = null,
    private val playerDataDetails: PlayerDataDetails? = null,
    private val customDataDetails: CustomDataDetails? = null,
) : PlayerListener {

    private val TAG = "FastPixKalturaPlayer"
    private lateinit var fastPixDataSDK: FastPixDataSDK

    // State machine for valid event transitions
    private var currentEventState: KalturaPlayerEvent? = null
    private val pendingVariantChangeEvents = mutableListOf<Boolean>()

    // Coroutine job for progress tracking
    private var progressJob: Job? = null

    // Player state tracking
    private var isSeeking = false
    private var isVideoPlaying = false
    private var isViewBeginSent = false
    private var isReleased = false
    private var useLastKnownPosition: Long? = null

    // Video properties
    private var currentVideoWidth: Int? = null
    private var currentVideoHeight: Int? = null
    private var currentBitRate: String? = null
    private var previousVideoWidth: Int? = null
    private var previousVideoHeight: Int? = null
    private var lastKnownPosition: Long = 0
    private var currentPosition: Long? = null
    private var lastKnownDuration: Long = 0
    private var sourceDuration: Long? = null
    private var errorMessage: String? = null
    private var errorCode: String? = null

    init {
        initializeFastPixSDK()
        setupPlayerListeners()
        startProgressTracking()
    }

    private fun initializeFastPixSDK() {
        fastPixDataSDK = FastPixDataSDK()
        kalturaPlayer.isPreload
        val config = SDKConfiguration(
            workspaceId = workspaceId,
            beaconUrl = beaconUrl,
            playerData = playerDataDetails ?: PlayerDataDetails(
                playerName = "kaltura_basic_player",
                playerVersion = "5.0.3"
            ),
            videoData = videoDataDetails,
            playerListener = this,
            enableLogging = enableLogging,
            customData = customDataDetails
        )

        fastPixDataSDK.initialize(config, context)

        if (enableLogging) {
            Log.d(TAG, "FastPix SDK initialized")
        }
        dispatchViewBegin()
        dispatchPlayerReadyEvent()
    }



    private fun setupPlayerListeners() {
        kalturaPlayer.addListener(this, PlayerEvent.stateChanged) { event ->


            when (event.newState) {
                PlayerState.BUFFERING -> {
                    if (!isSeeking) {
                        dispatchBufferingEvent()
                    }
                }

                PlayerState.READY -> {
                    if (event.oldState == PlayerState.BUFFERING && !isSeeking) {
                        dispatchBufferedEvent()
                    }

                    if (isSeeking) {
                        dispatchSeekedEvent()
                    }
                }

                else -> {}
            }
        }

        kalturaPlayer.addListener(this, PlayerEvent.play) {
            dispatchPlayEvent()
        }

        kalturaPlayer.addListener(this, PlayerEvent.playing) {
            if (enableLogging) {
                Log.i(TAG, "Kaltura PLAYING event fired")
            }
            isVideoPlaying = true
            dispatchPlayingEvent()
        }

        kalturaPlayer.addListener(this, PlayerEvent.playheadUpdated) { event ->
            currentPosition = event.position
        }

        kalturaPlayer.addListener(this, PlayerEvent.pause) {
            if (!isSeeking) {
                dispatchPauseEvent()
            }
        }

        kalturaPlayer.addListener(this, PlayerEvent.seeking) {
            if (enableLogging) {
                Log.i(TAG, "SEEKING EVENT")
            }

            if (isVideoPlaying) {
                dispatchPauseEvent()
            }
            dispatchSeekingEvent()
        }

        kalturaPlayer.addListener(this, PlayerEvent.seeked) {
            if (enableLogging) {
                Log.i(TAG, "SEEKED EVENT (handled in stateChanged)")
            }
        }

        kalturaPlayer.addListener(this, PlayerEvent.playbackInfoUpdated) { event ->
            val videoBitrate = event.playbackInfo.videoBitrate
            val videoWidth = event.playbackInfo.videoWidth
            val videoHeight = event.playbackInfo.videoHeight
            if (videoWidth != null && videoHeight != null && videoWidth > 0 && videoHeight > 0) {
                val newWidth = videoWidth.toInt()
                val newHeight = videoHeight.toInt()
                currentBitRate = videoBitrate.toString()

                if (newWidth != previousVideoWidth || newHeight != previousVideoHeight) {
                    currentVideoWidth = newWidth
                    currentVideoHeight = newHeight
                    dispatchVariantChangeEvent()

                    previousVideoWidth = newWidth
                    previousVideoHeight = newHeight
                }
            }
        }

        kalturaPlayer.addListener(this, PlayerEvent.ended) {
            dispatchEndedEvent()
        }

        kalturaPlayer.addListener(this, PlayerEvent.durationChanged) {
            sourceDuration = it.duration
        }

        kalturaPlayer.addListener(this, PlayerEvent.error) { event ->
            errorMessage = event.error?.message
            errorCode = event.error?.errorType?.name
            if (enableLogging) {
                Log.e(TAG, "ERROR: $errorMessage")
            }
            dispatchErrorEvent()
        }
    }
    private fun startProgressTracking() {
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    val currentPos = kalturaPlayer.currentPosition
                    val duration = kalturaPlayer.duration
                    lastKnownPosition = currentPos
                    lastKnownDuration = duration
                } catch (e: IOException) {
                    if (enableLogging) {
                        Log.e(TAG, "Error tracking progress: ${e.message}")
                    }
                }

                delay(300)
            }
        }
    }
    private fun isValidTransition(newEvent: KalturaPlayerEvent): Boolean {
        val allowedTransitions = validTransitions[currentEventState] ?: emptySet()
        return newEvent in allowedTransitions
    }

    private fun transitionToEvent(newEvent: KalturaPlayerEvent): Boolean {
        if (isValidTransition(newEvent)) {
            if (newEvent != KalturaPlayerEvent.VARIANT_CHANGED) {
                currentEventState = newEvent
            }
            if (enableLogging) {
                Log.d(TAG, " Valid transition to $newEvent")
            }
            return true
        } else {

            return false
        }
    }

    private fun dispatchViewBegin() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (!isViewBeginSent) {
            if (enableLogging) {
                Log.i(TAG, "✓ VIEW BEGIN")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.viewBegin)
            isViewBeginSent = true
        }
    }

    private fun dispatchPlayerReadyEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (enableLogging) {
            Log.i(TAG, "✓ PLAYER READY")
        }
        fastPixDataSDK.dispatchEvent(PlayerEventType.playerReady)
    }

    private fun dispatchPlayEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return

        if (currentEventState == null || transitionToEvent(KalturaPlayerEvent.PLAY)) {
            currentEventState = KalturaPlayerEvent.PLAY
            fastPixDataSDK.dispatchEvent(PlayerEventType.play)
            processQueuedVariantChangeEvents()
        }
    }
    private fun dispatchPlayingEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.PLAYING)) {
            if (enableLogging) {
                Log.i(TAG, " fastpix PLAYING")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.playing)
        }
    }
    private fun dispatchPauseEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.PAUSE)) {
            if (enableLogging) {
                Log.i(TAG, "PAUSED")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.pause)
        }
    }
    private fun dispatchSeekingEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.SEEKING)) {
            if (enableLogging) {
                Log.i(TAG, "SEEKING")
            }
            isSeeking = true
            fastPixDataSDK.dispatchEvent(PlayerEventType.seeking)
        }
    }

    private fun dispatchSeekedEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.SEEKED)) {
            if (enableLogging) {
                Log.i(TAG, "SEEKED")
            }
            isSeeking = false
            fastPixDataSDK.dispatchEvent(PlayerEventType.seeked)
        }
    }

    private fun dispatchBufferingEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.BUFFERING)) {
            if (enableLogging) {
                Log.i(TAG, "⏳ BUFFERING")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.buffering)
        }
    }

    private fun dispatchBufferedEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.BUFFERED)) {
            if (enableLogging) {
                Log.i(TAG, "✓ BUFFERED")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.buffered)
        }
    }

    private fun dispatchEndedEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.ENDED)) {
            if (enableLogging) {
                Log.i(TAG, "ENDED")
            }


            try {
                lastKnownPosition = kalturaPlayer.currentPosition
                lastKnownDuration = kalturaPlayer.duration
            } catch (e: IOException) {

            }


            fastPixDataSDK.dispatchEvent(PlayerEventType.ended)
            fastPixDataSDK.dispatchEvent(
                PlayerEventType.viewCompleted,
                useLastKnownPosition?.toInt()
            )
        }
    }


    private fun dispatchErrorEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (transitionToEvent(KalturaPlayerEvent.ERROR)) {
            if (enableLogging) {
                Log.e(TAG, "ERROR")
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.error)
        }
    }

    private fun dispatchVariantChangeEvent() {
        if (isReleased || !::fastPixDataSDK.isInitialized) return
        if (currentVideoWidth == null || currentVideoHeight == null) {
            return
        }

        if (transitionToEvent(KalturaPlayerEvent.VARIANT_CHANGED)) {
            if (enableLogging) {
            }
            fastPixDataSDK.dispatchEvent(PlayerEventType.variantChanged)
        } else {
            pendingVariantChangeEvents.add(true)
        }
    }

    private fun processQueuedVariantChangeEvents() {
        if (pendingVariantChangeEvents.isNotEmpty()) {
            pendingVariantChangeEvents.clear()
            dispatchVariantChangeEvent()
        }
    }


    override fun playerHeight(): Int? = playerView.height

    override fun playerWidth(): Int? = playerView.width

    override fun videoSourceWidth(): Int? = currentVideoWidth

    override fun videoSourceHeight(): Int? = currentVideoHeight

    override fun playHeadTime(): Int? {
        currentPosition?.let {
            if (it < 0) {
                return 0
            } else {
                return it.toInt()
            }
        }
        return 0
    }

    override fun mimeType(): String? = "application/x-mpegURL"

    override fun sourceFps(): String? = null

    override fun sourceAdvertisedBitrate(): String? = currentBitRate

    override fun sourceAdvertiseFrameRate(): String? = null

    override fun sourceDuration(): Int?= sourceDuration?.toInt()

    override fun isPause(): Boolean? = !kalturaPlayer.isPlaying

    override fun isAutoPlay(): Boolean? = true
    override fun preLoad(): Boolean? = kalturaPlayer.isPreload

    override fun isBuffering(): Boolean? = false

    override fun playerCodec(): String? = null
    override fun sourceHostName(): String? {
        return runCatching {
            val url = kalturaPlayer.mediaEntry?.sources?.firstOrNull()?.url ?: return null
            java.net.URL(url).host
        }.getOrNull()
    }

    override fun isLive(): Boolean? = kalturaPlayer.isLive

    override fun sourceUrl(): String? {
        return runCatching {
            kalturaPlayer.mediaEntry?.sources?.firstOrNull()?.url
        }.getOrNull()
    }

    override fun isFullScreen(): Boolean? {
        val orientation = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }


    override fun getBandWidthData(): BandwidthModel = BandwidthModel()

    override fun getPlayerError(): ErrorModel = ErrorModel(errorCode, errorMessage)

    override fun getSoftwareName(): String? = FastPixKalturaLibraryInfo.SDK_NAME

    override fun getSoftwareVersion(): String? = FastPixKalturaLibraryInfo.SDK_VERSION

    override fun getVideoCodec(): String? = null

    /**
     * Release the FastPix SDK and stop tracking
     * Call this when you're done with the player
     */
    fun release() {
        if (enableLogging) {
            Log.d(TAG, "Releasing FastPix Kaltura Player")
        }

        try {
            lastKnownPosition = kalturaPlayer.currentPosition
            lastKnownDuration = kalturaPlayer.duration

        } catch (e: IOException) {
            if (enableLogging) {
                Log.w(TAG, "Could not capture final position: ${e.message}")
            }
        }

        if (::fastPixDataSDK.isInitialized) {
            try {

            } catch (e: IOException) {
                if (enableLogging) {
                    Log.w(TAG, "Error dispatching viewCompleted: ${e.message}")
                }
            }
        }

        Thread.sleep(100)

        isReleased = true

        progressJob?.cancel()

        if (::fastPixDataSDK.isInitialized) {
            try {
                fastPixDataSDK.release(useLastKnownPosition?.toInt())
            } catch (e: IOException) {
                if (enableLogging) {
                    Log.e(TAG, "Error releasing SDK: ${e.message}")
                }
            }
        }

        currentEventState = null
        pendingVariantChangeEvents.clear()
    }


}
