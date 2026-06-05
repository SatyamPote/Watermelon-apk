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
import androidx.navigation.compose.rememberNavController
import com.watermelon.core.navigation.Routes
import com.watermelon.feature.home.HomeScreen
import kotlinx.coroutines.delay

@Composable
fun WatermelonNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
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
                navController.navigate(Routes.HOME) {
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
            // TODO: LoginScreen
        }
        composable(Routes.REGISTER) {
            // TODO: RegisterScreen
        }
        composable(Routes.FORGOT_PASSWORD) {
            // TODO: ForgotPasswordScreen
        }
        composable(Routes.HOME) {
            HomeScreen()
        }
        composable(Routes.SEARCH) {
            // TODO: SearchScreen
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
            // TODO: PlayerScreen
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
            // TODO: SettingsScreen
        }
        composable(Routes.ABOUT) {
            // TODO: AboutScreen
        }
    }
}
