package io.github.nwma_fywf.mineword.ui.screen.addword

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.ui.component.MeaningEntry
import io.github.nwma_fywf.mineword.ui.component.MeaningEntryRow
import kotlinx.coroutines.launch

data class ExampleSentenceEntry(
    val sentence: String = "",
    val translation: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel,
    onNavigateBack: () -> Unit,
) {
    val existingTags by viewModel.existingTags.collectAsState()
    val scope = rememberCoroutineScope()

    var word by remember { mutableStateOf("") }
    var phoneticUK by remember { mutableStateOf("") }
    var phoneticUS by remember { mutableStateOf("") }
    val meaningEntries = remember { mutableStateListOf(MeaningEntry()) }
    val exampleSentences = remember { mutableStateListOf(ExampleSentenceEntry()) }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var duplicateWarning by remember { mutableStateOf<String?>(null) }

    val canSave = word.isNotBlank() && meaningEntries.any { it.definition.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加单词") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (viewModel.checkDuplicate(word.trim())) {
                                    duplicateWarning = "单词 \"${word.trim()}\" 已存在"
                                } else {
                                    val validMeanings = meaningEntries.filter { it.definition.isNotBlank() }
                                    val meanings = validMeanings.mapIndexed { index, entry ->
                                        Meaning(
                                            wordId = 0,
                                            partOfSpeech = entry.partOfSpeech.ifBlank { null },
                                            definition = entry.definition,
                                            order = index
                                        )
                                    }
                                    val validExamples = exampleSentences.filter { it.sentence.isNotBlank() }
                                    val examples = validExamples.mapIndexed { index, entry ->
                                        ExampleSentence(
                                            wordId = 0,
                                            sentence = entry.sentence,
                                            translation = entry.translation.ifBlank { null },
                                            order = index
                                        )
                                    }
                                    viewModel.insertWord(
                                        word = word.trim(),
                                        phoneticUK = phoneticUK.ifBlank { null },
                                        phoneticUS = phoneticUS.ifBlank { null },
                                        meanings = meanings,
                                        exampleSentences = examples,
                                        tags = tags.text.trim(),
                                        onComplete = onNavigateBack
                                    )
                                }
                            }
                        },
                        enabled = canSave,
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            OutlinedTextField(
                value = word,
                onValueChange = {
                    word = it
                    duplicateWarning = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("单词") },
                singleLine = true,
                isError = duplicateWarning != null,
            )
            if (duplicateWarning != null) {
                Text(
                    text = duplicateWarning!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = phoneticUK,
                    onValueChange = { phoneticUK = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("英式音标") },
                    singleLine = true,
                    placeholder = { Text("/brɪtɪʃ/") },
                )
                OutlinedTextField(
                    value = phoneticUS,
                    onValueChange = { phoneticUS = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("美式音标") },
                    singleLine = true,
                    placeholder = { Text("/ˈæmerɪkən/") },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "释义",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))

            meaningEntries.forEachIndexed { index, entry ->
                MeaningEntryRow(
                    entry = entry,
                    onEntryChange = { newEntry ->
                        meaningEntries[index] = newEntry
                    },
                    onDelete = {
                        if (meaningEntries.size > 1) {
                            meaningEntries.removeAt(index)
                        }
                    },
                    showDelete = meaningEntries.size > 1,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { meaningEntries.add(MeaningEntry()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加释义")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "例句",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))

            exampleSentences.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = entry.sentence,
                            onValueChange = { exampleSentences[index] = entry.copy(sentence = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("例句 ${index + 1}", style = MaterialTheme.typography.bodySmall) },
                            minLines = 2,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = entry.translation,
                            onValueChange = { exampleSentences[index] = entry.copy(translation = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("翻译", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                        )
                    }
                    if (exampleSentences.size > 1) {
                        IconButton(onClick = { exampleSentences.removeAt(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "删除")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { exampleSentences.add(ExampleSentenceEntry()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加例句")
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标签（逗号分隔）") },
                singleLine = true,
                placeholder = { Text("如: CET-4, 动词") },
            )
            if (existingTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val selectedTags = remember(tags.text) {
                    tags.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                }
                Text(
                    text = "已有标签",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    existingTags.forEach { tag ->
                        val isSelected = tag in selectedTags
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    val currentTags = tags.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                    currentTags.remove(tag)
                                    val newText = currentTags.joinToString(", ")
                                    tags = TextFieldValue(newText, selection = TextRange(newText.length))
                                } else {
                                    val trimmed = tags.text.trimEnd()
                                    val newText = when {
                                        trimmed.isEmpty() -> "$tag, "
                                        trimmed.endsWith(",") -> "$trimmed $tag, "
                                        else -> "$trimmed, $tag, "
                                    }
                                    tags = TextFieldValue(newText, selection = TextRange(newText.length))
                                }
                            },
                            label = { Text(tag) },
                        )
                    }
                }
            }
        }
    }
}
