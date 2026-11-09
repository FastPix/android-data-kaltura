# FastPix Video Data SDK for Kaltura Player - monitor playback performance and QoE on Android

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF.svg)](https://kotlinlang.org)

The **FastPix Video Data SDK for Kaltura Player** is a third-party analytics SDK that monitors playback performance, events, and quality-of-experience (QoE) metrics in **Kaltura Player on Android**. It attaches to your `KalturaPlayer` instance, automatically tracks playback events, and reports real-time metrics - startup time, rebuffering, bitrate, drop-offs, and errors - to the [FastPix dashboard](https://dashboard.fastpix.com).

It is **not a video player** and does not replace Kaltura Player. It is a third-party data SDK you add alongside the Kaltura Player you already ship - the same role Mux Data plays - so you can track quality of experience and viewer engagement without building an analytics pipeline.

**Works with:** Kaltura Player Android SDK (PlayKit) 5.0+ · Android 7.0+ · Kotlin and Java · `io.fastpix.data:kaltura`

[Monitor Kaltura Player docs](https://fastpix.com/docs/video-data/monitors/kaltura-player) · [Create a free account](https://dashboard.fastpix.com/signup?utm_source=github&utm_medium=referral&utm_campaign=github_signup)

## Jump to

Skip straight to a section without scrolling:

| Get started | Understand | Reference |
|---|---|---|
| [Prerequisites](#prerequisites) | [Why FastPix](#why-fastpix) | [What FastPix tracks](#what-fastpix-tracks) |
| [Install the SDK](#installation) | [Built-in analytics vs FastPix](#built-in-kaltura-analytics-vs-fastpix) | [Which FastPix SDK?](#which-fastpix-sdk-for-which-android-player) |
| [Basic integration](#basic-integration) | [What you can track](#what-you-can-track) | [FAQ](#faq) |
| [Full example](#example-to-configure-kaltura-with-fastpix-data-sdk) | [Who it's for](#who-its-for) | [Troubleshooting](#troubleshooting) |

## Why FastPix

Kaltura Player fires playback events, but turning them into viewer-facing quality metrics, storing them, and watching them across devices and app versions is work you would otherwise build yourself. FastPix does it for you:

- **Real-time QoE monitoring.** Startup time, rebuffering, bitrate and resolution changes, drop-offs, and errors as they happen on real devices, in one dashboard.
- **Automatic event tracking.** The SDK listens to Kaltura Player's native events, so you keep full control of playback and add monitoring with a few lines.
- **One data model across platforms.** The same FastPix Video Data metrics are reported from web, iOS, Android, and other player integrations, so you compare experiences like for like.
- **Your metadata, your dashboard.** Tag views with video, player, and up to ten custom fields and analyze them in the FastPix dashboard.

## Built-in Kaltura analytics vs FastPix

Kaltura ships its own analytics suite (the KMC dashboards and Player Studio) for views, engagement, and media usage across Rich Media CMS, MediaSpace, and LMS platforms like Canvas. FastPix is a **third-party data SDK** you run alongside the Kaltura Player when you want:

- Kaltura playback data unified with your other FastPix-monitored players (web, iOS, ExoPlayer, Media3, THEOplayer, Bitmovin, and more).
- Developer-level QoE metrics (startup time, rebuffering, errors) with custom dimensions and your own metadata on every view.
- One dashboard, API, and alerting surface across every platform you ship.

Use Kaltura's built-in analytics, FastPix, or both - they are not mutually exclusive. Mux Data fills the same third-party role; FastPix reports the same class of QoE metrics and unifies them across your players.

## What you can track

- **User engagement** - detailed viewer interaction data.
- **Playback quality** - real-time performance analysis.
- **Delivery bottlenecks** - identify and fix video delivery issues on Android.
- **Custom tracking** - match specific monitoring needs with custom metadata.
- **Error reporting** - robust error diagnostics.
- **Streaming diagnostics** - deep insight into video performance.

## Who it's for

- **Education and e-learning teams on Kaltura** (MediaSpace, Canvas, and other LMS integrations) who want developer-level QoE monitoring of the Android player.
- **Enterprise and media apps shipping Kaltura Player** that need to catch rebuffering, slow startup, and playback errors before users churn.
- **Teams standardizing analytics across platforms** who want Kaltura data in the same FastPix dashboard as their web and iOS players.

## Prerequisites

To track and analyze video performance, initialize the SDK with your workspace key. Learn about [Workspaces](https://fastpix.com/docs/getting-started/set-up-a-workspace).

1. Access the FastPix [Dashboard](https://dashboard.fastpix.com): Log in and navigate to the Workspaces section. (Need an account first? [Create a free account](https://dashboard.fastpix.com/signup?utm_source=github&utm_medium=referral&utm_campaign=github_signup).)
2. Locate Your Workspace Key: Copy the Workspace Key for client-side monitoring. Include this key in your Kotlin or Java code on every screen where you want to track video performance.
3. Ensure Kaltura Player (PlayKit) is already integrated and configured in your project.

## Installation

Navigate to your app-level `build.gradle` file (or `build.gradle.kts` if using Kotlin DSL) and add the FastPix Data SDK dependency:

```groovy
// FastPix Kaltura Player SDK
implementation "io.fastpix.data:kaltura:1.0.4"
```

Navigate to your `settings.gradle` file and add the GitHub Packages repository:

```groovy
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

Sync your project with Gradle files - click `Sync Now` in the notification bar to download and integrate the FastPix Data SDK.

## Import the SDK

Ensure kalturaPlayer is already configured in your project.
```kotlin
import io.fastpix.data.domain.model.CustomDataDetails
import io.fastpix.data.domain.model.PlayerDataDetails
import io.fastpix.data.domain.model.VideoDataDetails
import io.fastpix.data.kaltura_player_data.src.FastPixKalturaPlayer
```


## Basic Integration

Ensure that the `workSpaceId` is provided, as it is a mandatory field for FastPix integration, uniquely identifying your workspace. Install and import `FastPixKalturaPlayer` into your project, and create an `fastPixKalturaPlayer` instance to bind it to. If you are using any other custom player then create an instance of that player.

Next, create an instance of `FastPixKalturaPlayer` for tracking the analytics. Once the video URL is loaded and playback has started, the SDK will begin tracking the analytics.

```kotlin
import ...
class MainActivity : AppCompatActivity() {
 private var fastPixKalturaPlayer: FastPixKalturaPlayer? = null}
```

You can initialize KalturaBasicPlayer with a `kalturaPlayer` in your Android application to enable seamless functionality. Use the following Kotlin or Java code in your Android application to configure kalturaPlayer with FastPix:

```kotlin
        val videoData = VideoDataDetails(
            videoId = "video-id",
            videoTitle = "Sample Video",
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
            videoDataDetails = videoData,
            playerDataDetails = playerData,
            customDataDetails = customData,
            enableLogging = true
        )
```

## Including custom data and metadata
- `workSpaceId` is a mandatory parameter that tells the SDK on which workspace the data will collect.
- `playerView` is another mandatory parameter.

Check out the user-passable metadata documentation to see the metadata supported by FastPix. You can use custom metadata fields like `customField1` to `customField10` for your business logic, giving you the flexibility to pass any required values. Named attributes, such as `videoId` and `videoTitle`, can be passed directly as they are.

```kotlin
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
```

To set up video analytics, create a FastPixKalturaPlayer object by providing the following parameters: your application's `Context` (usually the Activity), the `KalturaPlayer` instance, and the `customerData`.

```kotlin
  fastPixKalturaPlayer = FastPixKalturaPlayer(
            context = this,
            playerView = binding.playerContainer,
            kalturaPlayer = kalturaPlayer!!,
            workspaceId = "your-workspace-id",
            videoDataDetails = videoData,
            playerDataDetails = playerData,
            customDataDetails = customData,
            enableLogging = true
        )
```

Finally, when destroying the player, make sure to call the `fastPixKalturaPlayer?.release()` function to properly release resources.

```kotlin
override fun onDestroy() {
    super.onDestroy()
    fastPixKalturaPlayer?.release() // Cleanup FastPix tracking
}
```

After completing the integration, start playing a video in your player. A few minutes after stopping playback, you’ll see the results in your FastPix Video Data dashboard. Log in to the dashboard, navigate to the workspace associated with your ws_key, and look for video views.

## CustomerData Parameters

The `CustomerData` class accepts the following parameters:

| Parameter           | Type              | Required | Description                                       |
|---------------------|-------------------|----------|---------------------------------------------------|
| `workSpaceId`       | String            | ✅        | Your FastPix workspace identifier                 |
| `beaconUrl`         | String            | ❌        | Custom beacon URL (default: metrix.ws)            |
| `videoData`      | VideoDataDetails  | ❌        | Video metadata (see below)                        |
| `playerData`        | PlayerDataDetails | ❌        | Player information |
| `customData` | CustomDataDetails | ❌        | Custom metadata fields  |




## What FastPix Tracks
Once initialized, the SDK automatically collects:
| Category          | Examples                                  |
| ----------------- | ----------------------------------------- |
| Playback events   | play, pause, playing, ended               |
| Buffer events     | buffering, buffered                       |
| Seek behavior     | seeking, seeked                           |
| QoS metrics       | bitrate, resolution, FPS (when available) |
| Errors            | kalturaPlayer error codes and messages       |
| Player metadata   | fullscreen, autoplay, MIME type etc       |


## Example to configure Kaltura with FastPix Data SDK.
Add your Stream URL in url `"your-stream-url.m3u8"` and FastPix Workspace Key in  `workspaceId`
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

        kalturaPlayer?.setPlayerView(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val playerView = kalturaPlayer?.getPlayerView() ?: run {
            return
        }

        binding.playerContainer.addView(playerView)

        val entry = PKMediaEntry().apply {
            mediaType = PKMediaEntry.MediaEntryType.Vod
            sources = listOf(
                PKMediaSource().apply {
                    url = "your-stream-url.m3u8"
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
## Debug Logging
Enable logs using:
```kotlin
enableLogging = true
```

## Which FastPix SDK for which Android player

FastPix ships a Video Data SDK per player. Pick the one that matches the player your app ships:

| Your Android player | Use this SDK |
| --- | --- |
| **Kaltura Player** | **FastPix/android-data-kaltura** (this repository) |
| **AndroidX Media3** (the current ExoPlayer) | [FastPix/android-data-androidXmedia3](https://github.com/FastPix/android-data-androidXmedia3) |
| **Google ExoPlayer** (standalone / legacy) | [FastPix/android-data-exoplayer-sdk](https://github.com/FastPix/android-data-exoplayer-sdk) |
| **THEOplayer** | [FastPix/android-data-theoplayer](https://github.com/FastPix/android-data-theoplayer) |
| **Bitmovin Player** | [FastPix/android-data-bitmovin](https://github.com/FastPix/android-data-bitmovin) |
| **Brightcove Player** | [FastPix/android-data-brightcove](https://github.com/FastPix/android-data-brightcove) |
| Any player, shared core layer | [FastPix/android-core-data-sdk](https://github.com/FastPix/android-core-data-sdk) |

## FAQ

**How do I monitor Kaltura Player performance on Android?**
Add the FastPix Video Data SDK, then create a `FastPixKalturaPlayer` with your `KalturaPlayer` instance, player view, and workspace key. It automatically tracks playback events and reports real-time QoE metrics to the FastPix dashboard. See [Install the SDK](#installation) and [Basic integration](#basic-integration).

**Does Kaltura have built-in analytics, and how is this different?**
Yes - Kaltura has its own analytics (KMC dashboards and Player Studio) for views, engagement, and media usage. FastPix is a third-party data SDK you run alongside it to get developer-level QoE metrics, unify Kaltura playback data with your other players, and use one dashboard and API across platforms. See [Built-in analytics vs FastPix](#built-in-kaltura-analytics-vs-fastpix).

**What metrics does it track?**
Playback and quality-of-experience signals: play/pause/seek/buffering events, startup time, rebuffering, bitrate and resolution changes, errors, and viewer engagement, plus any custom metadata you attach. See [What FastPix tracks](#what-fastpix-tracks).

**Is this a video player?**
No. It does not play or render video. It is a monitoring SDK that attaches to the Kaltura Player you already use.

**How does it compare to Mux Data for Kaltura Player?**
Both are third-party Video Data SDKs for Kaltura Player. FastPix reports the same class of QoE metrics and unifies Kaltura data with your web, iOS, and other FastPix-monitored players in one dashboard.

**Do I need a FastPix account?**
Yes. You need a workspace key from your FastPix account to report data. [Create a free account](https://dashboard.fastpix.com/signup?utm_source=github&utm_medium=referral&utm_campaign=github_signup).

## Troubleshooting

### SDK Not Tracking Events

- Ensure you've initialized the SDK after configuring Kaltura Player
- Check that `workSpaceId` is correct
- Verify Kaltura Player events are firing (check Kaltura Player logs)
- Enable logging to see FastPix SDK activity

### Memory Leaks

- Always call `release()` in `onDestroy()`
- Ensure ` kalturaPlayer?.destroy()` is called before releasing FastPix SDK

### Missing Events

- The SDK automatically tracks all events from Kaltura Player
- Events are tracked based on Kaltura Player native event system
- Check that Kaltura player is properly configured and receiving events

## Documentation

- [Monitor Kaltura Player - FastPix docs](https://fastpix.com/docs/video-data/monitors/kaltura-player)
- [FastPix Video Data overview](https://fastpix.com/docs/video-data/overview)
- [Use custom dimensions](https://fastpix.com/docs/video-data/use-custom-dimensions)
- [Kaltura Player (PlayKit) Android docs](https://developer.kaltura.com/player/android/getting-started-android)

## Support
📩 Email: support@fastpix.com 
📚 Docs: https://fastpix.com/docs

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
