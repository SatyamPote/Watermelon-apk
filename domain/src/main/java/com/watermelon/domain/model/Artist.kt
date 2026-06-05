package com.watermelon.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val bio: String?,
    val imageUrl: String?,
    val genres: List<String> = emptyList()
) : Parcelable
