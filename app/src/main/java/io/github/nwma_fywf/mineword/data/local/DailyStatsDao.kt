package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStats)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: Long): DailyStats?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentStats(limit: Int): Flow<List<DailyStats>>

    @Query("SELECT SUM(newWordsCount) FROM daily_stats")
    suspend fun getTotalNewWords(): Int

    @Query("SELECT SUM(reviewedWordsCount) FROM daily_stats")
    suspend fun getTotalReviewedWords(): Int

    @Query("SELECT SUM(correctCount) FROM daily_stats")
    suspend fun getTotalCorrect(): Int

    @Query("SELECT SUM(wrongCount) FROM daily_stats")
    suspend fun getTotalWrong(): Int
}
