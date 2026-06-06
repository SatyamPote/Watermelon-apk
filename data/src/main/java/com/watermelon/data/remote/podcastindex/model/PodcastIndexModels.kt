package com.watermelon.data.remote.podcastindex.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("status") val status: String,
    @SerializedName("feeds") val feeds: List<PodcastFeed>,
    @SerializedName("count") val count: Int,
    @SerializedName("query") val query: String,
    @SerializedName("description") val description: String
)

data class EpisodesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<PodcastEpisode>,
    @SerializedName("count") val count: Int
)

data class RecentEpisodesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<PodcastEpisode>,
    @SerializedName("count") val count: Int
)

data class PodcastFeed(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("originalUrl") val originalUrl: String,
    @SerializedName("link") val link: String,
    @SerializedName("description") val description: String,
    @SerializedName("author") val author: String,
    @SerializedName("ownerName") val ownerName: String,
    @SerializedName("image") val image: String,
    @SerializedName("artwork") val artwork: String,
    @SerializedName("lastUpdateTime") val lastUpdateTime: Long,
    @SerializedName("lastCrawlTime") val lastCrawlTime: Long,
    @SerializedName("lastParseTime") val lastParseTime: Long,
    @SerializedName("lastGoodHttpStatusTime") val lastGoodHttpStatusTime: Long,
    @SerializedName("lastHttpStatus") val lastHttpStatus: Int,
    @SerializedName("contentType") val contentType: String,
    @SerializedName("itunesId") val itunesId: Long?,
    @SerializedName("generator") val generator: String,
    @SerializedName("language") val language: String,
    @SerializedName("type") val type: Int,
    @SerializedName("dead") val dead: Int,
    @SerializedName("chaptersUrl") val chaptersUrl: String?,
    @SerializedName("episodeCount") val episodeCount: Int
)

data class PodcastEpisode(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("link") val link: String,
    @SerializedName("description") val description: String,
    @SerializedName("guid") val guid: String,
    @SerializedName("datePublished") val datePublished: Long,
    @SerializedName("dateCrawled") val dateCrawled: Long,
    @SerializedName("enclosureUrl") val enclosureUrl: String,
    @SerializedName("enclosureLength") val enclosureLength: Long,
    @SerializedName("enclosureType") val enclosureType: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("explicit") val explicit: Int,
    @SerializedName("episode") val episode: Int?,
    @SerializedName("season") val season: Int?,
    @SerializedName("image") val image: String,
    @SerializedName("feedImage") val feedImage: String,
    @SerializedName("feedId") val feedId: Long,
    @SerializedName("feedUrl") val feedUrl: String,
    @SerializedName("feedTitle") val feedTitle: String,
    @SerializedName("feedLanguage") val feedLanguage: String,
    @SerializedName("chaptersUrl") val chaptersUrl: String?
)
