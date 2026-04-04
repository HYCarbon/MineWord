package io.github.nwma_fywf.mineword.ui.screen.editword

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
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
fun EditWordScreen(
    viewModel: EditWordViewModel,
    wordId: Long,
    onNavigateBack: () -> Unit,
) {
    val wordData by viewModel.wordData.collectAsState()
    val existingTags by viewModel.existingTags.collectAsState()
    val existingWords by viewModel.existingWords.collectAsState()
    val scope = rememberCoroutineScope()

    val wordFocusRequester = remember { FocusRequester() }
    val phoneticUKFocusRequester = remember { FocusRequester() }
    val phoneticUSFocusRequester = remember { FocusRequester() }
    val meaningFocusRequester = remember { FocusRequester() }
    val exampleSentenceFocusRequester = remember { FocusRequester() }
    val exampleTranslationFocusRequester = remember { FocusRequester() }
    val tagsFocusRequester = remember { FocusRequester() }
    val synonymsFocusRequester = remember { FocusRequester() }
    val antonymsFocusRequester = remember { FocusRequester() }
    val phrasesFocusRequester = remember { FocusRequester() }
    val derivativesFocusRequester = remember { FocusRequester() }
    val frequencyLevelFocusRequester = remember { FocusRequester() }
    val personalNotesFocusRequester = remember { FocusRequester() }

    val meaningFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    val exampleFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    val exampleTranslationFocusRequesters = remember { mutableStateListOf<FocusRequester>() }

    var word by remember { mutableStateOf("") }
    var phoneticUK by remember { mutableStateOf("") }
    var phoneticUS by remember { mutableStateOf("") }
    val meaningEntries = remember { mutableStateListOf<MeaningEntry>() }
    val exampleSentences = remember { mutableStateListOf<ExampleSentenceEntry>() }
    if (meaningFocusRequesters.isEmpty() && meaningEntries.isNotEmpty()) {
        meaningFocusRequesters.add(meaningFocusRequester)
        for (i in 1 until meaningEntries.size) {
            meaningFocusRequesters.add(FocusRequester())
        }
    }
    if (exampleFocusRequesters.isEmpty() && exampleSentences.isNotEmpty()) {
        exampleFocusRequesters.add(exampleSentenceFocusRequester)
        exampleTranslationFocusRequesters.add(exampleTranslationFocusRequester)
        for (i in 1 until exampleSentences.size) {
            exampleFocusRequesters.add(FocusRequester())
            exampleTranslationFocusRequesters.add(FocusRequester())
        }
    }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var synonyms by remember { mutableStateOf("") }
    var antonyms by remember { mutableStateOf("") }
    var phrases by remember { mutableStateOf("") }
    var derivatives by remember { mutableStateOf("") }
    var frequencyLevel by remember { mutableStateOf("") }
    var personalNotes by remember { mutableStateOf("") }
    var duplicateWarning by remember { mutableStateOf<String?>(null) }
    var dataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(wordId) {
        viewModel.loadWord(wordId)
    }

    LaunchedEffect(wordData) {
        wordData?.let { data ->
            if (!dataLoaded) {
                word = data.word.word
                phoneticUK = data.word.phoneticUK ?: ""
                phoneticUS = data.word.phoneticUS ?: ""
                tags = TextFieldValue(data.word.tags ?: "")
                synonyms = data.word.synonyms ?: ""
                antonyms = data.word.antonyms ?: ""
                phrases = data.word.phrases ?: ""
                derivatives = data.word.derivatives ?: ""
                frequencyLevel = data.word.frequencyLevel ?: ""
                personalNotes = data.word.personalNotes ?: ""
                meaningEntries.clear()
                meaningEntries.addAll(data.meanings.map { MeaningEntry(it.partOfSpeech ?: "", it.definition) })
                if (meaningEntries.isEmpty()) {
                    meaningEntries.add(MeaningEntry())
                }
                exampleSentences.clear()
                exampleSentences.addAll(data.exampleSentences.map { ExampleSentenceEntry(it.sentence, it.translation ?: "") })
                if (exampleSentences.isEmpty()) {
                    exampleSentences.add(ExampleSentenceEntry())
                }
                dataLoaded = true
            }
        }
    }

    val canSave = word.isNotBlank() && meaningEntries.any { it.definition.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑单词") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                wordData?.let { data ->
                                    if (viewModel.checkDuplicate(word.trim(), data.word.id)) {
                                        duplicateWarning = "单词 \"${word.trim()}\" 已存在"
                                    } else {
                                        val validMeanings = meaningEntries.filter { it.definition.isNotBlank() }
                                        val meanings = validMeanings.mapIndexed { index, entry ->
                                            Meaning(
                                                wordId = data.word.id,
                                                partOfSpeech = entry.partOfSpeech.ifBlank { null },
                                                definition = entry.definition,
                                                order = index
                                            )
                                        }
                                        val validExamples = exampleSentences.filter { it.sentence.isNotBlank() }
                                        val examples = validExamples.mapIndexed { index, entry ->
                                            ExampleSentence(
                                                wordId = data.word.id,
                                                sentence = entry.sentence,
                                                translation = entry.translation.ifBlank { null },
                                                order = index
                                            )
                                        }
                                        viewModel.updateWord(
                                            word = data.word,
                                            newWord = word.trim(),
                                            phoneticUK = phoneticUK.ifBlank { null },
                                            phoneticUS = phoneticUS.ifBlank { null },
                                            meanings = meanings,
                                            exampleSentences = examples,
                                            tags = tags.text.trim(),
                                            synonyms = synonyms.trim(),
                                            antonyms = antonyms.trim(),
                                            phrases = phrases.trim(),
                                            derivatives = derivatives.trim(),
                                            frequencyLevel = frequencyLevel.trim(),
                                            personalNotes = personalNotes.trim(),
                                            onComplete = onNavigateBack
                                        )
                                    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(wordFocusRequester),
                label = { Text("单词") },
                singleLine = true,
                isError = duplicateWarning != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { phoneticUKFocusRequester.requestFocus() }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = phoneticUK,
                    onValueChange = { phoneticUK = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(phoneticUKFocusRequester),
                    label = { Text("英式音标") },
                    singleLine = true,
                    placeholder = { Text("/brɪtɪʃ/") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { phoneticUSFocusRequester.requestFocus() }
                    ),
                )
                OutlinedTextField(
                    value = phoneticUS,
                    onValueChange = { phoneticUS = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(phoneticUSFocusRequester),
                    label = { Text("美式音标") },
                    singleLine = true,
                    placeholder = { Text("/ˈæmerɪkən/") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { meaningFocusRequester.requestFocus() }
                    ),
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
                    focusRequester = if (index == 0) meaningFocusRequester else meaningFocusRequesters.getOrNull(index),
                    nextFocusRequester = if (index == meaningEntries.lastIndex) exampleSentenceFocusRequester else null,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(exampleFocusRequesters.getOrElse(index) { FocusRequester() }),
                            label = { Text("例句 ${index + 1}", style = MaterialTheme.typography.bodySmall) },
                            minLines = 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { exampleTranslationFocusRequesters.getOrNull(index)?.requestFocus() ?: tagsFocusRequester.requestFocus() }
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = entry.translation,
                            onValueChange = { exampleSentences[index] = entry.copy(translation = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(exampleTranslationFocusRequesters.getOrElse(index) { FocusRequester() }),
                            label = { Text("翻译", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (index == exampleSentences.lastIndex) {
                                        tagsFocusRequester.requestFocus()
                                    } else {
                                        exampleFocusRequesters.getOrNull(index + 1)?.requestFocus()
                                    }
                                }
                            ),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(tagsFocusRequester),
                label = { Text("标签（逗号分隔）") },
                singleLine = true,
                placeholder = { Text("如: CET-4, 动词") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { synonymsFocusRequester.requestFocus() }
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
                value = synonyms,
                onValueChange = { synonyms = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(synonymsFocusRequester),
                label = { Text("同义词") },
                placeholder = { Text("如: happy, joyful, delighted") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { antonymsFocusRequester.requestFocus() }
                ),
            )
            val currentSynonyms = synonyms.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            val availableWordsForSynonyms = existingWords.filter { w -> 
                w.id != wordData?.word?.id && w.word !in currentSynonyms 
            }.take(20)
            if (availableWordsForSynonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "从词库选择同义词",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    availableWordsForSynonyms.forEach { w ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val trimmed = synonyms.trimEnd()
                                val newText = when {
                                    trimmed.isEmpty() -> "${w.word}, "
                                    trimmed.endsWith(",") -> "$trimmed ${w.word}, "
                                    else -> "$trimmed, ${w.word}, "
                                }
                                synonyms = newText
                            },
                            label = { Text(w.word) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = antonyms,
                onValueChange = { antonyms = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(antonymsFocusRequester),
                label = { Text("反义词") },
                placeholder = { Text("如: sad, unhappy, miserable") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { phrasesFocusRequester.requestFocus() }
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phrases,
                onValueChange = { phrases = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phrasesFocusRequester),
                label = { Text("短语搭配") },
                placeholder = { Text("如: account for (解释); take part in (参加)") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { derivativesFocusRequester.requestFocus() }
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = derivatives,
                onValueChange = { derivatives = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(derivativesFocusRequester),
                label = { Text("派生词/词根词缀") },
                placeholder = { Text("如: -tion (名词后缀); happy → happiness") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { frequencyLevelFocusRequester.requestFocus() }
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = frequencyLevel,
                onValueChange = { frequencyLevel = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(frequencyLevelFocusRequester),
                label = { Text("考频/词频标记") },
                placeholder = { Text("如: 高考, CET-4, 高频") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { personalNotesFocusRequester.requestFocus() }
                ),
            )

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
