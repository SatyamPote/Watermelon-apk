package com.watermelon.data.dto

import com.watermelon.domain.model.Playlist
import kotlinx.serialization.Serializable

@Serializable
 data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val cover_url: String? = null,
    val created_at: String? = null
)

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    coverUrl = cover_url,
    ownerId = "user"
)
