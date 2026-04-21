
# v1.0.3
- Upgrades Core SDK to 1.3.0

# v1.0.2
## Changed
- **SDK & Dependencies**
    - Updated `androidx.core:core-ktx` (`core`) from `1.2.7` to `1.2.9`.
    - Updated `README.md` dependency example to `1.0.2`.
- **FastPixKalturaPlayer Event Handling**
    - Adjusted pulse lifecycle to schedule periodic pulse events on `viewBegin`, `play`, and `buffering` transitions.
    - Improved seek handling so pulse events are cancelled only when playback remains paused after `seeked`, and resumed when playback continues.
    - Removed unnecessary pulse scheduling from `variantChanged` dispatch to avoid duplicate pulse triggers.
- **Stability**
    - Guarded `FastPixAnalytics.release(...)` with a null-check on `fastPixDataSDK` to avoid releasing analytics when SDK is not initialized.

# v1.0.1
## Changed
- **SDK & Dependencies**
    - Bumped `io.fastpix.data` version to `1.0.1`.
    - Updated `core` library version to `1.2.7`.
    - Removed `MuxKalturaSDK` and associated Maven repositories from the project.
- **FastPixKalturaPlayer Enhancements**
    - Switched to `FastPixAnalytics` singleton for SDK initialization and management.
    - Implemented a background pulse event mechanism (10-second interval) to track active playback sessions.
    - Improved event lifecycle management by ensuring pulse events are cancelled during buffering, seeking, pausing, or errors.
    - Refined `release()` logic to ensure proper state cleanup and final position reporting.
    - Updated `PlayerListener` interface implementation to match updated types for frame rate and duration.
- **Cleanup**
    - Removed unused `Utils.kt` and redundant Mux initialization code from `MainActivity`.
    - Cleaned up unused Kaltura library dependencies from the app module.

# V1.0.0
## Integration with Kaltura Player

- Added Kaltura Player integration with automatic video performance tracking using the FastPix Data SDK.
- Enabled playback event tracking, including play, pause, buffering, seek, and completion events.
- Added support for custom metadata such as video_id, video_title, video_series, video_language, and more.
- Included player metadata tracking with player name and version details.
- Added customizable behavior with options for logging, custom beacon URL, and metadata configuration.
- Implemented lifecycle-safe handling with automatic cleanup and resource release.
- Added support for switching videos with seamless tracking of new playback sessions.
