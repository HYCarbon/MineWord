package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases ORDER BY createdAt DESC")
    fun getAllPhrases(): Flow<List<Phrase>>

    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhraseById(id: Long): Phrase?

    @Query("""
        SELECT DISTINCT p.* FROM phrases p
        WHERE p.phrase LIKE '%' || :query || '%'
           OR p.meaning LIKE '%' || :query || '%'
           OR p.tags LIKE '%' || :query || '%'
        ORDER BY p.createdAt DESC
    """)
    fun searchPhrases(query: String): Flow<List<Phrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: Phrase): Long

    @Update
    suspend fun updatePhrase(phrase: Phrase)

    @Delete
    suspend fun deletePhrase(phrase: Phrase)

    @Query("SELECT tags FROM phrases WHERE tags != ''")
    fun getAllTagsRaw(): Flow<List<String>>

    @Query("SELECT * FROM phrases WHERE LOWER(phrase) = LOWER(:phrase) LIMIT 1")
    suspend fun getPhraseByPhrase(phrase: String): Phrase?

    @Query("SELECT * FROM phrases ORDER BY createdAt DESC")
    suspend fun getAllPhrasesOnce(): List<Phrase>

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun getPhraseCount(): Int

    @Query("DELETE FROM phrases")
    suspend fun deleteAllPhrases()
}
