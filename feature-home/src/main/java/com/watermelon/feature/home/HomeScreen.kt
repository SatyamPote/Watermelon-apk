package com.watermelon.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.watermelon.core.designsystem.theme.WatermelonSpacing
import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onSearchClick = onSearchClick,
        onSettingsClick = onSettingsClick,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.welcomeMessage,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = WatermelonSpacing.md)
            ) {
                item {
                    SearchBarShortcut(onClick = onSearchClick)
                }

                if (uiState.recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recently Played")
                        SongRow(
                            songs = uiState.recentlyPlayed,
                            onSongClick = onSongClick
                        )
                    }
                }

                if (uiState.favorites.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Favorites")
                        SongRow(
                            songs = uiState.favorites,
                            onSongClick = onSongClick
                        )
                    }
                }

                if (uiState.recommendedPlaylists.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recommended Playlists")
                        PlaylistRow(
                            playlists = uiState.recommendedPlaylists,
                            onPlaylistClick = onPlaylistClick
                        )
                    }
                }

                if (uiState.trendingMusic.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Trending Music")
                        SongRow(
                            songs = uiState.trendingMusic,
                            onSongClick = onSongClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarShortcut(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WatermelonSpacing.md)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WatermelonSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(WatermelonSpacing.md))
            Text(
                text = "Search songs, artists...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(
            start = WatermelonSpacing.md,
            top = WatermelonSpacing.lg,
            bottom = WatermelonSpacing.sm
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SongRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = WatermelonSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
    ) {
        items(songs, key = { it.id }) { song ->
            SongItem(song = song, onClick = { onSongClick(song) })
        }
    }
}

@Composable
private fun SongItem(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(modifier = Modifier.size(140.dp)) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            // Play overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                androidx.compose.material3.Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(WatermelonSpacing.sm))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlaylistRow(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = WatermelonSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            PlaylistItem(playlist = playlist, onClick = { onPlaylistClick(playlist) })
        }
    }
}

@Composable
private fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = playlist.coverUrl,
            contentDescription = playlist.name,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(WatermelonSpacing.sm))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = playlist.description ?: "",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
