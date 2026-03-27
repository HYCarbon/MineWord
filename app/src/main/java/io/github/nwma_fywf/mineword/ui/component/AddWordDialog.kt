package io.github.nwma_fywf.mineword.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.Word

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDialog(
    onDismiss: () -> Unit,
    onConfirm: (word: String, definition: String, tags: String) -> Unit,
    existingWord: Word? = null,
    existingTags: List<String> = emptyList(),
) {
    val isEditing = existingWord != null
    var word by remember { mutableStateOf(existingWord?.word ?: "") }
    var definition by remember { mutableStateOf(existingWord?.definition ?: "") }
    var tags by remember { mutableStateOf(TextFieldValue(existingWord?.tags ?: "")) }

    val sheetState = rememberModalBottomSheetState()

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
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = definition,
                onValueChange = { definition = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("释义") },
                minLines = 2,
            )
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
            Button(
                onClick = {
                    if (word.isNotBlank() && definition.isNotBlank()) {
                        onConfirm(word.trim(), definition.trim(), tags.text.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = word.isNotBlank() && definition.isNotBlank(),
            ) {
                Text("保存")
            }
        }
    }
}
