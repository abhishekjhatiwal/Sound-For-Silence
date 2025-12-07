package com.example.soundforsilence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soundforsilence.presentation.navigation.AppNavGraph
import com.example.soundforsilence.ui.theme.SoundForSilenceTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.soundforsilence.core.AppLanguage
import com.example.soundforsilence.presentation.navigation.Screen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            SoundForSilenceTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 🌐 Language state (English / Hindi)
                var currentLanguage by rememberSaveable { mutableStateOf(AppLanguage.ENGLISH) }

                Scaffold(
                    bottomBar = {
                        // Show bottom bar only on these routes
                        if (currentRoute in listOf(
                                Screen.Home.route,
                                Screen.Progress.route,
                                Screen.Settings.route
                            )
                        ) {
                            BottomNavigationBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }
                ) { padding ->
                    AppNavGraph(
                        navController = navController,
                        padding = padding,
                        isDarkTheme = isDarkTheme,
                        onThemeChanged = { isDarkTheme = it },
                        currentLanguage = currentLanguage,
                        onLanguageChanged = { newLang ->
                            currentLanguage = newLang
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Progress.route,
            onClick = {
                navController.navigate(Screen.Progress.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Progress") },
            label = { Text("Progress") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

