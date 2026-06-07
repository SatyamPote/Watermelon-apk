package com.watermelon.data.remote.watermelon

import com.watermelon.data.remote.watermelon.model.WatermelonSearchResult
import com.watermelon.data.remote.watermelon.model.WatermelonSong
import com.watermelon.domain.model.Song
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatermelonRepository @Inject constructor(
    private val api: WatermelonApi
) {

    /**
     * Lightweight ping to warm up the Render server (cold start)
     */
    suspend fun ping(): Boolean {
        return runCatching {
            val health = api.health()
            Timber.d("Watermelon backend ping: ${health.status}")
            health.status == "ok"
        }.getOrDefault(false)
    }

    suspend fun search(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return runCatching {
            val results = api.search(query).map { it.toSong() }
            Timber.d("Watermelon search returned ${results.size} results")
            results
        }.onFailure { Timber.e(it, "Watermelon search failed") }.getOrDefault(emptyList())
    }

    suspend fun getSongInfo(videoId: String): Song? {
        return runCatching {
            api.getSong(videoId).toSong()
        }.onFailure { Timber.e(it, "getSongInfo failed: $videoId") }.getOrNull()
    }

    suspend fun getStreamUrl(videoId: String): String? {
        return runCatching {
            api.getStream(videoId).streamUrl
        }.onFailure { Timber.e(it, "getStreamUrl failed: $videoId") }.getOrNull()
    }

    private fun WatermelonSearchResult.toSong(): Song {
        return Song(
            id = id,
            title = title,
            artistId = "",
            artistName = artist,
            albumId = null,
            albumName = null,
            durationMs = parseDuration(duration),
            coverUrl = thumbnail.takeIf { it.isNotBlank() }
                ?: "https://i.ytimg.com/vi/$id/hqdefault.jpg",
            audioUrl = "https://www.youtube.com/watch?v=$id",
            genre = "",
            releaseDate = ""
        )
    }

    private fun WatermelonSong.toSong(): Song {
        return Song(
            id = id,
            title = title,
            artistId = "",
            artistName = artist,
            albumId = null,
            albumName = null,
            durationMs = parseDuration(duration),
            coverUrl = thumbnail.takeIf { it.isNotBlank() }
                ?: "https://i.ytimg.com/vi/$id/hqdefault.jpg",
            audioUrl = "https://www.youtube.com/watch?v=$id",
            genre = "",
            releaseDate = ""
        )
    }

    private fun parseDuration(dur: String): Long {
        return try {
            val parts = dur.split(":")
            when (parts.size) {
                2 -> (parts[0].toInt() * 60 + parts[1].toInt()) * 1000L
                3 -> (parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt()) * 1000L
                else -> 0L
            }
        } catch (_: Exception) {
            0L
        }
    }
}
