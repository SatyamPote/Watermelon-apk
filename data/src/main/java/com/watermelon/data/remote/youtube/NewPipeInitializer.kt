package com.watermelon.data.remote.youtube

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeInitializer @Inject constructor(
    downloader: YouTubeDownloader
) {
    init {
        NewPipe.init(downloader, Localization.DEFAULT, ContentCountry.DEFAULT)
        try {
            val clazz = Class.forName(
                "org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor"
            )
            val field = clazz.getDeclaredField("fetchIosClient")
            field.isAccessible = true
            field.setBoolean(null, true)
        } catch (_: Exception) {
            // Reflection failed, iOS client stays disabled
        }
    }
}
