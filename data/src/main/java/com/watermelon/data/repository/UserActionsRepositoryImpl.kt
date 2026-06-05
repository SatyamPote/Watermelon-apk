package com.watermelon.data.repository

import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import com.watermelon.domain.repository.UserActionsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed repository for user-specific actions.
 *
 * Tables expected in Supabase:
 * - user_favorites        (user_id TEXT, song_id TEXT, favorited_at TIMESTAMPTZ)
 * - user_recently_played  (user_id TEXT, song_id TEXT, played_at TIMESTAMPTZ)
 * - user_playlists        (id UUID, user_id TEXT, name TEXT, description TEXT, cover_url TEXT)
 * - user_playlist_songs   (playlist_id UUID, song_id TEXT, position INT)
 *
 * For now these return mock data until the Supabase tables are created and queries are wired.
 */
@Singleton
class UserActionsRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : UserActionsRepository {

    private val mockSongs = listOf(
        Song("1","Blinding Lights","1","The Weeknd",null,null,200000L,"https://picsum.photos/seed/s1/300/300",null,"Pop","2020"),
        Song("2","Levitating","2","Dua Lipa",null,null,203000L,"https://picsum.photos/seed/s2/300/300",null,"Pop","2020"),
        Song("3","God's Plan","3","Drake",null,null,198000L,"https://picsum.photos/seed/s3/300/300",null,"Hip-Hop","2018"),
        Song("4","Save Your Tears","1","The Weeknd",null,null,215000L,"https://picsum.photos/seed/s4/300/300",null,"Pop","2020"),
        Song("5","Don't Start Now","2","Dua Lipa",null,null,183000L,"https://picsum.photos/seed/s5/300/300",null,"Pop","2019"),
        Song("6","One Dance","3","Drake",null,null,174000L,"https://picsum.photos/seed/s6/300/300",null,"Hip-Hop","2016")
    )

    private val mockPlaylists = listOf(
        Playlist("p1","My Favorites","Songs I love",null,"user"),
        Playlist("p2","Workout Mix","Gym motivation",null,"user")
    )

    override fun getRecentlyPlayed(): Flow<List<Song>> = flowOf(mockSongs.take(4))
    override fun getFavorites(): Flow<List<Song>> = flowOf(mockSongs.take(3))
    override fun getUserPlaylists(): Flow<List<Playlist>> = flowOf(mockPlaylists)

    override suspend fun addToFavorites(song: Song): Result<Unit> = runCatching {
        // TODO: Supabase query
        // client.postgrest["user_favorites"].insert(...)
    }

    override suspend fun removeFromFavorites(songId: String): Result<Unit> = runCatching {
        // TODO: Supabase query
    }

    override suspend fun recordRecentlyPlayed(song: Song): Result<Unit> = runCatching {
        // TODO: Supabase query
        // client.postgrest["user_recently_played"].insert(...)
    }
}
