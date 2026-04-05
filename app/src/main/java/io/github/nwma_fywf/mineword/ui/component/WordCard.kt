package io.github.nwma_fywf.mineword.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.util.MeaningParser

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun WordCard(
    word: Word,
    meanings: List<Meaning>,
    exampleSentences: List<ExampleSentence> = emptyList(),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleMedium,
            )

            if (!word.phoneticUK.isNullOrBlank() || !word.phoneticUS.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        if (!word.phoneticUK.isNullOrBlank()) {
                            append(stringResource(R.string.phonetic_uk_prefix, word.phoneticUK))
                        }
                        if (!word.phoneticUK.isNullOrBlank() && !word.phoneticUS.isNullOrBlank()) {
                            append("  ")
                        }
                        if (!word.phoneticUS.isNullOrBlank()) {
                            append(stringResource(R.string.phonetic_us_prefix, word.phoneticUS))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            MeaningsContent(meanings = meanings)
            if (exampleSentences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ExampleSentencesContent(sentences = exampleSentences)
            }
            val tags = word.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
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

            val synonyms = word.synonyms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val antonyms = word.antonyms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (synonyms.isNotEmpty() || antonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (synonyms.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.synonyms_prefix, synonyms.joinToString(", ")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (antonyms.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.antonyms_prefix, antonyms.joinToString(", ")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            val phrases = word.phrases.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (phrases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.phrases_prefix, phrases.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val wordForms = word.wordForms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (wordForms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.word_forms_prefix, wordForms.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            if (word.personalNotes.isNotBlank()) {
                val lines = word.personalNotes.split("\n")
                if (lines.any { it.contains("辨析") || it.startsWith("混") }) {
                    val confusionNote = lines.find { it.contains("辨析") || it.startsWith("混") }
                    if (confusionNote != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.confusion_prefix, confusionNote),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeaningsContent(meanings: List<Meaning>) {
    if (meanings.isEmpty()) {
        Text(
            text = stringResource(R.string.no_meaning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val grouped = MeaningParser.groupByPos(meanings)

    if (meanings.size == 1 && meanings[0].partOfSpeech == null) {
        Text(
            text = meanings[0].definition,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        grouped.forEach { (pos, posMeanings) ->
            val prefix = if (pos != null) "[$pos] " else ""
            posMeanings.forEach { meaning ->
                Text(
                    text = "$prefix${meaning.definition}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ExampleSentencesContent(sentences: List<ExampleSentence>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sentences.take(2).forEach { sentence ->
            Column {
                Text(
                    text = sentence.sentence,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!sentence.translation.isNullOrBlank()) {
                    Text(
                        text = sentence.translation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
