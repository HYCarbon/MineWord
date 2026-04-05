package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizHomeViewModel(private val repository: WordRepository) : ViewModel() {
    private val _dueReviewCount = MutableStateFlow(0)
    val dueReviewCount: StateFlow<Int> = _dueReviewCount

    init {
        viewModelScope.launch {
            _dueReviewCount.value = repository.getDueReviewCount()
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuizHomeViewModel(repository) as T
                }
            }
        }
    }
}
