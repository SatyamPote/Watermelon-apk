package com.watermelon.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.data.local.dao.CachedSongDao
import com.watermelon.domain.model.User
import com.watermelon.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cachedSongDao: CachedSongDao
) : ViewModel() {

    val user: StateFlow<User?> = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _cacheCleared = MutableStateFlow(false)
    val cacheCleared: StateFlow<Boolean> = _cacheCleared.asStateFlow()

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            cachedSongDao.clearAll()
            _cacheCleared.value = true
        }
    }

    fun resetCacheFlag() {
        _cacheCleared.value = false
    }
}
