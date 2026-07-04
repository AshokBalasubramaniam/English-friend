package com.englishfriendai.app.presentation.navigation

/** All top-level destinations in the app, as typed routes instead of raw strings. */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Onboarding : Screen("onboarding")
    data object Chat : Screen("chat")
    data object History : Screen("history")
    data object Dashboard : Screen("dashboard")
    data object Vocabulary : Screen("vocabulary")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")

    companion object {
        /** Destinations shown behind the app's main bottom navigation bar once logged in. */
        val bottomNavScreens = listOf(Chat, History, Dashboard, Vocabulary, Settings)
    }
}
