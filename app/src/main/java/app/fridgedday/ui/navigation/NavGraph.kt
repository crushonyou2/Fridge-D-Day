package app.fridgedday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.fridgedday.ui.home.HomeScreen
import app.fridgedday.ui.addedit.AddEditScreen
import app.fridgedday.ui.settings.SettingsScreen
import app.fridgedday.ui.statistics.StatisticsScreen

object Destinations {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"

    fun editRoute(id: Long) = "edit/$id"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) {
            HomeScreen(navController)
        }

        composable(Destinations.ADD) {
            AddEditScreen(navController, itemId = null)
        }

        composable(
            route = Destinations.EDIT,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "fridgedday://item/{id}" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")
            AddEditScreen(navController, itemId = id)
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(navController)
        }

        composable(Destinations.STATISTICS) {
            StatisticsScreen(navController)
        }
    }
}
