package com.watermelon.data.remote.youtube

import com.watermelon.domain.repository.UrlExtractorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
        val videoId = extractVideoId(sourceUrl)
            ?: return@withContext Result.failure(
                IllegalStateException("Could not extract video ID from $sourceUrl")
            )

        // 1. Cobalt.tools - actively maintained YouTube bypass
        runCatching {
            val cobaltUrl = fetchCobaltAudioUrl(sourceUrl)
                ?: throw IllegalStateException("Cobalt returned no audio URL")
            return@withContext Result.success(cobaltUrl)
        }.onFailure { e -> lastException = e }

        // 2. NewPipeExtractor
        runCatching {
            val streamInfo = StreamInfo.getInfo(youtube, sourceUrl)
            val audioStream = streamInfo.audioStreams
                .filter { it.isUrl }
                .maxByOrNull { it.averageBitrate }
                ?: throw IllegalStateException("No audio stream available")
            return@withContext Result.success(audioStream.content)
        }.onFailure { e ->
            lastException = e
        }

        // 3. Piped proxy
        runCatching {
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
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
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

    private fun fetchCobaltAudioUrl(sourceUrl: String): String? {
        try {
            val bodyJson = """{"url":"$sourceUrl","isAudioOnly":true,"aFormat":"best"}"""
            val requestBody = bodyJson.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.cobalt.tools/api/json")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val parsed = json.parseToJsonElement(body).jsonObject
            val status = parsed["status"]?.jsonPrimitive?.content ?: return null
            return when (status) {
                "stream", "tunnel" -> parsed["url"]?.jsonPrimitive?.content
                "picker" -> parsed["picker"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("url")?.jsonPrimitive?.content
                else -> null
            }
        } catch (_: Exception) {
            return null
        }
    }
}
