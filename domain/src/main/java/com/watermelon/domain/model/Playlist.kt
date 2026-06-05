package com.watermelon.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val coverUrl: String?,
    val ownerId: String,
    val songs: List<PlaylistSong> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Serializable
@Parcelize
data class PlaylistSong(
    val songId: String,
    val position: Int
) : Parcelable
