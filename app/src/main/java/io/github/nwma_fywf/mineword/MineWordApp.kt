package io.github.nwma_fywf.mineword

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.nwma_fywf.mineword.ui.navigation.NavGraph
import io.github.nwma_fywf.mineword.ui.navigation.Screen
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizScreen
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.SettingsScreen
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListScreen
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListViewModel
import io.github.nwma_fywf.mineword.ui.theme.MineWordTheme

@Composable
fun MineWordApp() {
    MineWordTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val bottomNavItems = listOf(
            Triple(Screen.WordList, "单词列表", Icons.AutoMirrored.Outlined.List),
            Triple(Screen.Quiz, "测验", Icons.Filled.Star),
            Triple(Screen.Settings, "设置", Icons.Outlined.Settings),
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                wordListScreen = {
                    val viewModel: WordListViewModel = viewModel(
                        factory = WordListViewModel.provideFactory(
                            (navController.context.applicationContext as MineWordApplication).repository
                        )
                    )
                    WordListScreen(
                        viewModel = viewModel,
                    )
                },
                quizScreen = {
                    val viewModel: QuizViewModel = viewModel(
                        factory = QuizViewModel.provideFactory(
                            (navController.context.applicationContext as MineWordApplication).repository
                        )
                    )
                    QuizScreen(
                        viewModel = viewModel,
                    )
                },
                settingsScreen = {
                    SettingsScreen()
                }
            )
        }
    }
}
