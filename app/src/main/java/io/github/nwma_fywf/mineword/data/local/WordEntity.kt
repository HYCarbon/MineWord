package io.github.nwma_fywf.mineword.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phoneticUK: String? = null,
    val phoneticUS: String? = null,
    val audioUrl: String? = null,
    val tags: String = "",
    val synonyms: String = "",
    val personalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val learningStage: Int = 0,
    val lastReviewTime: Long = 0,
    val nextReviewTime: Long = 0
)
