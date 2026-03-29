package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Phrase
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.component.ConfirmDeleteDialog
import io.github.nwma_fywf.mineword.ui.component.WordCard
import io.github.nwma_fywf.mineword.ui.screen.phraselist.PhraseListViewModel

sealed class VocabularyItem {
    abstract val id: Long
    abstract val createdAt: Long

    data class WordItem(val word: Word, val meanings: List<Meaning>, val exampleSentences: List<ExampleSentence>) : VocabularyItem() {
        override val id: Long = word.id
        override val createdAt: Long = word.createdAt
    }

    data class PhraseItem(val phrase: Phrase) : VocabularyItem() {
        override val id: Long = phrase.id
        override val createdAt: Long = phrase.createdAt
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel,
    phraseListViewModel: PhraseListViewModel,
    onNavigateToAddWord: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToAddPhrase: () -> Unit,
    onNavigateToPhraseDetail: (Long) -> Unit,
) {
    val words by viewModel.words.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var wordToDelete by remember { mutableStateOf<Word?>(null) }
    var deleteMeaningCount by remember { mutableIntStateOf(0) }

    val phrases by phraseListViewModel.phrases.collectAsState()
    var phraseToDelete by remember { mutableStateOf<Phrase?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }

    val allItems = remember(words, phrases) {
        val wordItems = words.map { word ->
            VocabularyItem.WordItem(word, emptyList(), emptyList())
        }
        val phraseItems = phrases.map { phrase ->
            VocabularyItem.PhraseItem(phrase)
        }
        (wordItems + phraseItems).sortedByDescending { it.createdAt }
    }

    val filteredItems = allItems

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词汇") }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showAddMenu = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "添加")
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("添加单词") },
                        onClick = {
                            showAddMenu = false
                            onNavigateToAddWord()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("添加词组") },
                        onClick = {
                            showAddMenu = false
                            onNavigateToAddPhrase()
                        }
                    )
                }
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
                    onValueChange = {
                        viewModel.onSearchQueryChanged(it)
                        phraseListViewModel.onSearchQueryChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索单词或词组...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.clearSearch()
                                phraseListViewModel.clearSearch()
                            }) {
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

            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "未找到匹配的内容" else "还没有内容，点击右下角 + 添加",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(
                    items = filteredItems,
                    key = { item -> "vocab_${item.id}_${item::class.simpleName}" },
                ) { item ->
                    when (item) {
                        is VocabularyItem.WordItem -> {
                            val meanings by viewModel.getMeanings(item.word.id).collectAsState(initial = emptyList())
                            val exampleSentences by viewModel.getExampleSentences(item.word.id).collectAsState(initial = emptyList())
                            val dismissState = rememberSwipeToDismissBoxState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    wordToDelete = item.word
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
                                    word = item.word,
                                    meanings = meanings,
                                    exampleSentences = exampleSentences,
                                    onClick = { onNavigateToWordDetail(item.word.id) },
                                )
                            }
                        }
                        is VocabularyItem.PhraseItem -> {
                            val dismissState = rememberSwipeToDismissBoxState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    phraseToDelete = item.phrase
                                    dismissState.reset()
                                }
                            }
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {},
                                enableDismissFromStartToEnd = false,
                            ) {
                                VocabularyPhraseCard(
                                    phrase = item.phrase,
                                    onClick = { onNavigateToPhraseDetail(item.phrase.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
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

    phraseToDelete?.let { phrase ->
        ConfirmDeleteDialog(
            word = phrase.phrase,
            meaningCount = 0,
            onConfirm = {
                phraseListViewModel.deletePhrase(phrase)
                phraseToDelete = null
            },
            onDismiss = { phraseToDelete = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabularyPhraseCard(
    phrase: Phrase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phrase.phrase,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "词组",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (phrase.meaning.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phrase.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val tags = phrase.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }
        }
    }
}
