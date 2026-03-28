package io.github.nwma_fywf.mineword.data.repository

import io.github.nwma_fywf.mineword.data.local.ExportData
import io.github.nwma_fywf.mineword.data.local.ExportExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExportMeaning
import io.github.nwma_fywf.mineword.data.local.ExportWord
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExampleSentenceDao
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.MeaningDao
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WordDao
import kotlinx.coroutines.flow.Flow

data class ImportResult(
    val totalCount: Int,
    val successCount: Int,
    val skipCount: Int,
    val replacedCount: Int,
    val mergedCount: Int,
    val duplicateWords: List<String>
)

enum class DuplicateStrategy {
    REPLACE,
    SKIP,
    MERGE
}

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

    suspend fun exportAllData(): ExportData {
        val words = wordDao.getAllWordsOnce()
        val exportWords = words.map { word ->
            val meanings = meaningDao.getMeaningsByWordIdOnce(word.id)
            val sentences = exampleSentenceDao.getSentencesByWordIdOnce(word.id)
            ExportWord(
                word = word.word,
                phoneticUK = word.phoneticUK,
                phoneticUS = word.phoneticUS,
                audioUrl = word.audioUrl,
                tags = word.tags,
                synonyms = word.synonyms,
                phraseCollocations = word.phraseCollocations,
                personalNotes = word.personalNotes,
                meanings = meanings.map { m ->
                    ExportMeaning(
                        partOfSpeech = m.partOfSpeech,
                        definition = m.definition,
                        order = m.order
                    )
                },
                exampleSentences = sentences.map { s ->
                    ExportExampleSentence(
                        sentence = s.sentence,
                        translation = s.translation,
                        order = s.order
                    )
                }
            )
        }
        return ExportData(words = exportWords)
    }

    suspend fun importWords(
        exportWords: List<ExportWord>,
        duplicateStrategy: DuplicateStrategy
    ): ImportResult {
        var successCount = 0
        var skipCount = 0
        var replacedCount = 0
        var mergedCount = 0
        val duplicateWords = mutableListOf<String>()

        for (exportWord in exportWords) {
            val existingWords = wordDao.getWordsByWordLower(exportWord.word)

            if (existingWords.isNotEmpty()) {
                duplicateWords.add(exportWord.word)
                when (duplicateStrategy) {
                    DuplicateStrategy.SKIP -> {
                        skipCount++
                    }
                    DuplicateStrategy.REPLACE -> {
                        val existingWord = existingWords.first()
                        val newWord = existingWord.copy(
                            phoneticUK = exportWord.phoneticUK,
                            phoneticUS = exportWord.phoneticUS,
                            audioUrl = exportWord.audioUrl,
                            tags = exportWord.tags,
                            synonyms = exportWord.synonyms,
                            phraseCollocations = exportWord.phraseCollocations,
                            personalNotes = exportWord.personalNotes
                        )
                        wordDao.updateWord(newWord)
                        saveMeanings(newWord.id, exportWord.meanings.map { m ->
                            Meaning(wordId = newWord.id, partOfSpeech = m.partOfSpeech, definition = m.definition, order = m.order)
                        })
                        saveExampleSentences(newWord.id, exportWord.exampleSentences.map { s ->
                            ExampleSentence(wordId = newWord.id, sentence = s.sentence, translation = s.translation, order = s.order)
                        })
                        replacedCount++
                    }
                    DuplicateStrategy.MERGE -> {
                        val existingWord = existingWords.first()
                        val existingMeanings = meaningDao.getMeaningsByWordIdOnce(existingWord.id).toMutableList()
                        val existingSentences = exampleSentenceDao.getSentencesByWordIdOnce(existingWord.id).toMutableList()

                        for (m in exportWord.meanings) {
                            if (existingMeanings.none { it.definition == m.definition }) {
                                existingMeanings.add(Meaning(wordId = existingWord.id, partOfSpeech = m.partOfSpeech, definition = m.definition, order = existingMeanings.size))
                            }
                        }

                        for (s in exportWord.exampleSentences) {
                            if (existingSentences.none { it.sentence == s.sentence }) {
                                existingSentences.add(ExampleSentence(wordId = existingWord.id, sentence = s.sentence, translation = s.translation, order = existingSentences.size))
                            }
                        }

                        val updatedTags = if (exportWord.tags.isNotEmpty()) {
                            val existingTags = existingWord.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                            exportWord.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { existingTags.add(it) }
                            existingTags.joinToString(",")
                        } else {
                            existingWord.tags
                        }

                        val newWord = existingWord.copy(
                            tags = updatedTags,
                            synonyms = if (exportWord.synonyms.isNotEmpty()) exportWord.synonyms else existingWord.synonyms,
                            phraseCollocations = if (exportWord.phraseCollocations.isNotEmpty()) exportWord.phraseCollocations else existingWord.phraseCollocations,
                            personalNotes = if (exportWord.personalNotes.isNotEmpty()) exportWord.personalNotes else existingWord.personalNotes
                        )
                        wordDao.updateWord(newWord)
                        saveMeanings(newWord.id, existingMeanings)
                        saveExampleSentences(newWord.id, existingSentences)
                        mergedCount++
                    }
                }
            } else {
                val newWord = Word(
                    word = exportWord.word,
                    phoneticUK = exportWord.phoneticUK,
                    phoneticUS = exportWord.phoneticUS,
                    audioUrl = exportWord.audioUrl,
                    tags = exportWord.tags,
                    synonyms = exportWord.synonyms,
                    phraseCollocations = exportWord.phraseCollocations,
                    personalNotes = exportWord.personalNotes
                )
                val wordId = wordDao.insertWord(newWord)
                saveMeanings(wordId, exportWord.meanings.map { m ->
                    Meaning(wordId = wordId, partOfSpeech = m.partOfSpeech, definition = m.definition, order = m.order)
                })
                saveExampleSentences(wordId, exportWord.exampleSentences.map { s ->
                    ExampleSentence(wordId = wordId, sentence = s.sentence, translation = s.translation, order = s.order)
                })
                successCount++
            }
        }

        return ImportResult(
            totalCount = exportWords.size,
            successCount = successCount,
            skipCount = skipCount,
            replacedCount = replacedCount,
            mergedCount = mergedCount,
            duplicateWords = duplicateWords
        )
    }
}
