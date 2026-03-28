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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.ui.navigation.NavGraph
import io.github.nwma_fywf.mineword.ui.navigation.Screen
import io.github.nwma_fywf.mineword.ui.screen.addword.AddWordScreen
import io.github.nwma_fywf.mineword.ui.screen.addword.AddWordViewModel
import io.github.nwma_fywf.mineword.ui.screen.editword.EditWordScreen
import io.github.nwma_fywf.mineword.ui.screen.editword.EditWordViewModel
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizScreen
import io.github.nwma_fywf.mineword.ui.screen.worddetail.WordDetailScreen
import io.github.nwma_fywf.mineword.ui.screen.worddetail.WordDetailViewModel
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel
import io.github.nwma_fywf.mineword.ui.screen.settings.SettingsScreen
import io.github.nwma_fywf.mineword.ui.screen.settings.SettingsViewModel
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListScreen
import io.github.nwma_fywf.mineword.ui.screen.wordlist.WordListViewModel
import io.github.nwma_fywf.mineword.ui.theme.MineWordTheme

@Composable
fun MineWordApp() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as MineWordApplication
    val themeMode by application.themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val useDynamicColor by application.themePreferences.useDynamicColor.collectAsState(initial = true)
    val fontStyle by application.themePreferences.fontStyle.collectAsState(initial = FontStyle.DEFAULT)
    val customFontPath by application.themePreferences.customFontPath.collectAsState(initial = null)

    MineWordTheme(themeMode = themeMode, useDynamicColor = useDynamicColor, fontStyle = fontStyle, customFontPath = customFontPath) {
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
                        onNavigateToAddWord = {
                            navController.navigate(Screen.AddWord.route)
                        },
                        onNavigateToWordDetail = { wordId ->
                            navController.navigate(Screen.WordDetail.createRoute(wordId))
                        },
                    )
                },
                wordDetailScreen = { wordId ->
                    val viewModel: WordDetailViewModel = viewModel(
                        factory = WordDetailViewModel.provideFactory(
                            (navController.context.applicationContext as MineWordApplication).repository
                        )
                    )
                    WordDetailScreen(
                        viewModel = viewModel,
                        wordId = wordId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { id ->
                            navController.navigate(Screen.EditWord.createRoute(id))
                        },
                    )
                },
                addWordScreen = {
                    val viewModel: AddWordViewModel = viewModel(
                        factory = AddWordViewModel.provideFactory(
                            (navController.context.applicationContext as MineWordApplication).repository
                        )
                    )
                    AddWordScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                    )
                },
                editWordScreen = { wordId ->
                    val viewModel: EditWordViewModel = viewModel(
                        factory = EditWordViewModel.provideFactory(
                            (navController.context.applicationContext as MineWordApplication).repository
                        )
                    )
                    EditWordScreen(
                        viewModel = viewModel,
                        wordId = wordId,
                        onNavigateBack = { navController.popBackStack() },
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
                    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as MineWordApplication
                    val settingsVm: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.provideFactory(
                            application.themePreferences,
                            application.repository,
                            navController.context
                        )
                    )
                    SettingsScreen(viewModel = settingsVm)
                }
            )
        }
    }
}
