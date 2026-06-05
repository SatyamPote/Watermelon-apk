package com.watermelon.domain.repository

import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * External online music catalog.
 * No music metadata is stored in Supabase.
 */
interface MusicCatalogRepository {
    fun getTrendingMusic(): Flow<List<Song>>
    fun getRecommendedPlaylists(): Flow<List<Playlist>>
    fun search(query: String): Flow<List<Song>>
}
