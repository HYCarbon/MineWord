package io.github.nwma_fywf.mineword.data.repository

import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WordDao
import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    suspend fun getWordById(id: Long): Word? = wordDao.getWordById(id)

    fun searchWords(query: String): Flow<List<Word>> = wordDao.searchWords(query)

    suspend fun insertWord(word: Word): Long = wordDao.insertWord(word)

    suspend fun updateWord(word: Word) = wordDao.updateWord(word)

    suspend fun deleteWord(word: Word) = wordDao.deleteWord(word)

    fun getAllTagsRaw(): Flow<List<String>> = wordDao.getAllTagsRaw()

    suspend fun getWordByWord(word: String): Word? = wordDao.getWordByWord(word)
}
