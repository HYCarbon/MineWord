package io.github.nwma_fywf.mineword.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey
    val date: Long,
    val newWordsCount: Int = 0,
    val reviewedWordsCount: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)
