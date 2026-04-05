package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongAnswerDao {
    @Query("SELECT * FROM wrong_answers ORDER BY timestamp DESC")
    fun getAllWrongAnswers(): Flow<List<WrongAnswer>>

    @Query("SELECT * FROM wrong_answers WHERE wordId = :wordId ORDER BY timestamp DESC")
    fun getWrongAnswersByWordId(wordId: Long): Flow<List<WrongAnswer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongAnswer(wrongAnswer: WrongAnswer): Long

    @Query("DELETE FROM wrong_answers WHERE id = :id")
    suspend fun deleteWrongAnswerById(id: Long)

    @Query("DELETE FROM wrong_answers WHERE wordId = :wordId")
    suspend fun deleteWrongAnswersByWordId(wordId: Long)

    @Query("DELETE FROM wrong_answers")
    suspend fun clearAllWrongAnswers()

    @Query("SELECT COUNT(*) FROM wrong_answers")
    fun getWrongAnswerCount(): Flow<Int>

    @Query("SELECT DISTINCT wordId FROM wrong_answers")
    suspend fun getWrongAnswerWordIds(): List<Long>
}
