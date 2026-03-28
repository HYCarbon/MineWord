package io.github.nwma_fywf.mineword.ui.navigation

sealed class Screen(val route: String) {
    data object WordList : Screen("word_list")
    data object AddWord : Screen("add_word")
    data object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Long) = "edit_word/$wordId"
    }
    data object Quiz : Screen("quiz")
    data object Settings : Screen("settings")
}
