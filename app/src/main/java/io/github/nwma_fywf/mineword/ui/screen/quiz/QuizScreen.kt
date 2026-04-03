package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val currentMeanings by viewModel.currentMeanings.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val quizMode by viewModel.quizMode.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (quizMode) {
                            QuizViewModel.QuizMode.EN_TO_CN -> "英译汉"
                            QuizViewModel.QuizMode.CN_TO_EN -> "汉译英"
                            QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> "选择中文"
                            QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> "选择英文"
                            QuizViewModel.QuizMode.REVIEW -> "复习"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val state = quizState) {
                is QuizViewModel.QuizState.Idle -> {
                    Text(
                        text = "没有单词可供测验",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is QuizViewModel.QuizState.WaitingInput -> {
                    currentWord?.let { word ->
                        WordQuizContent(
                            word = word,
                            meanings = currentMeanings,
                            mode = quizMode,
                            userInput = userInput,
                            totalCount = totalCount,
                            onUserInputChanged = viewModel::onUserInputChanged,
                            onSubmit = viewModel::submitAnswer,
                            onFinish = viewModel::finishQuiz,
                        )
                    }
                }
                is QuizViewModel.QuizState.WaitingInputDegraded -> {
                    WordQuizContent(
                        word = state.word,
                        meanings = currentMeanings,
                        mode = state.mode,
                        userInput = userInput,
                        totalCount = totalCount,
                        onUserInputChanged = viewModel::onUserInputChanged,
                        onSubmit = viewModel::submitAnswer,
                        onFinish = viewModel::finishQuiz,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                is QuizViewModel.QuizState.ExactMatch -> {}
                is QuizViewModel.QuizState.Correct -> {}
                is QuizViewModel.QuizState.UserJudgment -> {
                    UserJudgmentContent(
                        word = state.word,
                        userInput = state.userInput,
                        existingMeanings = state.existingMeanings,
                        onCorrect = viewModel::userJudgmentCorrect,
                        onIncorrect = viewModel::userJudgmentIncorrect,
                    )
                }
                is QuizViewModel.QuizState.WaitingChoice -> {
                    ChoiceQuizContent(
                        state = state,
                        mode = quizMode,
                        meanings = currentMeanings,
                        totalCount = totalCount,
                        onSelectOption = viewModel::selectChoiceOption,
                        onNext = viewModel::nextChoiceWord,
                        onFinish = viewModel::finishQuiz,
                    )
                }
                is QuizViewModel.QuizState.Incorrect -> {
                    currentWord?.let { word ->
                        IncorrectContent(
                            word = word,
                            mode = quizMode,
                            userInput = state.userInput,
                            onNext = viewModel::nextWord,
                        )
                    }
                }
                is QuizViewModel.QuizState.Finished -> {
                    QuizFinishedContent(
                        correctCount = state.correctCount,
                        wrongCount = state.wrongCount,
                        onRestart = viewModel::resetQuiz,
                        onExit = onNavigateBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordQuizContent(
    word: Word,
    meanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    mode: QuizViewModel.QuizMode,
    userInput: String,
    totalCount: Int,
    onUserInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onFinish: () -> Unit,
) {
    when (mode) {
        QuizViewModel.QuizMode.EN_TO_CN -> {
            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
        }
        QuizViewModel.QuizMode.CN_TO_EN -> {
            if (meanings.isNotEmpty()) {
                Text(
                    text = meanings.first().definition,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = "无释义",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        else -> {
            Text(
                text = "选择题模式",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "已答 $totalCount 题",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = userInput,
        onValueChange = onUserInputChanged,
        label = {
            Text(
                when (mode) {
                    QuizViewModel.QuizMode.EN_TO_CN -> "输入词义"
                    QuizViewModel.QuizMode.CN_TO_EN -> "输入单词"
                    else -> "输入答案"
                }
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onSubmit,
        enabled = userInput.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("提交")
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
        onClick = onFinish,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("完成测验")
    }
}

@Composable
private fun UserJudgmentContent(
    word: Word,
    userInput: String,
    existingMeanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
) {
    Text(
        text = "释义不完全匹配",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "你的释义：$userInput",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "已记录的释义：",
        style = MaterialTheme.typography.bodyLarge,
    )
    existingMeanings.forEach { meaning ->
        Text(
            text = meaning.definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "请判断你的释义是否正确：",
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onCorrect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("正确，添加新释义")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onIncorrect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("错误")
    }
}

@Composable
private fun IncorrectContent(
    word: Word,
    mode: QuizViewModel.QuizMode,
    userInput: String,
    onNext: () -> Unit,
) {
    Text(
        text = "回答错误",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "你的答案：$userInput",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "正确答案：",
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = word.word,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("下一个单词")
    }
}

@Composable
private fun ChoiceQuizContent(
    state: QuizViewModel.QuizState.WaitingChoice,
    mode: QuizViewModel.QuizMode,
    meanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    totalCount: Int,
    onSelectOption: (QuizViewModel.ChoiceOption) -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Text(
        text = when (mode) {
            QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> state.word.word
            QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> meanings.firstOrNull()?.definition ?: ""
            else -> ""
        },
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "已答 $totalCount 题",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))

    state.options.forEach { option ->
        val buttonColors = when {
            state.isCorrectAnswered == null -> ButtonDefaults.outlinedButtonColors()
            option.isCorrect -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            else -> ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        OutlinedButton(
            onClick = { onSelectOption(option) },
            enabled = state.isCorrectAnswered == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = buttonColors,
        ) {
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    if (state.isCorrectAnswered == null) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("完成测验")
        }
    }

    if (state.isCorrectAnswered != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (state.isCorrectAnswered) "回答正确！" else "回答错误！",
            style = MaterialTheme.typography.titleMedium,
            color = if (state.isCorrectAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("下一个单词")
        }
    }
}

@Composable
private fun QuizFinishedContent(
    correctCount: Int,
    wrongCount: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    val total = correctCount + wrongCount
    val percentage = if (total > 0) (correctCount * 100 / total) else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "测验完成",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "$correctCount / $total",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "正确率 $percentage%",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("再来一组")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("退出")
        }
    }
}
