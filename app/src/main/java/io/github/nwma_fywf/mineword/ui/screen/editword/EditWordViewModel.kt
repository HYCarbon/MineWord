package io.github.nwma_fywf.mineword.ui.screen.editword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditWordData(
    val word: Word,
    val meanings: List<Meaning>,
    val exampleSentences: List<ExampleSentence>
)

class EditWordViewModel(private val repository: WordRepository) : ViewModel() {

    private val _wordData = MutableStateFlow<EditWordData?>(null)
    val wordData: StateFlow<EditWordData?> = _wordData

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

    val existingWords: StateFlow<List<Word>>

    init {
        val flow = MutableStateFlow<List<Word>>(emptyList())
        existingWords = flow
        viewModelScope.launch {
            flow.value = repository.getAllWordsList()
        }
    }

    fun loadWord(wordId: Long) {
        viewModelScope.launch {
            val word = repository.getWordById(wordId)
            if (word != null) {
                val meanings = repository.getMeaningsByWordId(wordId).first()
                val exampleSentences = repository.getExampleSentencesByWordId(wordId).first()
                _wordData.value = EditWordData(word, meanings, exampleSentences)
            }
        }
    }

    suspend fun checkDuplicate(word: String, excludeId: Long): Boolean {
        val existing = repository.getWordByWord(word)
        return existing != null && existing.id != excludeId
    }

    fun updateWord(
        word: Word,
        newWord: String,
        phoneticUK: String?,
        phoneticUS: String?,
        meanings: List<Meaning>,
        exampleSentences: List<ExampleSentence>,
        tags: String,
        synonyms: String,
        antonyms: String,
        phrases: String,
        derivatives: String,
        frequencyLevel: String,
        confusion: String,
        personalNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val updatedWord = word.copy(
                word = newWord,
                phoneticUK = phoneticUK,
                phoneticUS = phoneticUS,
                tags = tags,
                synonyms = synonyms,
                antonyms = antonyms,
                phrases = phrases,
                derivatives = derivatives,
                frequencyLevel = frequencyLevel,
                confusion = confusion,
                personalNotes = personalNotes
            )
            repository.updateWord(updatedWord)
            repository.saveMeanings(word.id, meanings)
            if (exampleSentences.isNotEmpty()) {
                repository.saveExampleSentences(word.id, exampleSentences)
            }
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditWordViewModel(repository) as T
                }
            }
        }
    }
}
