package com.nijatva.videoplayer.data.model

import kotlinx.serialization.Serializable

/** A user-created playlist of videos (referenced by their media id). */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val videoIds: List<Long> = emptyList(),
)
