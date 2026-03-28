package io.github.nwma_fywf.mineword.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word

data class MeaningEntry(
    val partOfSpeech: String = "",
    val definition: String = ""
)

private val COMMON_PARTS_OF_SPEECH = listOf("动词", "名词", "形容词", "副词", "介词", "连词", "代词", "感叹词")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDialog(
    onDismiss: () -> Unit,
    onConfirm: (word: String, meanings: List<MeaningEntry>, tags: String) -> Unit,
    existingWord: Word? = null,
    existingMeanings: List<Meaning> = emptyList(),
    existingTags: List<String> = emptyList(),
    duplicateWarning: String? = null,
) {
    val isEditing = existingWord != null
    var word by remember { mutableStateOf(existingWord?.word ?: "") }
    val meaningEntries = remember { mutableStateListOf<MeaningEntry>() }
    var tags by remember { mutableStateOf(TextFieldValue(existingWord?.tags ?: "")) }
    var meaningsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(existingMeanings.hashCode()) {
        if (existingMeanings.isNotEmpty() && !meaningsLoaded) {
            meaningEntries.clear()
            meaningEntries.addAll(existingMeanings.map { MeaningEntry(it.partOfSpeech ?: "", it.definition) })
            meaningsLoaded = true
        }
        // Ensure at least one entry
        if (meaningEntries.isEmpty()) {
            meaningEntries.add(MeaningEntry())
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val canSave = word.isNotBlank() && meaningEntries.any { it.definition.isNotBlank() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = if (isEditing) "编辑单词" else "添加单词",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("单词") },
                singleLine = true,
                isError = duplicateWarning != null,
            )
            if (duplicateWarning != null) {
                Text(
                    text = duplicateWarning,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "释义",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))

            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                itemsIndexed(meaningEntries) { index, entry ->
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { meaningEntries.add(MeaningEntry()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加释义")
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
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    val validMeanings = meaningEntries.filter { it.definition.isNotBlank() }
                    onConfirm(word.trim(), validMeanings, tags.text.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
            ) {
                Text("保存")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeaningEntryRow(
    entry: MeaningEntry,
    onEntryChange: (MeaningEntry) -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean,
) {
    var posExpanded by remember { mutableStateOf(false) }
    var posInput by remember { mutableStateOf(entry.partOfSpeech) }
    var showCustomInput by remember { mutableStateOf(entry.partOfSpeech.isNotEmpty() && entry.partOfSpeech !in COMMON_PARTS_OF_SPEECH) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Part of speech dropdown
        ExposedDropdownMenuBox(
            expanded = posExpanded,
            onExpandedChange = { posExpanded = it },
            modifier = Modifier.width(90.dp),
        ) {
            OutlinedTextField(
                value = if (showCustomInput) posInput else entry.partOfSpeech,
                onValueChange = {
                    posInput = it
                    showCustomInput = true
                    onEntryChange(entry.copy(partOfSpeech = it))
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                label = { Text("词性", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posExpanded) },
                readOnly = !showCustomInput,
            )
            ExposedDropdownMenu(
                expanded = posExpanded,
                onDismissRequest = { posExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("无") },
                    onClick = {
                        posExpanded = false
                        showCustomInput = false
                        posInput = ""
                        onEntryChange(entry.copy(partOfSpeech = ""))
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
                COMMON_PARTS_OF_SPEECH.forEach { pos ->
                    DropdownMenuItem(
                        text = { Text(pos) },
                        onClick = {
                            posExpanded = false
                            showCustomInput = false
                            posInput = pos
                            onEntryChange(entry.copy(partOfSpeech = pos))
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
                DropdownMenuItem(
                    text = { Text("自定义...") },
                    onClick = {
                        posExpanded = false
                        showCustomInput = true
                        posInput = entry.partOfSpeech
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Definition input
        OutlinedTextField(
            value = entry.definition,
            onValueChange = { onEntryChange(entry.copy(definition = it)) },
            modifier = Modifier.weight(1f),
            label = { Text("释义", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
        )

        if (showDelete) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "删除")
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    word: String,
    meaningCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = {
            Text(
                if (meaningCount > 0) {
                    "确定要删除单词 \"$word\" 吗？将同时删除 $meaningCount 条释义。"
                } else {
                    "确定要删除单词 \"$word\" 吗？"
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
