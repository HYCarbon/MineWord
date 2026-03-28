package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeaningDao {
    @Query("SELECT * FROM meanings WHERE wordId = :wordId ORDER BY `order`")
    fun getMeaningsByWordId(wordId: Long): Flow<List<Meaning>>

    @Query("SELECT * FROM meanings WHERE wordId = :wordId ORDER BY `order`")
    suspend fun getMeaningsByWordIdOnce(wordId: Long): List<Meaning>

    @Query("SELECT COUNT(*) FROM meanings WHERE wordId = :wordId")
    suspend fun getMeaningCount(wordId: Long): Int

    @Insert
    suspend fun insertAll(meanings: List<Meaning>)

    @Query("DELETE FROM meanings WHERE wordId = :wordId")
    suspend fun deleteByWordId(wordId: Long)
}
