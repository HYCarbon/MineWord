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

data class ShareWordIntent(
    val text: String
)

class WordDetailViewModel(private val repository: WordRepository) : ViewModel() {

    private val _wordDetailData = MutableStateFlow<WordDetailData?>(null)
    val wordDetailData: StateFlow<WordDetailData?> = _wordDetailData

    private val _shareIntent = MutableStateFlow<ShareWordIntent?>(null)
    val shareIntent: StateFlow<ShareWordIntent?> = _shareIntent

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

    fun shareWord() {
        _wordDetailData.value?.let { data ->
            val shareText = buildString {
                appendLine(data.word.word)
                if (!data.word.phoneticUK.isNullOrBlank() || !data.word.phoneticUS.isNullOrBlank()) {
                    append("  ")
                    if (!data.word.phoneticUK.isNullOrBlank()) {
                        append("[英] ${data.word.phoneticUK}")
                    }
                    if (!data.word.phoneticUS.isNullOrBlank()) {
                        append(" [美] ${data.word.phoneticUS}")
                    }
                    appendLine()
                }
                data.meanings.forEach { meaning ->
                    val posPrefix = if (meaning.partOfSpeech != null) "[${meaning.partOfSpeech}] " else ""
                    appendLine("$posPrefix${meaning.definition}")
                }
                if (data.exampleSentences.isNotEmpty()) {
                    appendLine()
                    data.exampleSentences.take(2).forEach { sentence ->
                        appendLine("• ${sentence.sentence}")
                        if (!sentence.translation.isNullOrBlank()) {
                            appendLine("  ${sentence.translation}")
                        }
                    }
                }
                if (data.word.tags.isNotBlank()) {
                    appendLine()
                    appendLine("标签: ${data.word.tags}")
                }
            }
            _shareIntent.value = ShareWordIntent(shareText)
        }
    }

    fun clearShareIntent() {
        _shareIntent.value = null
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
