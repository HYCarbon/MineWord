package io.github.nwma_fywf.mineword.ui.screen.addword

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddWordViewModel(private val repository: WordRepository) : ViewModel() {

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

    suspend fun checkDuplicate(word: String): Boolean {
        return repository.getWordByWord(word) != null
    }

    fun insertWord(
        word: String,
        phoneticUK: String? = null,
        phoneticUS: String? = null,
        meanings: List<Meaning>,
        exampleSentences: List<ExampleSentence> = emptyList(),
        tags: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.insertWord(
                Word(
                    word = word,
                    phoneticUK = phoneticUK,
                    phoneticUS = phoneticUS,
                    tags = tags,
                )
            )
            repository.saveMeanings(id, meanings)
            if (exampleSentences.isNotEmpty()) {
                repository.saveExampleSentences(id, exampleSentences)
            }
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddWordViewModel(repository) as T
                }
            }
        }
    }
}
