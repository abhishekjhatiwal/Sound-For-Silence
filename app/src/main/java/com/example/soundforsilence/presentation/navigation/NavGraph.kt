package com.example.soundforsilence.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.soundforsilence.presentation.category.CategoryScreen
import com.example.soundforsilence.presentation.createaccount.CreateAccountScreen
import com.example.soundforsilence.presentation.home.HomeScreen
import com.example.soundforsilence.presentation.login.LoginScreen
import com.example.soundforsilence.presentation.profile.child.ChildProfileScreen
import com.example.soundforsilence.presentation.profile.parent.ProfileScreen
import com.example.soundforsilence.presentation.progress.ProgressScreen
import com.example.soundforsilence.presentation.setting.SettingsScreen
import com.example.soundforsilence.presentation.video.VideoScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    padding: PaddingValues,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.padding(padding)
    ) {
        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.CreateAccount.route)
                }
            )
        }

        // CREATE ACCOUNT
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                onBack = { navController.popBackStack() },
                onAccountCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // HOME
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
                },
                onVideosClick = {
                    navController.navigate(Screen.Video.createRoute("someVideoId")) // adjust if needed
                }
            )
        }

        // CATEGORY (videos list for a stage)
        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            CategoryScreen(
                onBack = { navController.popBackStack() },
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Video.createRoute(videoId))
                }
            )
        }

        // VIDEO DETAIL / PLAYER
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

        // PROGRESS
        composable(Screen.Progress.route) {
            ProgressScreen()
        }

        // SETTINGS
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onChildClick = {
                    navController.navigate(Screen.ChildProfile.route)
                },
                isDarkTheme = isDarkTheme,
                onThemeChanged = onThemeChanged
            )
        }

        // PARENT PROFILE
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // CHILD PROFILE
        composable(Screen.ChildProfile.route) {
            ChildProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}








/*
@Composable
fun AppNavGraph(
    navController: NavHostController,
    padding: PaddingValues,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.padding(padding)
    ) {
        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.CreateAccount.route)
                }
            )
        }

        // CREATE ACCOUNT
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                onBack = { navController.popBackStack() },
                onAccountCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // HOME
        composable(Screen.Home.route) { backStackEntry ->
            val childParentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Settings.route)
            }
            val childViewModel: ChildProfileViewModel = hiltViewModel(childParentEntry)
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Screen.Category.createRoute(categoryId))
                },
                onProgressClick = {
                    navController.navigate(Screen.Progress.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onVideosClick = {
                    navController.navigate(Screen.Video.route)
                },
                childViewModel = childViewModel,
            )
        }

        // CATEGORY (videos list for a stage)
        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            CategoryScreen(
                onBack = { navController.popBackStack() },
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Video.createRoute(videoId))
                }
            )
        }

        // VIDEO DETAIL / PLAYER
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

        // PROGRESS
        composable(Screen.Progress.route) {
            ProgressScreen()
        }

        // SETTINGS
        composable(Screen.Settings.route) { backStackEntry ->
            val childViewModel: ChildProfileViewModel = hiltViewModel(backStackEntry)
            SettingsScreen(
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onChildClick = { navController.navigate(Screen.ChildProfile.route) },
                isDarkTheme = isDarkTheme,
                onThemeChanged = onThemeChanged,
                childViewModel = childViewModel
            )
        }

        // PROFILE
        composable(Screen.Profile.route) {   // ✅ use the sealed class route
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Child Profile
        composable(Screen.ChildProfile.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Settings.route)
            }
            val childViewModel: ChildProfileViewModel = hiltViewModel(parentEntry)
            ChildProfileScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = childViewModel
            )
        }

    }
}


 */


