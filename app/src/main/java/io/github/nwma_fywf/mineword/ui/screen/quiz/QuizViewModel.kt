package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WrongAnswer
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: WordRepository) : ViewModel() {

    enum class QuizMode {
        EN_TO_CN,
        CN_TO_EN,
        CHOICE_EN_TO_CN,
        CHOICE_CN_TO_EN
    }

    data class ChoiceOption(
        val id: Int,
        val text: String,
        val isCorrect: Boolean
    )

    private val _quizMode = MutableStateFlow(QuizMode.EN_TO_CN)
    val quizMode: StateFlow<QuizMode> = _quizMode

    private val _allWords = MutableStateFlow<List<Word>>(emptyList())
    val allWords: StateFlow<List<Word>> = _allWords

    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord

    private val _currentMeanings = MutableStateFlow<List<Meaning>>(emptyList())
    val currentMeanings: StateFlow<List<Meaning>> = _currentMeanings

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    private val _allMeaningsMap = MutableStateFlow<Map<Long, List<Meaning>>>(emptyMap())
    private val allMeaningsMap: Map<Long, List<Meaning>> get() = _allMeaningsMap.value

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState

    sealed class QuizState {
        data object Idle : QuizState()
        data object WaitingInput : QuizState()
        data object ExactMatch : QuizState()
        data class UserJudgment(
            val word: Word,
            val userInput: String,
            val existingMeanings: List<Meaning>
        ) : QuizState()
        data object Correct : QuizState()
        data class Incorrect(val correctMeanings: List<Meaning>, val userInput: String) : QuizState()
        data class WaitingChoice(
            val word: Word,
            val options: List<ChoiceOption>,
            val isCorrectAnswered: Boolean? = null
        ) : QuizState()
    }

    init {
        viewModelScope.launch {
            repository.getAllWords().collect { words ->
                _allWords.value = words
                val meaningsMap = mutableMapOf<Long, List<Meaning>>()
                words.forEach { word ->
                    viewModelScope.launch {
                        repository.getMeaningsByWordId(word.id).collect { meanings ->
                            meaningsMap[word.id] = meanings
                            _allMeaningsMap.value = meaningsMap.toMap()
                        }
                    }
                }
                if (words.isNotEmpty() && _currentWord.value == null) {
                    selectRandomWord()
                }
            }
        }
    }

    fun selectRandomWord() {
        val words = _allWords.value
        if (words.isEmpty()) {
            _currentWord.value = null
            _quizState.value = QuizState.Idle
            return
        }
        val randomWord = words.random()
        _currentWord.value = randomWord
        _userInput.value = ""
        loadMeanings(randomWord.id)

        when (_quizMode.value) {
            QuizMode.CHOICE_EN_TO_CN, QuizMode.CHOICE_CN_TO_EN -> {
            }
            else -> {
                _quizState.value = QuizState.WaitingInput
            }
        }
    }

    fun onMeaningsLoaded(meanings: List<Meaning>) {
        _currentMeanings.value = meanings
        if (_quizMode.value == QuizMode.CHOICE_EN_TO_CN || _quizMode.value == QuizMode.CHOICE_CN_TO_EN) {
            generateChoiceOptions()
        }
    }

    private fun generateChoiceOptions() {
        val word = _currentWord.value ?: return
        val allWords = _allWords.value
        if (allWords.size < 4) {
            _quizState.value = QuizState.WaitingInput
            return
        }

        val correctMeaning = _currentMeanings.value.firstOrNull()?.definition

        val options = mutableListOf<ChoiceOption>()
        val usedTexts = mutableSetOf<String>()

        when (_quizMode.value) {
            QuizMode.CHOICE_EN_TO_CN -> {
                if (correctMeaning == null) {
                    _quizState.value = QuizState.WaitingInput
                    return
                }
                options.add(ChoiceOption(id = 0, text = correctMeaning, isCorrect = true))
                usedTexts.add(correctMeaning)

                val otherMeanings = allWords
                    .filter { it.id != word.id }
                    .mapNotNull { w ->
                        _allMeaningsMap.value[w.id]
                    }
                    .flatten()
                    .map { it: Meaning -> it.definition }
                    .filter { it !in usedTexts }
                    .distinct()
                    .shuffled()
                    .take(3)

                otherMeanings.forEachIndexed { index, meaning ->
                    options.add(ChoiceOption(id = index + 1, text = meaning, isCorrect = false))
                    usedTexts.add(meaning)
                }
            }
            QuizMode.CHOICE_CN_TO_EN -> {
                options.add(ChoiceOption(id = 0, text = word.word, isCorrect = true))
                usedTexts.add(word.word)

                val otherWords = allWords
                    .filter { it.id != word.id }
                    .map { it.word }
                    .filter { it !in usedTexts }
                    .shuffled()
                    .take(3)

                otherWords.forEachIndexed { index, w ->
                    options.add(ChoiceOption(id = index + 1, text = w, isCorrect = false))
                    usedTexts.add(w)
                }
            }
            else -> return
        }

        _quizState.value = QuizState.WaitingChoice(word = word, options = options.shuffled())
    }

    private fun loadMeanings(wordId: Long) {
        viewModelScope.launch {
            repository.getMeaningsByWordId(wordId).collect { meanings ->
                onMeaningsLoaded(meanings)
            }
        }
    }

    fun onUserInputChanged(input: String) {
        _userInput.value = input
    }

    fun submitAnswer() {
        val word = _currentWord.value ?: return
        val userInput = _userInput.value.trim()
        if (userInput.isEmpty()) return

        val existingMeanings = _currentMeanings.value
        val isCorrect = when (_quizMode.value) {
            QuizMode.EN_TO_CN -> existingMeanings.any { meaning ->
                meaning.definition.equals(userInput, ignoreCase = true)
            }
            QuizMode.CN_TO_EN -> word.word.equals(userInput, ignoreCase = true)
            else -> false
        }

        if (isCorrect) {
            selectRandomWord()
        } else {
            val correctAnswer = when (_quizMode.value) {
                QuizMode.EN_TO_CN -> existingMeanings.firstOrNull()?.definition ?: ""
                QuizMode.CN_TO_EN -> word.word
                else -> ""
            }
            recordWrongAnswer(word.id, userInput, correctAnswer)
            when (_quizMode.value) {
                QuizMode.EN_TO_CN -> {
                    _quizState.value = QuizState.UserJudgment(
                        word = word,
                        userInput = userInput,
                        existingMeanings = existingMeanings
                    )
                }
                QuizMode.CN_TO_EN -> {
                    _quizState.value = QuizState.Incorrect(
                        correctMeanings = existingMeanings,
                        userInput = userInput
                    )
                }
                else -> {}
            }
        }
    }

    private fun recordWrongAnswer(wordId: Long, userAnswer: String, correctAnswer: String) {
        viewModelScope.launch {
            val wrongAnswer = WrongAnswer(
                wordId = wordId,
                quizMode = _quizMode.value.name,
                userAnswer = userAnswer,
                correctAnswer = correctAnswer
            )
            repository.insertWrongAnswer(wrongAnswer)
        }
    }

    fun userJudgmentCorrect() {
        val state = _quizState.value
        if (state is QuizState.UserJudgment) {
            val word = state.word
            val newDefinition = state.userInput
            viewModelScope.launch {
                val newMeaning = Meaning(
                    wordId = word.id,
                    definition = newDefinition,
                    order = state.existingMeanings.size
                )
                repository.saveMeanings(word.id, state.existingMeanings + newMeaning)
                selectRandomWord()
            }
        }
    }

    fun userJudgmentIncorrect() {
        val state = _quizState.value
        if (state is QuizState.UserJudgment) {
            _quizState.value = QuizState.Incorrect(
                correctMeanings = state.existingMeanings,
                userInput = state.userInput
            )
        }
    }

    fun nextWord() {
        selectRandomWord()
    }

    fun setModeAndStart(mode: QuizMode) {
        _quizMode.value = mode
        _userInput.value = ""
        selectRandomWord()
    }

    fun selectChoiceOption(option: ChoiceOption) {
        val state = _quizState.value
        if (state is QuizViewModel.QuizState.WaitingChoice && state.isCorrectAnswered == null) {
            if (!option.isCorrect) {
                val correctAnswer = when (_quizMode.value) {
                    QuizMode.CHOICE_EN_TO_CN -> _currentMeanings.value.firstOrNull()?.definition ?: ""
                    QuizMode.CHOICE_CN_TO_EN -> _currentWord.value?.word ?: ""
                    else -> ""
                }
                recordWrongAnswer(_currentWord.value?.id ?: 0, option.text, correctAnswer)
            }
            _quizState.value = state.copy(isCorrectAnswered = option.isCorrect)
        }
    }

    fun nextChoiceWord() {
        selectRandomWord()
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuizViewModel(repository) as T
                }
            }
        }
    }
}