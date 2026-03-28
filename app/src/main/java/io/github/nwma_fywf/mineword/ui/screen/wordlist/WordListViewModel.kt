package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.ui.component.ExampleSentenceEntry
import io.github.nwma_fywf.mineword.ui.component.MeaningEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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

    val existingTags: StateFlow<List<String>> = repository.getAllTagsRaw()
        .map { rawList ->
            rawList
                .flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    suspend fun checkDuplicate(word: String, excludeId: Long? = null): Boolean {
        val existing = repository.getWordByWord(word)
        return existing != null && existing.id != excludeId
    }

    fun getMeanings(wordId: Long) = repository.getMeaningsByWordId(wordId)

    fun getExampleSentences(wordId: Long) = repository.getExampleSentencesByWordId(wordId)

    suspend fun getMeaningCount(wordId: Long): Int = repository.getMeaningCount(wordId)

    fun insertWord(word: String, meanings: List<Meaning>, tags: String = "") {
        viewModelScope.launch {
            val id = repository.insertWord(
                Word(
                    word = word,
                    tags = tags,
                )
            )
            repository.saveMeanings(id, meanings)
        }
    }

    fun updateWord(
        word: Word,
        newWord: String,
        phoneticUK: String?,
        phoneticUS: String?,
        newMeanings: List<Meaning>,
        newExampleSentences: List<ExampleSentenceEntry>,
        newTags: String = ""
    ) {
        viewModelScope.launch {
            repository.updateWord(
                word.copy(
                    word = newWord,
                    phoneticUK = phoneticUK,
                    phoneticUS = phoneticUS,
                    tags = newTags
                )
            )
            repository.saveMeanings(word.id, newMeanings)
            val exampleSentences = newExampleSentences.mapIndexed { index, entry ->
                ExampleSentence(
                    wordId = word.id,
                    sentence = entry.sentence,
                    translation = entry.translation.ifBlank { null },
                    order = index
                )
            }
            repository.saveExampleSentences(word.id, exampleSentences)
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
