package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.SortBy
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.ui.component.ExampleSentenceEntry
import io.github.nwma_fywf.mineword.ui.component.MeaningEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WordListViewModel(private val repository: WordRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortBy = MutableStateFlow(SortBy.CREATED_TIME)
    val sortBy: StateFlow<SortBy> = _sortBy

    private val _selectedWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedWordIds: StateFlow<Set<Long>> = _selectedWordIds

    private val _pendingDeletedWords = MutableStateFlow<List<Word>>(emptyList())
    val pendingDeletedWords: StateFlow<List<Word>> = _pendingDeletedWords

    private val _pendingDeletedMeanings = MutableStateFlow<List<Meaning>>(emptyList())
    private val pendingDeletedMeanings: List<Meaning> get() = _pendingDeletedMeanings.value

    private val _pendingDeletedExampleSentences = MutableStateFlow<List<ExampleSentence>>(emptyList())
    private val pendingDeletedExampleSentences: List<ExampleSentence> get() = _pendingDeletedExampleSentences.value

    val isSelectionMode: StateFlow<Boolean> = _selectedWordIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isUndoMode: StateFlow<Boolean> = _pendingDeletedWords.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val words: StateFlow<List<Word>> = kotlinx.coroutines.flow.combine(_searchQuery, _sortBy) { query, sort ->
        Pair(query, sort)
    }.flatMapLatest { (query, sort) ->
        if (query.isBlank()) {
            repository.getAllWordsSorted(sort)
        } else {
            repository.searchWords(query.trim())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setSortBy(sort: SortBy) {
        _sortBy.value = sort
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
            val meanings = repository.getMeaningsByWordId(word.id).first()
            val exampleSentences = repository.getExampleSentencesByWordId(word.id).first()
            repository.deleteWord(word)
            _pendingDeletedWords.value = listOf(word.copy(id = 0))
            _pendingDeletedMeanings.value = meanings.map { it.copy(id = 0, wordId = 0) }
            _pendingDeletedExampleSentences.value = exampleSentences.map { it.copy(id = 0, wordId = 0) }
        }
    }

    fun deleteSelectedWords(words: List<Word>) {
        viewModelScope.launch {
            val selectedWords = words.filter { it.id in _selectedWordIds.value }
            val allMeanings = mutableListOf<Meaning>()
            val allExampleSentences = mutableListOf<ExampleSentence>()
            val wordsToRestore = mutableListOf<Word>()
            
            selectedWords.forEach { word ->
                val meanings = repository.getMeaningsByWordId(word.id).first()
                val exampleSentences = repository.getExampleSentencesByWordId(word.id).first()
                allMeanings.addAll(meanings)
                allExampleSentences.addAll(exampleSentences)
                wordsToRestore.add(word.copy(id = 0))
                repository.deleteWord(word)
            }
            
            _pendingDeletedWords.value = wordsToRestore
            _pendingDeletedMeanings.value = allMeanings.map { it.copy(id = 0, wordId = 0) }
            _pendingDeletedExampleSentences.value = allExampleSentences.map { it.copy(id = 0, wordId = 0) }
            _selectedWordIds.value = emptySet()
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            _pendingDeletedWords.value.forEach { word ->
                val restoredWord = word.copy(id = 0)
                val newId = repository.insertWord(restoredWord)
                val wordMeanings = pendingDeletedMeanings.filter { it.wordId == 0L }
                val wordExamples = pendingDeletedExampleSentences.filter { it.wordId == 0L }
                repository.saveMeanings(newId, wordMeanings.map { it.copy(wordId = newId) })
                repository.saveExampleSentences(newId, wordExamples.map { it.copy(wordId = newId) })
            }
            clearPendingDelete()
        }
    }

    fun clearPendingDelete() {
        _pendingDeletedWords.value = emptyList()
        _pendingDeletedMeanings.value = emptyList()
        _pendingDeletedExampleSentences.value = emptyList()
    }

    fun toggleSelection(wordId: Long) {
        val current = _selectedWordIds.value
        _selectedWordIds.value = if (wordId in current) {
            current - wordId
        } else {
            current + wordId
        }
    }

    fun clearSelection() {
        _selectedWordIds.value = emptySet()
    }

    fun selectAll(wordList: List<Word>) {
        _selectedWordIds.value = wordList.map { it.id }.toSet()
    }

    fun addTagsToSelected(tags: String) {
        viewModelScope.launch {
            val selectedWords = words.value.filter { it.id in _selectedWordIds.value }
            selectedWords.forEach { word ->
                val existingTags = word.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                val newTags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                existingTags.addAll(newTags)
                repository.updateWord(word.copy(tags = existingTags.joinToString(",")))
            }
            _selectedWordIds.value = emptySet()
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
