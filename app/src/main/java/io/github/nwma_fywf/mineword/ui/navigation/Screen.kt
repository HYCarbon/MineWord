package io.github.nwma_fywf.mineword.ui.navigation

sealed class Screen(val route: String) {
    data object WordList : Screen("word_list")
    data object AddWord : Screen("add_word")
    data object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: Long) = "word_detail/$wordId"
    }
    data object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Long) = "edit_word/$wordId"
    }
    data object AddPhrase : Screen("add_phrase")
    data object PhraseDetail : Screen("phrase_detail/{phraseId}") {
        fun createRoute(phraseId: Long) = "phrase_detail/$phraseId"
    }
    data object EditPhrase : Screen("edit_phrase/{phraseId}") {
        fun createRoute(phraseId: Long) = "edit_phrase/$phraseId"
    }
    data object QuizMode : Screen("quiz_mode")
    data object QuizPlay : Screen("quiz_play/{mode}") {
        fun createRoute(mode: String) = "quiz_play/$mode"
    }
    data object Settings : Screen("settings")
}
