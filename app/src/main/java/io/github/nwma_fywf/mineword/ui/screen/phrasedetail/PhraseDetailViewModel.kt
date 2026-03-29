package io.github.nwma_fywf.mineword.ui.screen.phrasedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Phrase
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhraseDetailViewModel(private val repository: WordRepository) : ViewModel() {

    private val _phrase = MutableStateFlow<Phrase?>(null)
    val phrase: StateFlow<Phrase?> = _phrase

    fun loadPhrase(phraseId: Long) {
        viewModelScope.launch {
            _phrase.value = repository.getPhraseById(phraseId)
        }
    }

    fun deletePhrase(onComplete: () -> Unit) {
        viewModelScope.launch {
            _phrase.value?.let { repository.deletePhrase(it) }
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PhraseDetailViewModel(repository) as T
                }
            }
        }
    }
}
