package io.github.nwma_fywf.mineword.ui.screen.worddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    viewModel: WordDetailViewModel,
    wordId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
) {
    val wordDetailData by viewModel.wordDetailData.collectAsState()

    LaunchedEffect(wordId) {
        viewModel.loadWord(wordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(wordId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { innerPadding ->
        wordDetailData?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = data.word.word,
                    style = MaterialTheme.typography.headlineLarge,
                )

                if (data.word.phoneticUK != null || data.word.phoneticUS != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        data.word.phoneticUK?.let { phonetic ->
                            Text(
                                text = "英 $phonetic",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        data.word.phoneticUS?.let { phonetic ->
                            Text(
                                text = "美 $phonetic",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (data.meanings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "释义",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    data.meanings.sortedBy { it.order }.forEach { meaning ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (meaning.partOfSpeech != null) {
                                Text(
                                    text = meaning.partOfSpeech,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = meaning.definition,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (data.exampleSentences.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "例句",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    data.exampleSentences.sortedBy { it.order }.forEach { example ->
                        Column {
                            Text(
                                text = example.sentence,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (example.translation != null) {
                                Text(
                                    text = example.translation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (!data.word.tags.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        data.word.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) },
                            )
                        }
                    }
                }

                if (!data.word.synonyms.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "同义词",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.word.synonyms,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (!data.word.antonyms.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "反义词",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.word.antonyms,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (!data.word.personalNotes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "个人笔记",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.word.personalNotes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
