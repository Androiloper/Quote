// app/src/main/java/com/example/quotex/ui/navigation/Navigation.kt
// Fixed version with corrected parameters

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
import com.example.quotex.ui.categories.CategoryListScreen
import com.example.quotex.ui.categories.TitleListScreen
import com.example.quotex.ui.categories.SubtitleListScreen
import com.example.quotex.ui.categories.CategoryViewModel
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

    // New route - Page 2 (Categories, Recently, Favorites)
    data object CategoryList : Screen {
        override val route = "categories"
    }

    // New route - Page 3 (Titles within a category)
    data object TitleList : Screen {
        const val categoryArg = "category"
        override val route = "titles/{$categoryArg}"

        fun createRoute(category: String): String {
            val encodedCategory = Uri.encode(category)
            return "titles/$encodedCategory"
        }
    }

    // New route - Page 4 (Subtitles and promises within a title)
    data object SubtitleList : Screen {
        const val categoryArg = "category"
        const val titleArg = "title"
        override val route = "subtitles/{$categoryArg}/{$titleArg}"

        fun createRoute(category: String, title: String): String {
            val encodedCategory = Uri.encode(category)
            val encodedTitle = Uri.encode(title)
            return "subtitles/$encodedCategory/$encodedTitle"
        }
    }

    // Legacy screens from existing code
    data object PromiseTitles : Screen {
        override val route = "promise_titles"
    }

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

    // Create CategoryViewModel for the new hierarchy screens
    val categoryViewModel: CategoryViewModel = viewModel()

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
                    // Navigate to new CategoryList screen (Page 2)
                    navController.navigate(Screen.CategoryList.route)
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

        // New screen - Page 2 (Categories, Recently, Favorites)
        composable(Screen.CategoryList.route) {
            CategoryListScreen(
                viewModel = categoryViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.TitleList.createRoute(category))
                },
                onPromiseClick = { promiseId ->
                    // Navigate to promise detail if needed
                }
            )
        }

        // New screen - Page 3 (Titles within a category)
        composable(
            route = Screen.TitleList.route,
            arguments = listOf(
                navArgument(Screen.TitleList.categoryArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedCategory = backStackEntry.arguments?.getString(Screen.TitleList.categoryArg) ?: ""
            val category = URLDecoder.decode(encodedCategory, "UTF-8")

            TitleListScreen(
                viewModel = categoryViewModel,
                category = category,
                onBackClick = {
                    navController.popBackStack()
                },
                onTitleClick = { title ->
                    navController.navigate(Screen.SubtitleList.createRoute(category, title))
                }
            )
        }

        // New screen - Page 4 (Subtitles and promises within a title)
        composable(
            route = Screen.SubtitleList.route,
            arguments = listOf(
                navArgument(Screen.SubtitleList.categoryArg) {
                    type = NavType.StringType
                },
                navArgument(Screen.SubtitleList.titleArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedCategory = backStackEntry.arguments?.getString(Screen.SubtitleList.categoryArg) ?: ""
            val encodedTitle = backStackEntry.arguments?.getString(Screen.SubtitleList.titleArg) ?: ""
            val category = URLDecoder.decode(encodedCategory, "UTF-8")
            val title = URLDecoder.decode(encodedTitle, "UTF-8")

            SubtitleListScreen(
                viewModel = categoryViewModel,
                category = category,
                title = title,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Legacy screens from existing code
        composable(Screen.Promises.route) {
            PromisesScreen(
                viewModel = promisesViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PromiseTitles.route) {
            PromiseTitlesScreen(
                viewModel = promisesViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onTitleClick = { title ->
                    navController.navigate(Screen.PromiseDetail.createRoute(title))
                }
            )
        }

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