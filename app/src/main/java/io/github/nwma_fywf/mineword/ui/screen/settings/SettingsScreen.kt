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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
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
    val clearDataDialogState by viewModel.clearDataDialogState.collectAsState()
    val dataStats by viewModel.dataStats.collectAsState()
    var showColorPickerDialog by remember { mutableStateOf(false) }
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
            wordCount = dataStats.wordCount,
            phraseCount = dataStats.phraseCount
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
            onReviewReminderChange = viewModel::setReviewReminderEnabled
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
            phraseCount = clearDataDialogState.phraseCount,
            onConfirm = viewModel::confirmClearData,
            onDismiss = viewModel::dismissClearDataDialog
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
        text = "主题设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsSectionCard {
        RadioOption(
            text = "跟随系统",
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
        )
        RadioOption(
            text = "亮色模式",
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onThemeModeChange(ThemeMode.LIGHT) }
        )
        RadioOption(
            text = "暗色模式",
            selected = themeMode == ThemeMode.DARK,
            onClick = { onThemeModeChange(ThemeMode.DARK) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SwitchOption(
            title = "莫奈取色",
            subtitle = "使用系统壁纸颜色（Android 12+）",
            checked = useDynamicColor,
            onCheckedChange = onUseDynamicColorChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "主题颜色",
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
                    text = "已选择自定义颜色",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearThemeColor) {
                    Text("清除")
                }
            }
        }

        ClickableOption(
            title = "自定义 RGB 颜色",
            subtitle = "输入 RGB 值自定义主题色",
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
        text = "字体设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsSectionCard {
        RadioOption(
            text = "默认",
            selected = fontStyle == FontStyle.DEFAULT,
            onClick = { onFontStyleChange(FontStyle.DEFAULT) }
        )
        RadioOption(
            text = "衬线体 (Serif)",
            selected = fontStyle == FontStyle.SERIF,
            onClick = { onFontStyleChange(FontStyle.SERIF) }
        )
        RadioOption(
            text = "无衬线体 (Sans Serif)",
            selected = fontStyle == FontStyle.SANS_SERIF,
            onClick = { onFontStyleChange(FontStyle.SANS_SERIF) }
        )
        RadioOption(
            text = "等宽字体 (Monospace)",
            selected = fontStyle == FontStyle.MONOSPACE,
            onClick = { onFontStyleChange(FontStyle.MONOSPACE) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "自定义字体",
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
                    text = "已选择自定义字体",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearFont) {
                    Text("清除")
                }
            }
        }

        ClickableOption(
            title = "从文件选择字体",
            subtitle = "选择 .ttf 或 .otf 字体文件",
            onClick = onSelectFontFile
        )
    }
}

@Composable
private fun DataStatsSection(
    wordCount: Int,
    phraseCount: Int
) {
    Text(
        text = "词汇统计",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = wordCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "单词",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phraseCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "词组",
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
        text = "数据管理",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        ClickableOption(
            title = "导出数据",
            subtitle = "将所有单词导出为JSON文件",
            isLoading = isExporting,
            onClick = onExport
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ClickableOption(
            title = "批量导入",
            subtitle = "选择多个JSON文件批量导入单词",
            isLoading = isImporting,
            onClick = onImport
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ClickableOption(
            title = "清除所有数据",
            subtitle = "删除所有单词、词组和学习记录",
            onClick = onClearData
        )
    }
}

@Composable
private fun LearningSettingsSection(
    reviewReminderEnabled: Boolean,
    onReviewReminderChange: (Boolean) -> Unit
) {
    Text(
        text = "学习设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    SettingsClickableCard {
        SwitchOption(
            title = "复习提醒",
            subtitle = "基于艾宾浩斯遗忘曲线定时提醒复习",
            checked = reviewReminderEnabled,
            onCheckedChange = onReviewReminderChange
        )
    }
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
        title = { Text("发现重复单词") },
        text = {
            Column {
                Text("检测到 $duplicateCount 个重复单词：")
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
                                text = "...等 ${duplicateWords.size} 个",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("请选择处理方式：")
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
                            Text("替换现有数据", style = MaterialTheme.typography.bodyMedium)
                            Text("用导入的数据覆盖重复的单词", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("跳过重复项", style = MaterialTheme.typography.bodyMedium)
                            Text("保留原数据，忽略重复项", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("合并", style = MaterialTheme.typography.bodyMedium)
                            Text("合并释义和例句，去重", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onStrategySelected(selectedStrategy) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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
        title = { Text("导入结果") },
        text = {
            if (result == null || result.totalCount == 0) {
                Text("导入失败，请检查文件格式是否正确。")
            } else {
                Column {
                    Text("共 ${result.totalCount} 个单词")
                    if (result.successCount > 0) Text("新增: ${result.successCount}")
                    if (result.replacedCount > 0) Text("替换: ${result.replacedCount}")
                    if (result.mergedCount > 0) Text("合并: ${result.mergedCount}")
                    if (result.skipCount > 0) Text("跳过: ${result.skipCount}")
                    if (result.duplicateWords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "注意: ${result.duplicateWords.size} 个重复单词未处理",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确定")
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
        title = { Text("自定义 RGB 颜色") },
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
                        text = "预览",
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
                Text("范围: 0-255", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ClearDataConfirmDialog(
    wordCount: Int,
    phraseCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认清除所有数据？") },
        text = {
            Column {
                Text("此操作将删除以下数据：")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• $wordCount 个单词")
                Text("• $phraseCount 个词组")
                Text("• 所有学习记录和错题")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "此操作不可撤销，请确保已备份重要数据。",
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
                Text("确认删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}