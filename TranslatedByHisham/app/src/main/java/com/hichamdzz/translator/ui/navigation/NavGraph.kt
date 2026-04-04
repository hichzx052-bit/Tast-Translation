package com.hichamdzz.translator.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hichamdzz.translator.ui.screens.*
import com.hichamdzz.translator.viewmodel.MainViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val VOICE_SELECTION = "voice_selection"
    const val LIVE_MODE = "live_mode"
    const val VIDEO_TRANSLATION = "video_translation"
    const val DEVELOPER = "developer"
    const val FORCE_UPDATE = "force_update"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val onboardingDone by viewModel.onboardingDone.collectAsState()
    val updateRequired by viewModel.updateRequired.collectAsState()

    val startDest = when {
        updateRequired != null -> Routes.FORCE_UPDATE
        !onboardingDone -> Routes.ONBOARDING
        else -> Routes.HOME
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.ONBOARDING) { OnboardingScreen(onComplete = { viewModel.completeOnboarding(); navController.navigate(Routes.HOME) { popUpTo(0) } }) }
        composable(Routes.HOME) { HomeScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.SETTINGS) { SettingsScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.VOICE_SELECTION) { VoiceSelectionScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.LIVE_MODE) { LiveModeScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.VIDEO_TRANSLATION) { VideoTranslationScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.DEVELOPER) { DeveloperScreen(navController = navController, viewModel = viewModel) }
        composable(Routes.FORCE_UPDATE) { ForceUpdateScreen(updateInfo = updateRequired) }
    }
}
