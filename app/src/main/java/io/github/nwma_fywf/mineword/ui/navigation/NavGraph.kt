package io.github.nwma_fywf.mineword.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    wordListScreen: @Composable () -> Unit,
    addWordScreen: @Composable () -> Unit,
    editWordScreen: @Composable (Long) -> Unit,
    quizScreen: @Composable () -> Unit,
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
        composable(Screen.AddWord.route) {
            addWordScreen()
        }
        composable(
            route = Screen.EditWord.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            editWordScreen(wordId)
        }
        composable(Screen.Quiz.route) {
            quizScreen()
        }
        composable(Screen.Settings.route) {
            settingsScreen()
        }
    }
}
