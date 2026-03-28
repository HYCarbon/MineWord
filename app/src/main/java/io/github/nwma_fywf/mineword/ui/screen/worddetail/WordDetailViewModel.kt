package io.github.nwma_fywf.mineword.ui.screen.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WordDetailData(
    val word: Word,
    val meanings: List<Meaning>,
    val exampleSentences: List<ExampleSentence>
)

class WordDetailViewModel(private val repository: WordRepository) : ViewModel() {

    private val _wordDetailData = MutableStateFlow<WordDetailData?>(null)
    val wordDetailData: StateFlow<WordDetailData?> = _wordDetailData

    fun loadWord(wordId: Long) {
        viewModelScope.launch {
            val word = repository.getWordById(wordId)
            if (word != null) {
                val meanings = repository.getMeaningsByWordId(wordId).first()
                val exampleSentences = repository.getExampleSentencesByWordId(wordId).first()
                _wordDetailData.value = WordDetailData(word, meanings, exampleSentences)
            }
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WordDetailViewModel(repository) as T
                }
            }
        }
    }
}
