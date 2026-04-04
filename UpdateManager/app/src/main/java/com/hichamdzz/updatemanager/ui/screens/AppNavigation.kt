package com.hichamdzz.updatemanager.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var loggedIn by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = if (loggedIn) "dashboard" else "login") {
        composable("login") { LoginScreen(onLogin = { loggedIn = true; navController.navigate("dashboard") { popUpTo(0) } }) }
        composable("dashboard") { DashboardScreen() }
    }
}
