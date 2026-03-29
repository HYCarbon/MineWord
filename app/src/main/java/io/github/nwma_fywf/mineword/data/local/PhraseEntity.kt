package io.github.nwma_fywf.mineword.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class Phrase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phrase: String,
    val meaning: String = "",
    val tags: String = "",
    val personalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
