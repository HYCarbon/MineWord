package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.component.ConfirmDeleteDialog
import io.github.nwma_fywf.mineword.ui.component.WordCard
import io.github.nwma_fywf.mineword.ui.component.WordDialog
import io.github.nwma_fywf.mineword.ui.component.ExampleSentenceEntry
import io.github.nwma_fywf.mineword.ui.component.MeaningEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel,
    onNavigateToAddWord: () -> Unit,
) {
    val words by viewModel.words.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val existingTags by viewModel.existingTags.collectAsState()
    var editingWord by remember { mutableStateOf<Word?>(null) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }
    var deleteMeaningCount by remember { mutableIntStateOf(0) }
    var editDuplicateWarning by remember { mutableStateOf<String?>(null) }
    var editingMeanings by remember { mutableStateOf<List<Meaning>>(emptyList()) }
    var editingExampleSentences by remember { mutableStateOf<List<ExampleSentence>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editingWord) {
        if (editingWord != null) {
            editingMeanings = viewModel.getMeanings(editingWord!!.id)
                .first()
            editingExampleSentences = viewModel.getExampleSentences(editingWord!!.id)
                .first()
        } else {
            editingMeanings = emptyList()
            editingExampleSentences = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("单词列表") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddWord) {
                Icon(Icons.Filled.Add, contentDescription = "添加单词")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索单词、释义或标签") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Filled.Close, contentDescription = "清除搜索")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                )
            }

            if (words.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "未找到匹配的单词" else "还没有单词，点击右下角 + 添加",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(
                    items = words,
                    key = { it.id },
                ) { word ->
                    val meanings by viewModel.getMeanings(word.id).collectAsState(initial = emptyList())
                    val exampleSentences by viewModel.getExampleSentences(word.id).collectAsState(initial = emptyList())
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            wordToDelete = word
                            deleteMeaningCount = meanings.size
                            dismissState.reset()
                        }
                    }
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        enableDismissFromStartToEnd = false,
                    ) {
                        WordCard(
                            word = word,
                            meanings = meanings,
                            exampleSentences = exampleSentences,
                            onClick = { editingWord = word },
                        )
                    }
                }
            }
        }
    }

    editingWord?.let { word ->
        WordDialog(
            onDismiss = { editingWord = null; editDuplicateWarning = null },
            onConfirm = { newWord, phoneticUK, phoneticUS, meaningEntries, exampleSentenceEntries, newTags ->
                scope.launch {
                    if (viewModel.checkDuplicate(newWord, excludeId = word.id)) {
                        editDuplicateWarning = "单词 \"$newWord\" 已存在"
                    } else {
                        val meanings = meaningEntries.mapIndexed { index, entry ->
                            Meaning(
                                wordId = word.id,
                                partOfSpeech = entry.partOfSpeech.ifBlank { null },
                                definition = entry.definition,
                                order = index
                            )
                        }
                        viewModel.updateWord(word, newWord, phoneticUK, phoneticUS, meanings, exampleSentenceEntries, newTags)
                        editingWord = null
                        editDuplicateWarning = null
                    }
                }
            },
            existingWord = word,
            existingMeanings = editingMeanings,
            existingExampleSentences = editingExampleSentences,
            existingTags = existingTags,
            duplicateWarning = editDuplicateWarning,
        )
    }

    wordToDelete?.let { word ->
        ConfirmDeleteDialog(
            word = word.word,
            meaningCount = deleteMeaningCount,
            onConfirm = {
                viewModel.deleteWord(word)
                wordToDelete = null
            },
            onDismiss = { wordToDelete = null },
        )
    }
}
