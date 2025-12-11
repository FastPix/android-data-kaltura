package com.example.kaltura_player_data_sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


val dummyData = listOf<DummyData>(
    DummyData(
        "ca854fd4-a3d0-4525-bd43-80de50887e1a",
        "https://stream.fastpix.io/ca854fd4-a3d0-4525-bd43-80de50887e1a.m3u8",
    ),
    DummyData(
        "16ac212a-0f4f-49c5-9fd7-a42d9ff61541",
        "https://stream.mux.com/1o000002oktK92bFDoAWU5E1v895fCK9Bwy00KdQLya2jaM.m3u8",
    ),
    DummyData(
        "3ad91e5f-0f45-403f-bda0-2a668a3581ee",
        "https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8",
    ),
    DummyData(
        "46c09d0c-d97a-44b2-9737-c5e6daf30a41",
        "https://sdks.support.brightcove.com/assets/videos/hls/greatblueheron/greatblueheron.m3u8",
    )
)
@Parcelize
data class DummyData(
    var id: String,
    var url: String,
) : Parcelable