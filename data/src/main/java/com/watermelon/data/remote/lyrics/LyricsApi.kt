package com.watermelon.data.remote.lyrics

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface LyricsApi {

    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): LyricsResponse
}

data class LyricsResponse(
    @SerializedName("lyrics") val lyrics: String?,
    @SerializedName("error") val error: String?
)
