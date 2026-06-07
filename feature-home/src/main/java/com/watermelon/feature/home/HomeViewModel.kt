package com.watermelon.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import com.watermelon.domain.repository.MusicCatalogRepository
import com.watermelon.domain.repository.UserActionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicCatalogRepository: MusicCatalogRepository,
    private val userActionsRepository: UserActionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val recentlyPlayedDeferred = async { userActionsRepository.getRecentlyPlayed().first() }
            val favoritesDeferred = async { userActionsRepository.getFavorites().first() }
            val trendingDeferred = async { musicCatalogRepository.getTrendingMusic().first() }
            val playlistsDeferred = async { musicCatalogRepository.getRecommendedPlaylists().first() }

            val recentlyPlayed = recentlyPlayedDeferred.await()
            val favorites = favoritesDeferred.await()
            val trending = trendingDeferred.await()
            val playlists = playlistsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    recentlyPlayed = recentlyPlayed,
                    favorites = favorites,
                    trendingMusic = trending,
                    recommendedPlaylists = playlists
                )
            }
        }
    }
}

data class HomeUiState(
    val welcomeMessage: String = "Welcome back",
    val recentlyPlayed: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val trendingMusic: List<Song> = emptyList(),
    val recommendedPlaylists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false
)
