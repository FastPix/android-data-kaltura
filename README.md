# FastPix Kaltura Player SDK

[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-0.0.1-green.svg)](CHANGELOG.md)
[![Min SDK](https://img.shields.io/badge/minSdk-24-orange.svg)](theo-player-data/build.gradle.kts)

The FastPix Kaltura Player SDK provides seamless integration between Kaltura Player and the FastPix
analytics platform. This SDK automatically tracks video playback events, metrics, and analytics data
from your Kaltura instances, enabling real-time monitoring and insights on the FastPix dashboard.

## Key Features

- **Automatic Event Tracking** – Automatically captures all playback events (play, pause, seek,
  buffering, etc.)
- **Kaltura Player Integration** – Built specifically for Kaltura Player Android SDK
- **Real-time Analytics** – Provides instant access to video performance metrics on the FastPix
  dashboard
- **Minimal Setup** – Easy integration with just a few lines of code
- **Custom Metadata** – Support for custom video and player metadata

## Requirements

- **Minimum Android SDK**: 24 (Android 7.0)
- **Target/Compile SDK**: 35+
- **Kaltura Player SDK**: 5.0+
- **Kotlin**: 2.0.21+
- **Java**: 11

## Installation

### Step 1: Add GitHub Packages Repository

Add the GitHub Packages repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/FastPix/android-data-kaltura")
            credentials {
                username = "your-github-userName"
                password = "your-PAT"
            }
        }
    }
}
```

### Step 2: Add Dependencies

Add the FastPix Kaltura Player SDK and Kaltura Player dependencies to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // FastPix Kaltura Player SDK
    implementation "io.fastpix.data:kaltura:1.0.0"
   //Kaltura player SDK
   implementation("com.kaltura.player:tvplayer:5.0.3")
   implementation("com.kaltura.playkit:playkitproviders:5.0.3")
}
```


## Quick Start

### 1. Add Framelayout to Your Layout

Add the `framelayout` to your activity/fragment layout:

```xml

<FrameLayout
    android:id="@+id/playerContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

```

### 2. Initialize FastPix SDK in Your Activity

Here's a complete example of how to integrate FastPix SDK with Fastpix Player:

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var kalturaPlayer: KalturaBasicPlayer? = null
    private var fastPixKalturaPlayer: FastPixKalturaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKalturaPlayer()
        setupFastPixSDK()
    }

    private fun setupKalturaPlayer() {
        val initOptions = PlayerInitOptions().setAutoPlay(true)
        kalturaPlayer = KalturaBasicPlayer.create(this, initOptions)

        binding.playerContainer.addView(kalturaPlayer?.playerView)

        val entry = PKMediaEntry().apply {
            id = "video-id"
            mediaType = PKMediaEntry.MediaEntryType.Vod
            sources = listOf(
                PKMediaSource().apply {
                    id = "video-id"
                    url = "https://your-video-url.m3u8"
                }
            )
        }

        kalturaPlayer?.setMedia(entry)
    }

    private fun setupFastPixSDK() {
        val videoData = VideoDataDetails(
            videoId = "video-id",
            videoTitle = "Sample Video",
            videoSeries = "Demo Series",
            videoProducer = "Producer Name",
            videoContentType = "Video",
            videoVariant = "HD",
            videoLanguage = "en",
            videoCDN = "cloudflare"
        )

        val customData = CustomDataDetails(
            customField1 = "custom-value-1",
            customField2 = "custom-value-2"
        )

        val playerData = PlayerDataDetails(
            playerName = "Kaltura Basic Player",
            playerVersion = "5.0.3"
        )

        fastPixKalturaPlayer = FastPixKalturaPlayer(
            context = this,
            playerView = binding.playerContainer,
            kalturaPlayer = kalturaPlayer!!,
            workspaceId = "your-workspace-id",
            beaconUrl = "metrix.ninja",
            videoDataDetails = videoData,
            playerDataDetails = playerData,
            customDataDetails = customData,
            enableLogging = true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fastPixKalturaPlayer?.release()
        kalturaPlayer?.destroy()
    }
}

```

## Detailed Configuration

### CustomerData Parameters

The `CustomerData` class accepts the following parameters:

| Parameter           | Type              | Required | Description                                       |
|---------------------|-------------------|----------|---------------------------------------------------|
| `workSpaceId`       | String            | ✅        | Your FastPix workspace identifier                 |
| `beaconUrl`         | String            | ❌        | Custom beacon URL (default: metrix.ws)            |
| `videoData`      | VideoDataDetails  | ❌        | Video metadata (see below)                        |
| `playerData`        | PlayerDataDetails | ❌        | Player information (default: "theo-player", "5+") |
| `customData` | CustomDataDetails | ❌        | Custom metadata fields  |


### VideoDataDetails

Configure video metadata for better analytics:

```kotlin
val videoData = VideoDataDetails(
    videoId = "12345",
    videoTitle = "Sample Video",
    videoSeries = "Series Name",
    videoProducer = "Producer",
    videoContentType = "TV Show",
    videoVariant = "HD",
    videoLanguage = "en",
    videoCDN = "cloudflare"
)

```

### CustomDataDetails

Add custom metadata fields (up to 10 fields):

```kotlin
val customDataDetails = CustomDataDetails(
    customField1 = "value1",
    customField2 = "value2",
    // ... up to customField10
)
```

## Complete Sample Player Implementation

Here's a more complete example with custom controls:

```kotlin

```

## Lifecycle Management

It's crucial to properly manage the SDK lifecycle:

1. **Initialize** the SDK after Kaltura Player is configured
2. **Always call `release()`** in `onDestroy()` to clean up resources

```kotlin
override fun onDestroy() {
    fastPixKalturaPlayer?.release()
    kalturaPlayer?.destroy()
    super.onDestroy()
}
```

## Switching Videos

When switching to a new video, release and reinitialize:

```kotlin
fun switchVideo(newVideo: DummyData) {
    fastPixKalturaPlayer?.release()

    val entry = PKMediaEntry().apply {
        id = newVideo.id
        mediaType = PKMediaEntry.MediaEntryType.Vod
        sources = listOf(PKMediaSource().apply {
            id = newVideo.id
            url = newVideo.url
        })
    }

    kalturaPlayer?.setMedia(entry)

    setupFastPixSDK()
}

```

## Debugging

Enable logging during development:

```kotlin
     fastPixKalturaPlayer = FastPixKalturaPlayer(
            context = this,
            playerView = binding.playerContainer,
            kalturaPlayer = kalturaPlayer!!,
            workspaceId = "your-workspace-id",
            beaconUrl = "metrix.ninja",
            videoDataDetails = videoData,
            playerDataDetails = playerData,
            customDataDetails = customData,
            enableLogging = true // set to false in production
        )
```

Logs will appear in Logcat with the tag `FastPixKalturaPlayer`.

## Troubleshooting

### SDK Not Tracking Events

- Ensure you've initialized the SDK after configuring Kaltura Player
- Check that `workSpaceId` is correct
- Verify Kaltura Player events are firing (check Kaltura Player logs)
- Enable logging to see FastPix SDK activity

### Memory Leaks

- Always call `release()` in `onDestroy()`
- Ensure `  kalturaPlayer?.destroy()` is called before releasing FastPix SDK

### Missing Events

- The SDK automatically tracks all events from Kaltura Player
- Events are tracked based on Kaltura Player native event system
- Check that Kaltura player is properly configured and receiving events

## Support

For questions, issues, or feature requests:

- **Email**: support@fastpix.io
- **Documentation**: [FastPix Documentation](https://docs.fastpix.io)
- **GitHub Issues**: [Report an issue](https://github.com/FastPix/android-data-theoplayer/issues)

## License

Copyright © 2025 FastPix. All rights reserved.

This SDK is proprietary software. Unauthorized copying, modification, distribution, or use of this
software is strictly prohibited.
