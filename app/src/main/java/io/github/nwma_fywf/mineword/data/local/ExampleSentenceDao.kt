package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExampleSentenceDao {
    @Query("SELECT * FROM example_sentences WHERE wordId = :wordId ORDER BY `order`")
    fun getSentencesByWordId(wordId: Long): Flow<List<ExampleSentence>>

    @Query("SELECT * FROM example_sentences WHERE wordId = :wordId ORDER BY `order`")
    suspend fun getSentencesByWordIdOnce(wordId: Long): List<ExampleSentence>

    @Query("SELECT COUNT(*) FROM example_sentences WHERE wordId = :wordId")
    suspend fun getSentenceCount(wordId: Long): Int

    @Insert
    suspend fun insertAll(sentences: List<ExampleSentence>)

    @Query("DELETE FROM example_sentences WHERE wordId = :wordId")
    suspend fun deleteByWordId(wordId: Long)
}
