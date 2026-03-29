package io.github.nwma_fywf.mineword.ui.screen.editphrase

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

class EditPhraseViewModel(private val repository: WordRepository) : ViewModel() {

    private val _phrase = MutableStateFlow<Phrase?>(null)
    val phrase: StateFlow<Phrase?> = _phrase

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

    fun loadPhrase(phraseId: Long) {
        viewModelScope.launch {
            _phrase.value = repository.getPhraseById(phraseId)
        }
    }

    suspend fun checkDuplicate(phrase: String, excludeId: Long): Boolean {
        val existing = repository.getPhraseByPhrase(phrase)
        return existing != null && existing.id != excludeId
    }

    fun updatePhrase(
        phrase: Phrase,
        newPhrase: String,
        meaning: String,
        tags: String,
        personalNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val updatedPhrase = phrase.copy(
                phrase = newPhrase,
                meaning = meaning,
                tags = tags,
                personalNotes = personalNotes
            )
            repository.updatePhrase(updatedPhrase)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditPhraseViewModel(repository) as T
                }
            }
        }
    }
}
