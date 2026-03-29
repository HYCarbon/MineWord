package io.github.nwma_fywf.mineword.ui.screen.addphrase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Phrase
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddPhraseViewModel(private val repository: WordRepository) : ViewModel() {

    val existingTags: StateFlow<List<String>> = repository.getAllPhraseTagsRaw()
        .map { rawList ->
            rawList
                .flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun checkDuplicate(phrase: String): Boolean {
        return repository.getPhraseByPhrase(phrase) != null
    }

    fun insertPhrase(
        phrase: String,
        meaning: String = "",
        tags: String = "",
        personalNotes: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertPhrase(
                Phrase(
                    phrase = phrase,
                    meaning = meaning,
                    tags = tags,
                    personalNotes = personalNotes
                )
            )
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddPhraseViewModel(repository) as T
                }
            }
        }
    }
}
