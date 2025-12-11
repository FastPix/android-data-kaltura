package io.fastpix.data.kaltura_player_data.src

enum class KalturaPlayerEvent {
    PLAY,
    PLAYING,
    PAUSE,
    SEEKING,
    SEEKED,
    BUFFERING,
    BUFFERED,
    ENDED,
    ERROR,
    VARIANT_CHANGED
}

internal val validTransitions = mapOf(
    null to setOf(KalturaPlayerEvent.PLAY, KalturaPlayerEvent.ERROR),
    KalturaPlayerEvent.PLAY to setOf(
        KalturaPlayerEvent.PLAYING,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.PAUSE,
        KalturaPlayerEvent.VARIANT_CHANGED,
        KalturaPlayerEvent.SEEKING,
        KalturaPlayerEvent.ERROR
    ),
    KalturaPlayerEvent.PLAYING to setOf(
        KalturaPlayerEvent.BUFFERING,
        KalturaPlayerEvent.PAUSE,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.SEEKING,
        KalturaPlayerEvent.VARIANT_CHANGED,
        KalturaPlayerEvent.ERROR
    ),
    KalturaPlayerEvent.BUFFERING to setOf(
        KalturaPlayerEvent.BUFFERED,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.VARIANT_CHANGED
    ),
    KalturaPlayerEvent.BUFFERED to setOf(
        KalturaPlayerEvent.PAUSE,
        KalturaPlayerEvent.SEEKING,
        KalturaPlayerEvent.PLAYING,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.VARIANT_CHANGED
    ),
    KalturaPlayerEvent.PAUSE to setOf(
        KalturaPlayerEvent.SEEKING,
        KalturaPlayerEvent.PLAY,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.VARIANT_CHANGED
    ),
    KalturaPlayerEvent.SEEKING to setOf(
        KalturaPlayerEvent.SEEKED,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.VARIANT_CHANGED
    ),
    KalturaPlayerEvent.SEEKED to setOf(
        KalturaPlayerEvent.PLAY,
        KalturaPlayerEvent.ENDED,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.VARIANT_CHANGED,
        KalturaPlayerEvent.PLAYING
    ),
    KalturaPlayerEvent.ENDED to setOf(
        KalturaPlayerEvent.PLAY,
        KalturaPlayerEvent.PAUSE,
        KalturaPlayerEvent.ERROR,
        KalturaPlayerEvent.SEEKING,
        KalturaPlayerEvent.VARIANT_CHANGED
    ),
    KalturaPlayerEvent.ERROR to setOf(
        KalturaPlayerEvent.PLAYING,
        KalturaPlayerEvent.PLAY,
        KalturaPlayerEvent.PAUSE
    )
)