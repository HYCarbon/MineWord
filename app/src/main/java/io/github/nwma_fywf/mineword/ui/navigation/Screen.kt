package io.github.nwma_fywf.mineword.ui.navigation

sealed class Screen(val route: String) {
    data object WordList : Screen("word_list")
    data object Quiz : Screen("quiz")
    data object Settings : Screen("settings")
}
