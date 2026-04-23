package com.jellyflix.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jellyflix.tv.session.SessionViewModel
import com.jellyflix.tv.ui.HomeScreen
import com.jellyflix.tv.ui.LibraryScreen
import com.jellyflix.tv.ui.LoginScreen
import com.jellyflix.tv.ui.ServerConnectScreen
import com.jellyflix.tv.ui.theme.JellyflixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JellyflixTheme { App() } }
    }
}

object Routes {
    const val CONNECT = "connect"
    const val LOGIN = "login"
    const val HOME = "home"
    const val LIBRARY = "library/{id}"
    fun library(id: String) = "library/$id"
}

@Composable
private fun App(sessionVm: SessionViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val state by sessionVm.state.collectAsState()

    LaunchedEffect(state.stage) {
        val target = when (state.stage) {
            SessionViewModel.Stage.NeedsServer -> Routes.CONNECT
            SessionViewModel.Stage.NeedsLogin -> Routes.LOGIN
            SessionViewModel.Stage.Authenticated -> Routes.HOME
            SessionViewModel.Stage.Loading -> null
        }
        if (target != null && nav.currentDestination?.route != target) {
            nav.navigate(target) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = nav, startDestination = Routes.CONNECT) {
        composable(Routes.CONNECT) { ServerConnectScreen(onConnected = { sessionVm.onServerSet(it) }) }
        composable(Routes.LOGIN) { LoginScreen(onAuthenticated = { sessionVm.refresh() }) }
        composable(Routes.HOME) {
            HomeScreen(onOpenLibrary = { id -> nav.navigate(Routes.library(id)) })
        }
        composable(Routes.LIBRARY) { backStack ->
            val id = backStack.arguments?.getString("id").orEmpty()
            LibraryScreen(libraryId = id, onBack = { nav.popBackStack() })
        }
    }
}
