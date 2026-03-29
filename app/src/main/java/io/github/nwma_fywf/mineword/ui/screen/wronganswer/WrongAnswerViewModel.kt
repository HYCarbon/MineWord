package io.github.nwma_fywf.mineword.ui.screen.wronganswer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WrongAnswer
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class WrongAnswerWithWord(
    val wrongAnswer: WrongAnswer,
    val word: Word?
)

class WrongAnswerViewModel(private val repository: WordRepository) : ViewModel() {

    private val _wrongAnswers = MutableStateFlow<List<WrongAnswer>>(emptyList())
    private val _words = MutableStateFlow<Map<Long, Word>>(emptyMap())

    private val _wrongAnswersWithWords = MutableStateFlow<List<WrongAnswerWithWord>>(emptyList())
    val wrongAnswersWithWords: StateFlow<List<WrongAnswerWithWord>> = _wrongAnswersWithWords

    init {
        viewModelScope.launch {
            repository.getAllWrongAnswers().collect { answers ->
                _wrongAnswers.value = answers
                answers.forEach { answer ->
                    if (_words.value[answer.wordId] == null) {
                        viewModelScope.launch {
                            val word = repository.getWordById(answer.wordId)
                            word?.let {
                                _words.value = _words.value + (answer.wordId to it)
                            }
                        }
                    }
                }
                updateCombinedList()
            }
        }
        viewModelScope.launch {
            _words.collect {
                updateCombinedList()
            }
        }
    }

    private fun updateCombinedList() {
        val answers = _wrongAnswers.value
        val words = _words.value
        _wrongAnswersWithWords.value = answers.map { answer ->
            WrongAnswerWithWord(
                wrongAnswer = answer,
                word = words[answer.wordId]
            )
        }
    }

    fun deleteWrongAnswer(id: Long) {
        viewModelScope.launch {
            repository.deleteWrongAnswerById(id)
        }
    }

    fun clearAllWrongAnswers() {
        viewModelScope.launch {
            repository.clearAllWrongAnswers()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WrongAnswerViewModel(repository) as T
                }
            }
        }
    }
}
