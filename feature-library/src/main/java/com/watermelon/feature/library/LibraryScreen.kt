package com.watermelon.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import android.os.Environment
import com.watermelon.core.designsystem.theme.WatermelonSpacing
import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBackClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSongClick: (Song) -> Unit,
    onCreatePlaylist: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val canCreate by viewModel.canCreatePlaylist.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPaywall by remember { mutableStateOf(false) }
    val tabs = listOf("Playlists", "Favorites", "Feed", "Downloads")
    val tabIcons = listOf(
        Icons.AutoMirrored.Filled.QueueMusic,
        Icons.Filled.Favorite,
        Icons.Filled.History,
        Icons.Filled.Download
    )

    var showDeleteDialog by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Library") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (canCreate) onCreatePlaylist() else showPaywall = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> PlaylistList(
                    playlists = playlists,
                    onPlaylistClick = onPlaylistClick,
                    onDeletePlaylist = { showDeleteDialog = it },
                    modifier = Modifier.fillMaxSize()
                )
                1 -> SongList(
                    songs = favorites,
                    onSongClick = onSongClick,
                    emptyText = "No favorites yet",
                    modifier = Modifier.fillMaxSize()
                )
                2 -> FeedContent(
                    recentlyPlayed = recentlyPlayed,
                    onSongClick = onSongClick,
                    modifier = Modifier.fillMaxSize()
                )
                3 -> DownloadsPlaceholder(onSongClick = onSongClick)
            }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Playlist") },
            text = { Text("Delete \"${showDeleteDialog?.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deletePlaylist(it.id) }
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPaywall) {
        AlertDialog(
            onDismissRequest = { showPaywall = false },
            title = { Text("Playlist Limit Reached") },
            text = {
                Text("Free users can create up to 3 playlists. Upgrade to Premium for unlimited playlists.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showPaywall = false
                    onNavigateToPremium()
                }) {
                    Text("Go Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaywall = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}

@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    if (playlists.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No playlists yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(WatermelonSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(playlist) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(WatermelonSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = playlist.coverUrl,
                            contentDescription = playlist.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(WatermelonSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = playlist.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    emptyText: String,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(WatermelonSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
        ) {
            items(songs, key = { it.id }) { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(WatermelonSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(WatermelonSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artistName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedContent(
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(WatermelonSpacing.md),
        verticalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
    ) {
        item {
            Text(
                text = "Recently Played",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = WatermelonSpacing.sm)
            )
        }
        if (recentlyPlayed.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Play some music to see your activity",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recentlyPlayed, key = { it.id }) { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(WatermelonSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(WatermelonSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artistName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DownloadMeta(val title: String, val artistName: String, val coverUrl: String)

@Composable
private fun DownloadsPlaceholder(onSongClick: (Song) -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var fileToDelete by remember { mutableStateOf<java.io.File?>(null) }

    val downloads = remember(refreshKey) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val audioFiles = dir?.listFiles()
            ?.filter { it.extension.equals("mp3", ignoreCase = true) || it.extension.equals("m4a", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        audioFiles.mapNotNull { file ->
            val metaFile = java.io.File(file.parent, file.nameWithoutExtension + ".json")
            val meta = if (metaFile.exists()) {
                try {
                    val json = org.json.JSONObject(metaFile.readText())
                    DownloadMeta(
                        title = json.optString("title", file.nameWithoutExtension),
                        artistName = json.optString("artistName", ""),
                        coverUrl = json.optString("coverUrl", "")
                    )
                } catch (_: Exception) { null }
            } else null
            file to meta
        }
    }

    if (downloads.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Downloaded songs will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(WatermelonSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WatermelonSpacing.md)
        ) {
            items(downloads, key = { it.first.absolutePath }) { (file, meta) ->
                val sizeText = remember(file) {
                    val len = file.length()
                    when {
                        len > 1024 * 1024 -> "%.1f MB".format(len / (1024.0 * 1024.0))
                        len > 1024 -> "%.1f KB".format(len / 1024.0)
                        else -> "${len} B"
                    }
                }
                val song = remember(file, meta) {
                    Song(
                        id = file.nameWithoutExtension,
                        title = meta?.title?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension,
                        artistId = meta?.artistName ?: "",
                        artistName = meta?.artistName?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
                        albumId = null,
                        albumName = null,
                        durationMs = 0,
                        coverUrl = meta?.coverUrl?.takeIf { it.isNotBlank() },
                        audioUrl = file.toURI().toString(),
                        genre = null,
                        releaseDate = null
                    )
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song) },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(WatermelonSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!meta?.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = meta!!.coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(WatermelonSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    if (!song.artistName.isNullOrBlank() && song.artistName != "Unknown Artist") {
                                        append(song.artistName)
                                        append(" · ")
                                    }
                                    append(sizeText)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { fileToDelete = file },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete Download") },
            text = { Text("Remove \"${fileToDelete?.nameWithoutExtension}\" from downloads? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fileToDelete?.let { f ->
                            f.delete()
                            java.io.File(f.parent, f.nameWithoutExtension + ".json").delete()
                            refreshKey++
                        }
                        fileToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}