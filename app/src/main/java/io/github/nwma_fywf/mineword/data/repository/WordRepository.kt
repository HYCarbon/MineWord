package io.github.nwma_fywf.mineword.data.repository

import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExampleSentenceDao
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.MeaningDao
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WordDao
import kotlinx.coroutines.flow.Flow

class WordRepository(
    private val wordDao: WordDao,
    private val meaningDao: MeaningDao,
    private val exampleSentenceDao: ExampleSentenceDao
) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    suspend fun getWordById(id: Long): Word? = wordDao.getWordById(id)

    fun searchWords(query: String): Flow<List<Word>> = wordDao.searchWords(query)

    suspend fun insertWord(word: Word): Long = wordDao.insertWord(word)

    suspend fun updateWord(word: Word) = wordDao.updateWord(word)

    suspend fun deleteWord(word: Word) = wordDao.deleteWord(word)

    fun getAllTagsRaw(): Flow<List<String>> = wordDao.getAllTagsRaw()

    suspend fun getWordByWord(word: String): Word? = wordDao.getWordByWord(word)

    fun getMeaningsByWordId(wordId: Long): Flow<List<Meaning>> =
        meaningDao.getMeaningsByWordId(wordId)

    suspend fun getMeaningCount(wordId: Long): Int =
        meaningDao.getMeaningCount(wordId)

    suspend fun saveMeanings(wordId: Long, meanings: List<Meaning>) {
        meaningDao.deleteByWordId(wordId)
        if (meanings.isNotEmpty()) {
            meaningDao.insertAll(meanings.map { it.copy(wordId = wordId) })
        }
    }

    fun getExampleSentencesByWordId(wordId: Long): Flow<List<ExampleSentence>> =
        exampleSentenceDao.getSentencesByWordId(wordId)

    suspend fun saveExampleSentences(wordId: Long, sentences: List<ExampleSentence>) {
        exampleSentenceDao.deleteByWordId(wordId)
        if (sentences.isNotEmpty()) {
            exampleSentenceDao.insertAll(sentences.map { it.copy(wordId = wordId) })
        }
    }
}
