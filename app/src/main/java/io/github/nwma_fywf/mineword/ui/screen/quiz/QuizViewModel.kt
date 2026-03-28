package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: WordRepository) : ViewModel() {

    private val _allWords = MutableStateFlow<List<Word>>(emptyList())
    val allWords: StateFlow<List<Word>> = _allWords

    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord

    private val _currentMeanings = MutableStateFlow<List<Meaning>>(emptyList())
    val currentMeanings: StateFlow<List<Meaning>> = _currentMeanings

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState

    sealed class QuizState {
        data object Idle : QuizState()
        data object WaitingInput : QuizState()
        data class ExactMatch(val word: Word) : QuizState()
        data class UserJudgment(
            val word: Word,
            val userInput: String,
            val existingMeanings: List<Meaning>
        ) : QuizState()
        data class Correct(val word: Word, val newMeaningAdded: Boolean) : QuizState()
        data class Incorrect(val word: Word, val userInput: String) : QuizState()
    }

    init {
        viewModelScope.launch {
            repository.getAllWords().collect { words ->
                _allWords.value = words
                if (words.isNotEmpty() && _currentWord.value == null) {
                    selectRandomWord()
                }
            }
        }
    }

    fun selectRandomWord() {
        val words = _allWords.value
        if (words.isEmpty()) {
            _currentWord.value = null
            _quizState.value = QuizState.Idle
            return
        }
        val randomWord = words.random()
        _currentWord.value = randomWord
        _userInput.value = ""
        _quizState.value = QuizState.WaitingInput
        loadMeanings(randomWord.id)
    }

    private fun loadMeanings(wordId: Long) {
        viewModelScope.launch {
            repository.getMeaningsByWordId(wordId).collect { meanings ->
                _currentMeanings.value = meanings
            }
        }
    }

    fun onUserInputChanged(input: String) {
        _userInput.value = input
    }

    fun submitAnswer() {
        val word = _currentWord.value ?: return
        val userInput = _userInput.value.trim()
        if (userInput.isEmpty()) return

        val existingMeanings = _currentMeanings.value
        val exactMatch = existingMeanings.any { meaning ->
            meaning.definition.equals(userInput, ignoreCase = true)
        }

        if (exactMatch) {
            _quizState.value = QuizState.ExactMatch(word)
        } else {
            _quizState.value = QuizState.UserJudgment(
                word = word,
                userInput = userInput,
                existingMeanings = existingMeanings
            )
        }
    }

    fun userJudgmentCorrect() {
        val state = _quizState.value
        if (state is QuizState.UserJudgment) {
            val word = state.word
            val newDefinition = state.userInput
            viewModelScope.launch {
                val newMeaning = Meaning(
                    wordId = word.id,
                    definition = newDefinition,
                    order = state.existingMeanings.size
                )
                repository.saveMeanings(word.id, state.existingMeanings + newMeaning)
                _quizState.value = QuizState.Correct(word, newMeaningAdded = true)
            }
        }
    }

    fun userJudgmentIncorrect() {
        val state = _quizState.value
        if (state is QuizState.UserJudgment) {
            _quizState.value = QuizState.Incorrect(state.word, state.userInput)
        }
    }

    fun confirmExactMatch() {
        val state = _quizState.value
        if (state is QuizState.ExactMatch) {
            _quizState.value = QuizState.Correct(state.word, newMeaningAdded = false)
        }
    }

    fun nextWord() {
        selectRandomWord()
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuizViewModel(repository) as T
                }
            }
        }
    }
}