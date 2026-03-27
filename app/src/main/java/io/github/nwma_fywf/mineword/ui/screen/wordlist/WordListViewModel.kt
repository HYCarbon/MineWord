package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordListViewModel(private val repository: WordRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val words: StateFlow<List<Word>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllWords()
            } else {
                repository.searchWords(query.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun insertWord(word: String, definition: String, tags: String = "") {
        viewModelScope.launch {
            repository.insertWord(
                Word(
                    word = word,
                    definition = definition,
                    tags = tags,
                )
            )
        }
    }

    fun updateWord(word: Word, newWord: String, newDefinition: String, newTags: String = "") {
        viewModelScope.launch {
            repository.updateWord(word.copy(word = newWord, definition = newDefinition, tags = newTags))
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
