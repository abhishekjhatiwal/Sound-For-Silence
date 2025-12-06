package com.example.soundforsilence.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.example.soundforsilence.presentation.category.CategoryScreen
import com.example.soundforsilence.presentation.home.HomeScreen
import com.example.soundforsilence.presentation.login.LoginScreen
import com.example.soundforsilence.presentation.progress.ProgressScreen
import com.example.soundforsilence.presentation.setting.SettingsScreen
import com.example.soundforsilence.presentation.video.VideoScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Screen.Category.createRoute(categoryId))
                },
                onProgressClick = {
                    navController.navigate(Screen.Progress.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            CategoryScreen(
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Video.createRoute(videoId))
                }
            )
        }

        composable(
            route = Screen.Video.route,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType }
            )
        ) {
            VideoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
