package io.github.nwma_fywf.mineword.data.local

import kotlinx.serialization.Serializable

@Serializable
data class ExportMeaning(
    val partOfSpeech: String? = null,
    val definition: String,
    val order: Int = 0
)

@Serializable
data class ExportExampleSentence(
    val sentence: String,
    val translation: String? = null,
    val order: Int = 0
)

@Serializable
data class ExportWord(
    val word: String,
    val phoneticUK: String? = null,
    val phoneticUS: String? = null,
    val audioUrl: String? = null,
    val tags: String = "",
    val synonyms: String = "",
    val personalNotes: String = "",
    val meanings: List<ExportMeaning> = emptyList(),
    val exampleSentences: List<ExportExampleSentence> = emptyList()
)

@Serializable
data class ExportPhrase(
    val phrase: String,
    val meaning: String = "",
    val tags: String = "",
    val personalNotes: String = ""
)

@Serializable
data class ExportData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val words: List<ExportWord> = emptyList(),
    val phrases: List<ExportPhrase> = emptyList()
)
