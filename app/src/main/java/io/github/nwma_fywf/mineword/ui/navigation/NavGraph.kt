package io.github.nwma_fywf.mineword.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    wordListScreen: @Composable () -> Unit,
    settingsScreen: @Composable () -> Unit,
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Screen.WordList.route
    ) {
        composable(Screen.WordList.route) {
            wordListScreen()
        }
        composable(Screen.Settings.route) {
            settingsScreen()
        }
    }
}
