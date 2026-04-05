package io.github.nwma_fywf.mineword.ui.screen.addword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.nwma_fywf.mineword.ui.component.WordFormData
import io.github.nwma_fywf.mineword.ui.component.WordFormScreen

@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel,
    onNavigateBack: () -> Unit,
) {
    val existingTags by viewModel.existingTags.collectAsState()
    val existingWords by viewModel.existingWords.collectAsState()

    WordFormScreen(
        title = "添加单词",
        existingTags = existingTags,
        existingWords = existingWords,
        onNavigateBack = onNavigateBack,
        onSave = { data ->
            viewModel.insertWord(
                word = data.word,
                phoneticUK = data.phoneticUK,
                phoneticUS = data.phoneticUS,
                meanings = data.meanings,
                exampleSentences = data.exampleSentences,
                tags = data.tags,
                synonyms = data.synonyms,
                antonyms = data.antonyms,
                phrases = data.phrases,
                wordForms = data.wordForms,
                source = data.source,
                personalNotes = data.personalNotes,
                onComplete = onNavigateBack
            )
        },
        onCheckDuplicate = { word ->
            viewModel.checkDuplicate(word)
        }
    )
}
