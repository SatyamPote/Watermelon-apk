package com.watermelon.data.repository

import com.watermelon.data.remote.lyrics.LyricsApi
import com.watermelon.domain.repository.LyricsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LyricsApi
) : LyricsRepository {

    override suspend fun getLyrics(artist: String, title: String): Result<String> {
        return runCatching {
            val response = api.getLyrics(artist, title)
            if (!response.error.isNullOrBlank()) {
                throw IllegalStateException(response.error)
            }
            response.lyrics ?: throw IllegalStateException("No lyrics found")
        }
    }
}
