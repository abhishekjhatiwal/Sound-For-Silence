package com.example.soundforsilence.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object CreateAccount : Screen("create_account")
    data object Home : Screen("home")
    data object Progress : Screen("progress")
    data object Settings : Screen("settings")
    data object Category : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }

    data object Video : Screen("video/{videoId}") {
        fun createRoute(videoId: String) = "video/$videoId"
    }

    data object Profile : Screen("profile")
}