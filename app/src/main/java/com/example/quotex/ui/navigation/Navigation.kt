package com.example.quotex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quotex.ui.main.MainScreen
import com.example.quotex.ui.main.MainViewModel
import com.example.quotex.ui.promises.PromisesScreen
import com.example.quotex.ui.promises.PromisesViewModel
import com.example.quotex.ui.settings.SettingsScreen

// Use a proper sealed interface instead of sealed class for better data class support
sealed interface Screen {
    val route: String

    data object Main : Screen {
        override val route = "main"
    }

    data object Settings : Screen {
        override val route = "settings"
    }

    data object Promises : Screen {
        override val route = "promises"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    // Create a separate PromisesViewModel instance for the Promises screen
    val promisesViewModel: PromisesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = mainViewModel,
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onPromisesClick = {
                    navController.navigate(Screen.Promises.route)
                },
                showSnackbar = { _ ->
                    // Implementation if needed, or remove if not used
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = mainViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Promises.route) {
            // Use the proper PromisesViewModel here instead of MainViewModel
            PromisesScreen(
                viewModel = promisesViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}