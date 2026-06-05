package com.watermelon.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.watermelon.core.navigation.Routes
import com.watermelon.domain.model.Song
import com.watermelon.feature.auth.ForgotPasswordScreen
import com.watermelon.feature.auth.LoginScreen
import com.watermelon.feature.auth.RegisterScreen
import com.watermelon.feature.home.HomeScreen
import com.watermelon.feature.player.PlayerScreen
import com.watermelon.feature.player.PlayerViewModel
import com.watermelon.feature.search.SearchScreen
import com.watermelon.feature.settings.SettingsScreen
import com.watermelon.feature.settings.SettingsScreen
import kotlinx.coroutines.delay

@Composable
fun WatermelonNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            LaunchedEffect(Unit) {
                delay(1500)
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Watermelon",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        composable(Routes.ONBOARDING) {
            // TODO: OnboardingScreen
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onSongClick = { song: Song ->
                    playerViewModel.loadAndPlay(
                        song.audioUrl ?: "",
                        song.title,
                        song.artistName,
                        song.coverUrl ?: ""
                    )
                    navController.navigate(Routes.PLAYER)
                },
                onPlaylistClick = { navController.navigate(Routes.PLAYLIST_DETAIL) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { song: Song ->
                    playerViewModel.loadAndPlay(
                        song.audioUrl ?: "",
                        song.title,
                        song.artistName,
                        song.coverUrl ?: ""
                    )
                    navController.navigate(Routes.PLAYER)
                }
            )
        }
        composable(Routes.LIBRARY) {
            // TODO: LibraryScreen
        }
        composable(Routes.PLAYLIST_DETAIL) {
            // TODO: PlaylistDetailsScreen
        }
        composable(Routes.CREATE_PLAYLIST) {
            // TODO: CreatePlaylistScreen
        }
        composable(Routes.PLAYER) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = playerViewModel
            )
        }
        composable(Routes.QUEUE) {
            // TODO: QueueScreen
        }
        composable(Routes.DOWNLOADS) {
            // TODO: DownloadsScreen
        }
        composable(Routes.PROFILE) {
            // TODO: ProfileScreen
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogoutComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ABOUT) {
            // TODO: AboutScreen
        }
    }
}
