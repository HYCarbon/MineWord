package io.github.nwma_fywf.mineword.ui.component

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.util.MeaningParser

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordCard(
    word: Word,
    meanings: List<Meaning>,
    exampleSentences: List<ExampleSentence> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                            append("英 ${word.phoneticUK}")
                        }
                        if (!word.phoneticUK.isNullOrBlank() && !word.phoneticUS.isNullOrBlank()) {
                            append("  ")
                        }
                        if (!word.phoneticUS.isNullOrBlank()) {
                            append("美 ${word.phoneticUS}")
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
        }
    }
}

@Composable
private fun MeaningsContent(meanings: List<Meaning>) {
    if (meanings.isEmpty()) {
        Text(
            text = "（无释义）",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val grouped = MeaningParser.groupByPos(meanings)
    val showGroupHeaders = grouped.keys.any { it != null }

    if (!showGroupHeaders && meanings.size == 1) {
        Text(
            text = meanings[0].definition,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        grouped.forEach { (pos, posMeanings) ->
            if (pos != null) {
                Text(
                    text = "[$pos]",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (posMeanings.size == 1 && !showGroupHeaders) {
                Text(
                    text = posMeanings[0].definition,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                posMeanings.forEachIndexed { index, meaning ->
                    Text(
                        text = "${index + 1}. ${meaning.definition}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
