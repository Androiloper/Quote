package com.example.quotex.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quotex.ui.chapter.ChapterScreen
import com.example.quotex.ui.chapter.ChapterViewModel
import com.example.quotex.ui.main.MainScreen
import com.example.quotex.ui.main.MainViewModel
import com.example.quotex.ui.promises.*
import com.example.quotex.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

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

    // Screen to display promise titles (categories)
    data object PromiseTitles : Screen {
        override val route = "promise_titles"
    }

    // Screen to display promises for a selected title
    data object PromiseDetail : Screen {
        const val titleArg = "title"
        override val route = "promise_detail/{$titleArg}"

        fun createRoute(title: String): String {
            val encodedTitle = Uri.encode(title)
            return "promise_detail/$encodedTitle"
        }
    }

    data object Chapter : Screen {
        override val route = "chapter/{chapterNumber}"

        fun createRoute(chapterNumber: Int): String {
            return "chapter/$chapterNumber"
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    // Create a separate PromisesViewModel instance for the Promises screens
    val promisesViewModel: PromisesViewModel = viewModel()

    // Create ChapterViewModel for the Chapter screen
    val chapterViewModel: ChapterViewModel = viewModel()

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
                    // Navigate to PromiseTitles (categories) screen
                    navController.navigate(Screen.PromiseTitles.route)
                },
                onQuoteClick = { chapterNumber ->
                    navController.navigate(Screen.Chapter.createRoute(chapterNumber))
                },
                showSnackbar = { message ->
                    // Implementation if needed
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

        // Original Promises screen for backward compatibility
        composable(Screen.Promises.route) {
            PromisesScreen(
                viewModel = promisesViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Add Categories (PromiseTitles) screen
        composable(Screen.PromiseTitles.route) {
            PromiseTitlesScreen(
                viewModel = promisesViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onTitleClick = { title ->
                    // Navigate to detail screen with the title
                    navController.navigate(Screen.PromiseDetail.createRoute(title))
                }
            )
        }

        // Add PromiseDetail screen to show promises in a category
        composable(
            route = Screen.PromiseDetail.route,
            arguments = listOf(
                navArgument(Screen.PromiseDetail.titleArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedTitle = backStackEntry.arguments?.getString(Screen.PromiseDetail.titleArg) ?: ""
            val title = URLDecoder.decode(encodedTitle, "UTF-8")

            // Set the selected title in ViewModel
            promisesViewModel.setSelectedTitle(title)

            PromiseDetailScreen(
                viewModel = promisesViewModel,
                title = title,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Chapter.route,
            arguments = listOf(
                navArgument("chapterNumber") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
            ChapterScreen(
                viewModel = chapterViewModel,
                chapterNumber = chapterNumber,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}