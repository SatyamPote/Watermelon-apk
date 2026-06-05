package com.watermelon.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
