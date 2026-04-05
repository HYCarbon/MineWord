package io.github.nwma_fywf.mineword.ui.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
import io.github.nwma_fywf.mineword.ui.component.ClickableOption
import io.github.nwma_fywf.mineword.ui.component.RadioOption
import io.github.nwma_fywf.mineword.ui.component.SettingsClickableCard
import io.github.nwma_fywf.mineword.ui.component.SettingsSectionCard
import io.github.nwma_fywf.mineword.ui.component.SwitchOption
import io.github.nwma_fywf.mineword.ui.theme.StandardThemeColors
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
        TimePickerDialog(
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

@Composable
private fun ThemeSettingsSection(
    themeMode: ThemeMode,
    useDynamicColor: Boolean,
    customPrimaryColor: Int?,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onThemeColorSelected: (Int, Int, Int) -> Unit,
    onClearThemeColor: () -> Unit,
    onCustomColorClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.theme_settings),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsSectionCard {
        RadioOption(
            text = stringResource(R.string.theme_system),
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
        )
        RadioOption(
            text = stringResource(R.string.theme_light),
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onThemeModeChange(ThemeMode.LIGHT) }
        )
        RadioOption(
            text = stringResource(R.string.theme_dark),
            selected = themeMode == ThemeMode.DARK,
            onClick = { onThemeModeChange(ThemeMode.DARK) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SwitchOption(
            title = stringResource(R.string.theme_monet),
            subtitle = stringResource(R.string.theme_monet_subtitle),
            checked = useDynamicColor,
            onCheckedChange = onUseDynamicColorChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = stringResource(R.string.theme_color),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(StandardThemeColors) { themeColor ->
                val isSelected = customPrimaryColor == themeColor.primary.toArgb()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(themeColor.primary, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            onThemeColorSelected(
                                themeColor.primary.toArgb(),
                                themeColor.secondary.toArgb(),
                                themeColor.tertiary.toArgb()
                            )
                        }
                )
            }
        }

        if (customPrimaryColor != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.custom_color_selected),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearThemeColor) {
                    Text(stringResource(R.string.clear))
                }
            }
        }

        ClickableOption(
            title = stringResource(R.string.custom_rgb_color),
            subtitle = stringResource(R.string.custom_rgb_subtitle),
            onClick = onCustomColorClick
        )
    }
}

@Composable
private fun FontSettingsSection(
    fontStyle: FontStyle,
    customFontPath: String?,
    onFontStyleChange: (FontStyle) -> Unit,
    onSelectFontFile: () -> Unit,
    onClearFont: () -> Unit
) {
    Text(
        text = stringResource(R.string.font_settings),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsSectionCard {
        RadioOption(
            text = stringResource(R.string.font_default),
            selected = fontStyle == FontStyle.DEFAULT,
            onClick = { onFontStyleChange(FontStyle.DEFAULT) }
        )
        RadioOption(
            text = stringResource(R.string.font_serif),
            selected = fontStyle == FontStyle.SERIF,
            onClick = { onFontStyleChange(FontStyle.SERIF) }
        )
        RadioOption(
            text = stringResource(R.string.font_sans_serif),
            selected = fontStyle == FontStyle.SANS_SERIF,
            onClick = { onFontStyleChange(FontStyle.SANS_SERIF) }
        )
        RadioOption(
            text = stringResource(R.string.font_monospace),
            selected = fontStyle == FontStyle.MONOSPACE,
            onClick = { onFontStyleChange(FontStyle.MONOSPACE) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = stringResource(R.string.custom_font),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )

        if (fontStyle == FontStyle.CUSTOM && customFontPath != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.custom_font_selected),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearFont) {
                    Text(stringResource(R.string.clear))
                }
            }
        }

        ClickableOption(
            title = stringResource(R.string.select_font_file),
            subtitle = stringResource(R.string.select_font_file_subtitle),
            onClick = onSelectFontFile
        )
    }
}

@Composable
private fun DataStatsSection(
    wordCount: Int
) {
    Text(
        text = stringResource(R.string.data_stats),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = wordCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.word_count_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DataManagementSection(
    isExporting: Boolean,
    isImporting: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearData: () -> Unit
) {
    Text(
        text = stringResource(R.string.data_management),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        ClickableOption(
            title = stringResource(R.string.export_data),
            subtitle = stringResource(R.string.export_data_subtitle),
            isLoading = isExporting,
            onClick = onExport
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ClickableOption(
            title = stringResource(R.string.batch_import),
            subtitle = stringResource(R.string.batch_import_subtitle),
            isLoading = isImporting,
            onClick = onImport
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ClickableOption(
            title = stringResource(R.string.clear_all_data),
            subtitle = stringResource(R.string.clear_all_data_subtitle),
            onClick = onClearData
        )
    }
}

@Composable
private fun LearningSettingsSection(
    reviewReminderEnabled: Boolean,
    reviewReminderHour: Int,
    reviewReminderMinute: Int,
    onReviewReminderChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.learning_settings),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        SwitchOption(
            title = stringResource(R.string.review_reminder),
            subtitle = stringResource(R.string.review_reminder_subtitle),
            checked = reviewReminderEnabled,
            onCheckedChange = onReviewReminderChange
        )

        if (reviewReminderEnabled) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTimeClick() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_time),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = String.format("%02d:%02d", reviewReminderHour, reviewReminderMinute),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.modify),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_reminder_time)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DuplicateStrategyDialog(
    duplicateCount: Int,
    duplicateWords: List<String>,
    onStrategySelected: (DuplicateStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(DuplicateStrategy.REPLACE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicate_words_found)) },
        text = {
            Column {
                Text(stringResource(R.string.duplicate_words_detected, duplicateCount))
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(duplicateWords.take(10)) { word ->
                        Text(
                            text = word,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (duplicateWords.size > 10) {
                        item {
                            Text(
                                text = stringResource(R.string.etc_count, duplicateWords.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.choose_strategy))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.REPLACE }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.REPLACE,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_replace), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_replace_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.SKIP }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.SKIP,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_skip), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_skip_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.MERGE }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.MERGE,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_merge), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_merge_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onStrategySelected(selectedStrategy) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ImportResultDialog(
    result: io.github.nwma_fywf.mineword.data.repository.ImportResult?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_result)) },
        text = {
            if (result == null || result.totalCount == 0) {
                Text(stringResource(R.string.import_failed))
            } else {
                Column {
                    Text(stringResource(R.string.import_total_count, result.totalCount))
                    if (result.successCount > 0) Text(stringResource(R.string.import_new_count, result.successCount))
                    if (result.replacedCount > 0) Text(stringResource(R.string.import_replaced_count, result.replacedCount))
                    if (result.mergedCount > 0) Text(stringResource(R.string.import_merged_count, result.mergedCount))
                    if (result.skipCount > 0) Text(stringResource(R.string.import_skipped_count, result.skipCount))
                    if (result.duplicateWords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.import_duplicate_warning, result.duplicateWords.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}

@Composable
private fun CustomColorPickerDialog(
    onColorSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableStateOf("25") }
    var green by remember { mutableStateOf("25") }
    var blue by remember { mutableStateOf("25") }

    val previewColor = Color(
        red = (red.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f,
        green = (green.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f,
        blue = (blue.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_rgb_color)) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(previewColor, MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.preview),
                        color = if ((red.toIntOrNull() ?: 0) > 127 ||
                            (green.toIntOrNull() ?: 0) > 127 ||
                            (blue.toIntOrNull() ?: 0) > 127) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("R:", modifier = Modifier.width(24.dp))
                    TextField(value = red, onValueChange = { if (it.length <= 3) red = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("G:", modifier = Modifier.width(24.dp))
                    TextField(value = green, onValueChange = { if (it.length <= 3) green = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("B:", modifier = Modifier.width(24.dp))
                    TextField(value = blue, onValueChange = { if (it.length <= 3) blue = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.range_0_255), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = (red.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val g = (green.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val b = (blue.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val color = Color(r / 255f, g / 255f, b / 255f).toArgb()
                    onColorSelected(color, color, color)
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ClearDataConfirmDialog(
    wordCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_clear_data)) },
        text = {
            Column {
                Text(stringResource(R.string.clear_data_description))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.clear_data_word_count, wordCount))
                Text(stringResource(R.string.clear_data_records))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.clear_data_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm_delete_all))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
