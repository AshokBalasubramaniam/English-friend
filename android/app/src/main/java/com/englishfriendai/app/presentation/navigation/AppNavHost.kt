package com.englishfriendai.app.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.englishfriendai.app.presentation.auth.LoginScreen
import com.englishfriendai.app.presentation.auth.RegisterScreen
import com.englishfriendai.app.presentation.chat.ChatScreen
import com.englishfriendai.app.presentation.dashboard.DashboardScreen
import com.englishfriendai.app.presentation.history.HistoryScreen
import com.englishfriendai.app.presentation.profile.ProfileScreen
import com.englishfriendai.app.presentation.settings.SettingsScreen
import com.englishfriendai.app.presentation.vocabulary.VocabularyScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Main, bottom-nav-hosted destinations. Each is registered individually (rather than
        // nested inside one MainScaffold composable) so deep links / process restoration can
        // target them directly.
        composable(Screen.Chat.route) { MainScaffold(navController) { ChatScreen() } }
        composable(Screen.History.route) {
            MainScaffold(navController) { HistoryScreen(onConversationClick = { /* TODO: navigate to a conversation detail/replay screen once designed */ }) }
        }
        composable(Screen.Dashboard.route) { MainScaffold(navController) { DashboardScreen() } }
        composable(Screen.Vocabulary.route) { MainScaffold(navController) { VocabularyScreen() } }
        composable(Screen.Settings.route) {
            MainScaffold(navController) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Screen.Profile.route) { MainScaffold(navController) { ProfileScreen() } }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = { AppBottomNavigation(navController) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            content()
        }
    }
}

@Composable
private fun AppBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        Screen.bottomNavScreens.forEach { screen ->
            val icon = when (screen) {
                Screen.Chat -> Icons.Default.Chat
                Screen.History -> Icons.Default.History
                Screen.Dashboard -> Icons.Default.Dashboard
                Screen.Vocabulary -> Icons.Default.MenuBook
                Screen.Settings -> Icons.Default.Settings
                else -> Icons.Default.Chat
            }
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = screen.route) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
private fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.AuthenticatedHome -> onNavigateToHome()
            SplashDestination.RequiresLogin -> onNavigateToLogin()
            SplashDestination.Undetermined -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "English Friend AI", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Warming up your conversation practice…",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Minimal single-screen onboarding placeholder.
 * TODO: replace with the real multi-page onboarding flow (value props, permission priming for
 * mic/notifications) once product/design finalize the sequence.
 */
@Composable
private fun OnboardingScreen(onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to English Friend AI", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Practice spoken English with an AI conversation partner, in English or Tamil.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onFinished) { Text("Get started") }
    }
}
