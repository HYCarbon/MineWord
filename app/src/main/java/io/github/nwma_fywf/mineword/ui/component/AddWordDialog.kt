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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word

data class MeaningEntry(
    val partOfSpeech: String = "",
    val definition: String = ""
)

@Composable
private fun getCommonPartsOfSpeech(): List<String> {
    val context = LocalContext.current
    return context.resources.getStringArray(R.array.parts_of_speech).toList()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDialog(
    onDismiss: () -> Unit,
    onConfirm: (word: String, phoneticUK: String?, phoneticUS: String?, meanings: List<MeaningEntry>, exampleSentences: List<ExampleSentenceEntry>, tags: String) -> Unit,
    existingWord: Word? = null,
    existingMeanings: List<Meaning> = emptyList(),
    existingExampleSentences: List<ExampleSentence> = emptyList(),
    existingTags: List<String> = emptyList(),
    duplicateWarning: String? = null,
) {
    val isEditing = existingWord != null
    val context = LocalContext.current
    val commonPartsOfSpeech = getCommonPartsOfSpeech()
    var word by remember { mutableStateOf(existingWord?.word ?: "") }
    var phoneticUK by remember { mutableStateOf(existingWord?.phoneticUK ?: "") }
    var phoneticUS by remember { mutableStateOf(existingWord?.phoneticUS ?: "") }
    val meaningEntries = remember { mutableStateListOf<MeaningEntry>() }
    val exampleSentenceEntries = remember { mutableStateListOf<ExampleSentenceEntry>() }
    var tags by remember { mutableStateOf(TextFieldValue(existingWord?.tags ?: "")) }
    var meaningsLoaded by remember { mutableStateOf(false) }
    var examplesLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(existingMeanings.hashCode()) {
        if (existingMeanings.isNotEmpty() && !meaningsLoaded) {
            meaningEntries.clear()
            meaningEntries.addAll(existingMeanings.map { MeaningEntry(it.partOfSpeech ?: "", it.definition) })
            meaningsLoaded = true
        }
        if (meaningEntries.isEmpty()) {
            meaningEntries.add(MeaningEntry())
        }
    }

    LaunchedEffect(existingExampleSentences.hashCode()) {
        if (existingExampleSentences.isNotEmpty() && !examplesLoaded) {
            exampleSentenceEntries.clear()
            exampleSentenceEntries.addAll(existingExampleSentences.map { ExampleSentenceEntry(it.sentence, it.translation ?: "") })
            examplesLoaded = true
        }
        if (exampleSentenceEntries.isEmpty()) {
            exampleSentenceEntries.add(ExampleSentenceEntry())
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
                text = if (isEditing) stringResource(R.string.edit_word) else stringResource(R.string.add_word),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.word_label)) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = phoneticUK,
                    onValueChange = { phoneticUK = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.phonetic_uk)) },
                    singleLine = true,
                    placeholder = { Text("/brɪtɪʃ/") },
                )
                OutlinedTextField(
                    value = phoneticUS,
                    onValueChange = { phoneticUS = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.phonetic_us)) },
                    singleLine = true,
                    placeholder = { Text("/ˈæmerɪkən/") },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.meanings),
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
                Text(stringResource(R.string.add_meaning))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.example_sentences),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))

            exampleSentenceEntries.forEachIndexed { index, entry ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = entry.sentence,
                            onValueChange = { exampleSentenceEntries[index] = entry.copy(sentence = it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.example_sentence_label, index + 1), style = MaterialTheme.typography.bodySmall) },
                            minLines = 2,
                        )
                        if (exampleSentenceEntries.size > 1) {
                            IconButton(onClick = { exampleSentenceEntries.removeAt(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = entry.translation,
                        onValueChange = { exampleSentenceEntries[index] = entry.copy(translation = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.translation), style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { exampleSentenceEntries.add(ExampleSentenceEntry()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_example))
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.tags_label)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.tags_placeholder)) },
            )
            if (existingTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val selectedTags = remember(tags.text) {
                    tags.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                }
                Text(
                    text = stringResource(R.string.existing_tags),
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
                    val validExamples = exampleSentenceEntries.filter { it.sentence.isNotBlank() }
                    onConfirm(
                        word.trim(),
                        phoneticUK.ifBlank { null },
                        phoneticUS.ifBlank { null },
                        validMeanings,
                        validExamples,
                        tags.text.trim()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeaningEntryRow(
    entry: MeaningEntry,
    onEntryChange: (MeaningEntry) -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
) {
    val context = LocalContext.current
    val commonPartsOfSpeech = getCommonPartsOfSpeech()
    var posExpanded by remember { mutableStateOf(false) }
    var posInput by remember { mutableStateOf(entry.partOfSpeech) }
    var showCustomInput by remember { mutableStateOf(entry.partOfSpeech.isNotEmpty() && entry.partOfSpeech !in commonPartsOfSpeech) }
    val definitionFocusRequester = remember { FocusRequester() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                    .fillMaxWidth()
                    .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
                label = { Text(stringResource(R.string.part_of_speech), style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posExpanded) },
                readOnly = !showCustomInput,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { nextFocusRequester?.requestFocus() ?: definitionFocusRequester.requestFocus() }
                ),
            )
            ExposedDropdownMenu(
                expanded = posExpanded,
                onDismissRequest = { posExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.pos_none)) },
                    onClick = {
                        posExpanded = false
                        showCustomInput = false
                        posInput = ""
                        onEntryChange(entry.copy(partOfSpeech = ""))
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
                commonPartsOfSpeech.forEach { pos ->
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
                    text = { Text(stringResource(R.string.pos_custom)) },
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

        OutlinedTextField(
            value = entry.definition,
            onValueChange = { onEntryChange(entry.copy(definition = it)) },
            modifier = Modifier
                .weight(1f)
                .focusRequester(definitionFocusRequester),
            label = { Text(stringResource(R.string.meanings), style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { nextFocusRequester?.requestFocus() }
            ),
        )

        if (showDelete) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
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
        title = { Text(stringResource(R.string.confirm_delete)) },
        text = {
            Text(
                if (meaningCount > 0) {
                    stringResource(R.string.confirm_delete_word_with_meanings, word, meaningCount)
                } else {
                    stringResource(R.string.confirm_delete_word, word)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
