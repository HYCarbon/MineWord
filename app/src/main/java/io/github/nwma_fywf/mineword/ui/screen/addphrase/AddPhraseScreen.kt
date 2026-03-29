package io.github.nwma_fywf.mineword.ui.screen.addphrase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPhraseScreen(
    viewModel: AddPhraseViewModel,
    onNavigateBack: () -> Unit,
) {
    val existingTags by viewModel.existingTags.collectAsState()
    val scope = rememberCoroutineScope()

    val phraseFocusRequester = remember { FocusRequester() }
    val meaningFocusRequester = remember { FocusRequester() }
    val tagsFocusRequester = remember { FocusRequester() }
    val personalNotesFocusRequester = remember { FocusRequester() }

    var phrase by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var personalNotes by remember { mutableStateOf("") }
    var duplicateWarning by remember { mutableStateOf<String?>(null) }

    val canSave = phrase.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加词组") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (viewModel.checkDuplicate(phrase.trim())) {
                                    duplicateWarning = "词组 \"${phrase.trim()}\" 已存在"
                                } else {
                                    viewModel.insertPhrase(
                                        phrase = phrase.trim(),
                                        meaning = meaning.trim(),
                                        tags = tags.text.trim(),
                                        personalNotes = personalNotes.trim(),
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
                value = phrase,
                onValueChange = {
                    phrase = it
                    duplicateWarning = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phraseFocusRequester),
                label = { Text("词组") },
                singleLine = true,
                isError = duplicateWarning != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { meaningFocusRequester.requestFocus() }
                ),
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
            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(meaningFocusRequester),
                label = { Text("释义") },
                placeholder = { Text("输入词组的释义...") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { tagsFocusRequester.requestFocus() }
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(tagsFocusRequester),
                label = { Text("标签（逗号分隔）") },
                singleLine = true,
                placeholder = { Text("如: 动词短语, CET-6") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { personalNotesFocusRequester.requestFocus() }
                ),
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
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = personalNotes,
                onValueChange = { personalNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(personalNotesFocusRequester),
                label = { Text("个人笔记") },
                placeholder = { Text("添加你的个人笔记...") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { }
                ),
            )
        }
    }
}
