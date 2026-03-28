package io.github.nwma_fywf.mineword.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ExportData
import io.github.nwma_fywf.mineword.data.local.ExportWord
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
import io.github.nwma_fywf.mineword.data.repository.ImportResult
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ImportDialogState(
    val isVisible: Boolean = false,
    val pendingWords: List<ExportWord> = emptyList(),
    val duplicateWords: List<String> = emptyList()
)

data class ImportResultDialogState(
    val isVisible: Boolean = false,
    val result: ImportResult? = null
)

data class ExportResultState(
    val isSuccess: Boolean = false,
    val message: String = ""
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val repository: WordRepository,
    private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importDialogState = MutableStateFlow(ImportDialogState())
    val importDialogState: StateFlow<ImportDialogState> = _importDialogState.asStateFlow()

    private val _importResultDialogState = MutableStateFlow(ImportResultDialogState())
    val importResultDialogState: StateFlow<ImportResultDialogState> = _importResultDialogState.asStateFlow()

    private val _exportResult = MutableStateFlow(ExportResultState())
    val exportResult: StateFlow<ExportResultState> = _exportResult.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val exportData = repository.exportAllData()
                val json = Json { prettyPrint = true }
                val jsonString = json.encodeToString(exportData)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                _exportResult.value = ExportResultState(
                    isSuccess = true,
                    message = "成功导出 ${exportData.words.size} 个单词"
                )
            } catch (e: Exception) {
                _exportResult.value = ExportResultState(
                    isSuccess = false,
                    message = "导出失败: ${e.message}"
                )
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun processImportFile(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: throw Exception("无法读取文件")
                val json = Json { ignoreUnknownKeys = true }
                val exportData = json.decodeFromString<ExportData>(jsonString)
                
                val existingWords = repository.getWordByWord("")
                val duplicateWords = mutableListOf<String>()
                for (word in exportData.words) {
                    val existing = repository.getWordByWord(word.word)
                    if (existing != null) {
                        duplicateWords.add(word.word)
                    }
                }

                if (duplicateWords.isNotEmpty()) {
                    _importDialogState.value = ImportDialogState(
                        isVisible = true,
                        pendingWords = exportData.words,
                        duplicateWords = duplicateWords
                    )
                } else {
                    val result = repository.importWords(exportData.words, DuplicateStrategy.SKIP)
                    _importResultDialogState.value = ImportResultDialogState(
                        isVisible = true,
                        result = result
                    )
                }
            } catch (e: Exception) {
                _importResultDialogState.value = ImportResultDialogState(
                    isVisible = true,
                    result = ImportResult(
                        totalCount = 0,
                        successCount = 0,
                        skipCount = 0,
                        replacedCount = 0,
                        mergedCount = 0,
                        duplicateWords = emptyList()
                    )
                )
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun onDuplicateStrategySelected(strategy: DuplicateStrategy) {
        viewModelScope.launch {
            val pendingWords = _importDialogState.value.pendingWords
            _importDialogState.value = _importDialogState.value.copy(isVisible = false)
            
            val result = repository.importWords(pendingWords, strategy)
            _importResultDialogState.value = ImportResultDialogState(
                isVisible = true,
                result = result
            )
        }
    }

    fun dismissImportDialog() {
        _importDialogState.value = _importDialogState.value.copy(isVisible = false)
    }

    fun dismissImportResultDialog() {
        _importResultDialogState.value = _importResultDialogState.value.copy(isVisible = false)
    }

    fun dismissExportResult() {
        _exportResult.value = ExportResultState()
    }

    companion object {
        fun provideFactory(
            themePreferences: ThemePreferences,
            repository: WordRepository,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(themePreferences, repository, context) as T
                }
            }
        }
    }
}
