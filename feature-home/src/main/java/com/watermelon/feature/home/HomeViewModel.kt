package com.watermelon.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.domain.model.Playlist
import com.watermelon.domain.model.Song
import com.watermelon.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val recentlyPlayed = musicRepository.getRecentlyPlayed().first()
            val favorites = musicRepository.getFavorites().first()
            val trending = musicRepository.getTrendingMusic().first()
            val playlists = musicRepository.getRecommendedPlaylists().first()

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
