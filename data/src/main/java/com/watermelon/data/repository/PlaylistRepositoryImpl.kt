package com.watermelon.data.repository

import com.watermelon.data.dto.PlaylistDto
import com.watermelon.data.dto.toDomain
import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import com.watermelon.domain.repository.PlaylistRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : PlaylistRepository {

    private val fallback = listOf(
        Playlist("p1","My Favorites","Songs I love",null,"user"),
        Playlist("p2","Workout Mix","Gym motivation",null,"user")
    )

    override fun getUserPlaylists(): Flow<List<Playlist>> = flow {
        val result = runCatching {
            client.postgrest["user_playlists"]
                .select()
                .decodeList<PlaylistDto>()
                .map { it.toDomain() }
        }
        emit(result.getOrDefault(fallback))
    }.flowOn(Dispatchers.IO)

    override suspend fun createPlaylist(name: String, description: String?, coverUrl: String?): Result<Playlist> = runCatching {
        val dto = PlaylistDto(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            description = description,
            cover_url = coverUrl
        )
        client.postgrest["user_playlists"].insert(dto)
        dto.toDomain()
    }

    override suspend fun addSongToPlaylist(playlistId: String, song: Song): Result<Unit> = runCatching {
        val entry = mapOf(
            "playlist_id" to playlistId,
            "song_id" to song.id,
            "song_title" to song.title,
            "song_artist" to song.artistName,
            "song_cover_url" to song.coverUrl,
            "position" to 0
        )
        client.postgrest["user_playlist_songs"].insert(entry)
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = runCatching {
        client.postgrest["user_playlist_songs"]
            .delete {
                filter { eq("playlist_id", playlistId) }
                filter { eq("song_id", songId) }
            }
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> = runCatching {
        client.postgrest["user_playlist_songs"]
            .delete { filter { eq("playlist_id", playlistId) } }
        client.postgrest["user_playlists"]
            .delete { filter { eq("id", playlistId) } }
    }
}
