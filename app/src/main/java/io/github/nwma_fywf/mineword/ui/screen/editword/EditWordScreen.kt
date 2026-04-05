package io.github.nwma_fywf.mineword.ui.screen.editword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.ui.component.WordFormData
import io.github.nwma_fywf.mineword.ui.component.WordFormScreen

@Composable
fun EditWordScreen(
    viewModel: EditWordViewModel,
    wordId: Long,
    onNavigateBack: () -> Unit,
) {
    val wordData by viewModel.wordData.collectAsState()
    val existingTags by viewModel.existingTags.collectAsState()
    val existingWords by viewModel.existingWords.collectAsState()

    LaunchedEffect(wordId) {
        viewModel.loadWord(wordId)
    }

    WordFormScreen(
        title = stringResource(R.string.edit_word),
        initialWord = wordData?.word,
        initialMeanings = wordData?.meanings ?: emptyList(),
        initialExampleSentences = wordData?.exampleSentences ?: emptyList(),
        existingTags = existingTags,
        existingWords = existingWords,
        onNavigateBack = onNavigateBack,
        onSave = { data ->
            wordData?.let { wd ->
                viewModel.updateWord(
                    word = wd.word,
                    newWord = data.word,
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
            }
        },
        onCheckDuplicate = { word ->
            wordData?.let { wd ->
                viewModel.checkDuplicate(word, wd.word.id)
            } ?: viewModel.checkDuplicate(word, -1)
        }
    )
}
