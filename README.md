## Monitor Kaltura Player (Android)
The FastPix Data SDK with [KalturaPlayer](https://github.com/FastPix/android-data-KalturaPlayer) helps you track key video metrics like user interactions, playback quality, and performance to enhance the viewing experience. It lets you customize data tracking, monitor streaming quality, and securely send insights for better optimization and error resolution.

### Key features:
- Capture user engagement through detailed viewer interaction data.
- Monitor playback quality with real-time performance analysis.
- Identify and fix video delivery bottlenecks on Android.
- Customize tracking to match specific monitoring needs.
- Handle errors with robust reporting and diagnostics.
- Gain deep insights into video performance with streaming diagnostics.

### Prerequisites:
To track and analyze video performance, initialize the SDK with your Workspace key. Learn about [Workspaces](https://docs.fastpix.io/docs/workspaces).

1. Access the FastPix [Dashboard](https://dashboard.fastpix.io/login?redirect=https://dashboard.fastpix.io/): Log in and navigate to the Workspaces section.
2. Locate Your Workspace Key: Copy the Workspace Key for client-side monitoring. Include this key in your Swift code on every page where you want to track video performance.

### Step 1: Install and setup

1. Open your Android Studio project where you want to integrate the SDK.

2. Add the FastPix Data SDK dependency:

3. Navigate to your app-level `build.gradle` file (or `build.gradle.kts` if using Kotlin DSL).

```groovy
// FastPix Kaltura Player SDK
implementation "io.fastpix.data:kaltura:1.0.1"
```

Navigate to your `settings.gradle` file

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/FastPix/android-data-kalturaPlayer")
            credentials {
                username = "your-github-userName"
                password = "your-PAT"
            }
        }
    }
}
```

3. Sync your project with Gradle files
   Click `Sync Now` in the notification bar to download and integrate the FastPix Data SDK.


### Step 2: Import the SDK

Ensure kalturaPlayer is already configured in your project.
```kotlin
import io.fastpix.data.domain.model.CustomDataDetails
import io.fastpix.data.domain.model.PlayerDataDetails
import io.fastpix.data.domain.model.VideoDataDetails
import io.fastpix.data.kaltura_player_data.src.FastPixKalturaPlayer
```


### Step 3: Basic Integration

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

### Step 4: Including custom data and metadata
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

### CustomerData Parameters

The `CustomerData` class accepts the following parameters:

| Parameter           | Type              | Required | Description                                       |
|---------------------|-------------------|----------|---------------------------------------------------|
| `workSpaceId`       | String            | ✅        | Your FastPix workspace identifier                 |
| `beaconUrl`         | String            | ❌        | Custom beacon URL (default: metrix.ws)            |
| `videoData`      | VideoDataDetails  | ❌        | Video metadata (see below)                        |
| `playerData`        | PlayerDataDetails | ❌        | Player information |
| `customData` | CustomDataDetails | ❌        | Custom metadata fields  |




### What FastPix Tracks
Once initialized, the SDK automatically collects:
| Category          | Examples                                  |
| ----------------- | ----------------------------------------- |
| Playback events   | play, pause, playing, ended               |
| Buffer events     | buffering, buffered                       |
| Seek behavior     | seeking, seeked                           |
| QoS metrics       | bitrate, resolution, FPS (when available) |
| Errors            | kalturaPlayer error codes and messages       |
| Player metadata   | fullscreen, autoplay, MIME type etc       |


### Example to configure Kaltura with FastPix Data SDK.
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
### Debug Logging
Enable logs using:
```kotlin
enableLogging = true
```

## Troubleshooting

#### SDK Not Tracking Events

- Ensure you've initialized the SDK after configuring Kaltura Player
- Check that `workSpaceId` is correct
- Verify Kaltura Player events are firing (check Kaltura Player logs)
- Enable logging to see FastPix SDK activity

#### Memory Leaks

- Always call `release()` in `onDestroy()`
- Ensure ` kalturaPlayer?.destroy()` is called before releasing FastPix SDK

#### Missing Events

- The SDK automatically tracks all events from Kaltura Player
- Events are tracked based on Kaltura Player native event system
- Check that Kaltura player is properly configured and receiving events



### Support
📩 Email: support@fastpix.io
📚 Docs: https://docs.fastpix.io
