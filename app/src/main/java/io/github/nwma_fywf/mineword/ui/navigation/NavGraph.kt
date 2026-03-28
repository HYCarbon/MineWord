package io.github.nwma_fywf.mineword.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.animation.ExperimentalAnimationApi

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    wordListScreen: @Composable () -> Unit,
    addWordScreen: @Composable () -> Unit,
    wordDetailScreen: @Composable (Long) -> Unit,
    editWordScreen: @Composable (Long) -> Unit,
    quizScreen: @Composable () -> Unit,
    settingsScreen: @Composable () -> Unit,
) {
    val animationDuration = 200
    val bottomNavRoutes = setOf(
        Screen.WordList.route,
        Screen.Quiz.route,
        Screen.Settings.route
    )

    fun AnimatedContentTransitionScope<NavBackStackEntry>.getSlideDirection(): AnimatedContentTransitionScope.SlideDirection {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        return if (initialRoute in bottomNavRoutes && targetRoute in bottomNavRoutes) {
            val bottomNavOrder = listOf(
                Screen.WordList.route,
                Screen.Quiz.route,
                Screen.Settings.route
            )
            val initialIndex = bottomNavOrder.indexOf(initialRoute)
            val targetIndex = bottomNavOrder.indexOf(targetRoute)

            if (targetIndex > initialIndex) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
        } else if (targetRoute in bottomNavRoutes) {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }
    }

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Screen.WordList.route,
        enterTransition = {
            val direction = getSlideDirection()
            slideIntoContainer(
                towards = direction,
                animationSpec = tween(animationDuration)
            )
        },
        exitTransition = {
            val direction = getSlideDirection()
            slideOutOfContainer(
                towards = direction,
                animationSpec = tween(animationDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationDuration)
            )
        },
    ) {
        composable(Screen.WordList.route) {
            wordListScreen()
        }
        composable(Screen.AddWord.route) {
            addWordScreen()
        }
        composable(
            route = Screen.WordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            wordDetailScreen(wordId)
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
