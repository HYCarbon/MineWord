package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val quizState by viewModel.quizState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("测验") })
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
                            userInput = userInput,
                            onUserInputChanged = viewModel::onUserInputChanged,
                            onSubmit = viewModel::submitAnswer,
                        )
                    }
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
                is QuizViewModel.QuizState.Incorrect -> {
                    IncorrectContent(
                        correctMeanings = state.correctMeanings,
                        userInput = state.userInput,
                        onNext = viewModel::nextWord,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordQuizContent(
    word: Word,
    userInput: String,
    onUserInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Text(
        text = word.word,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(32.dp))
    OutlinedTextField(
        value = userInput,
        onValueChange = onUserInputChanged,
        label = { Text("输入词义") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        maxLines = 3,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onSubmit,
        enabled = userInput.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("提交")
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
    correctMeanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
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
        text = "你的释义：$userInput",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "正确答案：",
        style = MaterialTheme.typography.bodyLarge,
    )
    correctMeanings.forEach { meaning ->
        Text(
            text = meaning.definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("下一个单词")
    }
}