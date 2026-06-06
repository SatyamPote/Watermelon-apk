package com.watermelon.data.remote.youtube

import com.watermelon.domain.repository.UrlExtractorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeUrlExtractorImpl @Inject constructor(
    initializer: NewPipeInitializer,
    private val okHttpClient: OkHttpClient
) : UrlExtractorRepository {

    private val youtube by lazy { org.schabi.newpipe.extractor.ServiceList.YouTube }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun extractAudioUrl(sourceUrl: String): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Throwable? = null
        repeat(2) { attempt ->
            runCatching {
                val streamInfo = StreamInfo.getInfo(youtube, sourceUrl)
                val audioStream = streamInfo.audioStreams
                    .filter { it.isUrl }
                    .maxByOrNull { it.averageBitrate }
                    ?: throw IllegalStateException("No audio stream available")
                return@withContext Result.success(audioStream.content)
            }.onFailure { e ->
                lastException = e
                if (attempt < 1) delay(1000L * (attempt + 1))
            }
        }
        // Fallback to Piped proxy
        runCatching {
            val videoId = extractVideoId(sourceUrl)
                ?: throw IllegalStateException("Could not extract video ID from $sourceUrl")
            val pipedUrl = fetchPipedAudioUrl(videoId)
                ?: throw IllegalStateException("Piped returned no audio URL")
            return@withContext Result.success(pipedUrl)
        }.onFailure { e ->
            lastException = e
        }
        Result.failure(lastException ?: IllegalStateException("Extraction failed after all attempts"))
    }

    private val YOUTUBE_VIDEO_ID_REGEX = Regex(
        """(?:youtube\.com\/(?:watch\?(?:[^&]*&)*v=|shorts\/|live\/|embed\/|v\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})"""
    )

    private fun extractVideoId(sourceUrl: String): String? {
        return YOUTUBE_VIDEO_ID_REGEX.find(sourceUrl)?.groupValues?.get(1)
    }

    private fun fetchPipedAudioUrl(videoId: String): String? {
        val pipedInstances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://api.piped.projectmainstreet.org",
            "https://pipedapi.adminforge.de"
        )
        for (baseUrl in pipedInstances) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/streams/$videoId")
                    .header("User-Agent", "Watermelon/1.0")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) continue
                val body = response.body?.string() ?: continue
                val parsed = json.parseToJsonElement(body).jsonObject
                val audioStreams = parsed["audioStreams"]?.jsonArray
                if (!audioStreams.isNullOrEmpty()) {
                    return audioStreams[0].jsonObject["url"]?.jsonPrimitive?.content
                }
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }
}
