package io.github.nwma_fywf.mineword.ui.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val fontStyle by viewModel.fontStyle.collectAsState()
    val customFontPath by viewModel.customFontPath.collectAsState()
    val customPrimaryColor by viewModel.customPrimaryColor.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importDialogState by viewModel.importDialogState.collectAsState()
    val importResultDialogState by viewModel.importResultDialogState.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val reviewReminderEnabled by viewModel.reviewReminderEnabled.collectAsState()
    val reviewReminderHour by viewModel.reviewReminderHour.collectAsState()
    val reviewReminderMinute by viewModel.reviewReminderMinute.collectAsState()
    val clearDataDialogState by viewModel.clearDataDialogState.collectAsState()
    val dataStats by viewModel.dataStats.collectAsState()
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.processImportFiles(uris)
        }
    }

    val fontFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setCustomFontFromUri(it) }
    }

    LaunchedEffect(exportResult) {
        if (exportResult.message.isNotEmpty()) {
            Toast.makeText(context, exportResult.message, Toast.LENGTH_SHORT).show()
            viewModel.dismissExportResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ThemeSettingsSection(
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            customPrimaryColor = customPrimaryColor,
            onThemeModeChange = viewModel::setThemeMode,
            onUseDynamicColorChange = viewModel::setUseDynamicColor,
            onThemeColorSelected = viewModel::setCustomThemeColor,
            onClearThemeColor = viewModel::clearCustomThemeColor,
            onCustomColorClick = { showColorPickerDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        FontSettingsSection(
            fontStyle = fontStyle,
            customFontPath = customFontPath,
            onFontStyleChange = viewModel::setFontStyle,
            onSelectFontFile = { fontFileLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf")) },
            onClearFont = viewModel::clearCustomFont
        )

        Spacer(modifier = Modifier.height(24.dp))

        DataStatsSection(
            wordCount = dataStats.wordCount
        )

        Spacer(modifier = Modifier.height(24.dp))

        DataManagementSection(
            isExporting = isExporting,
            isImporting = isImporting,
            onExport = {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                exportLauncher.launch("mineword_backup_$timestamp.json")
            },
            onImport = { importLauncher.launch(arrayOf("application/json")) },
            onClearData = viewModel::showClearDataDialog
        )

        Spacer(modifier = Modifier.height(24.dp))

        LearningSettingsSection(
            reviewReminderEnabled = reviewReminderEnabled,
            reviewReminderHour = reviewReminderHour,
            reviewReminderMinute = reviewReminderMinute,
            onReviewReminderChange = viewModel::setReviewReminderEnabled,
            onTimeClick = { showTimePickerDialog = true }
        )
    }

    if (importDialogState.isVisible) {
        DuplicateStrategyDialog(
            duplicateCount = importDialogState.duplicateWords.size,
            duplicateWords = importDialogState.duplicateWords,
            onStrategySelected = viewModel::onDuplicateStrategySelected,
            onDismiss = viewModel::dismissImportDialog
        )
    }

    if (importResultDialogState.isVisible) {
        ImportResultDialog(
            result = importResultDialogState.result,
            onDismiss = viewModel::dismissImportResultDialog
        )
    }

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            onColorSelected = { primary, secondary, tertiary ->
                viewModel.setCustomThemeColor(primary, secondary, tertiary)
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }

    if (clearDataDialogState.isVisible) {
        ClearDataConfirmDialog(
            wordCount = clearDataDialogState.wordCount,
            onConfirm = viewModel::confirmClearData,
            onDismiss = viewModel::dismissClearDataDialog
        )
    }

    if (showTimePickerDialog) {
        SettingsTimePickerDialog(
            initialHour = reviewReminderHour,
            initialMinute = reviewReminderMinute,
            onTimeSelected = { hour, minute ->
                viewModel.setReviewReminderTime(hour, minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}
