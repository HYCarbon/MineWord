package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordListViewModel(private val repository: WordRepository) : ViewModel() {

    val words: StateFlow<List<Word>> = repository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertWord(word: String, definition: String) {
        viewModelScope.launch {
            repository.insertWord(
                Word(
                    word = word,
                    definition = definition,
                )
            )
        }
    }

    fun updateWord(word: Word, newWord: String, newDefinition: String) {
        viewModelScope.launch {
            repository.updateWord(word.copy(word = newWord, definition = newDefinition))
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WordListViewModel(repository) as T
                }
            }
        }
    }
}
