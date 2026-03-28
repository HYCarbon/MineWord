package io.github.nwma_fywf.mineword.ui.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
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
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importDialogState by viewModel.importDialogState.collectAsState()
    val importResultDialogState by viewModel.importResultDialogState.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it)
        }
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
        uri?.let {
            viewModel.setCustomFontFromUri(it)
        }
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
        Text(
            text = "主题设置",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(8.dp)
            ) {
                ThemeOption(
                    text = "跟随系统",
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                )
                ThemeOption(
                    text = "亮色模式",
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                )
                ThemeOption(
                    text = "暗色模式",
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "莫奈取色",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "使用系统壁纸颜色（Android 12+）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { viewModel.setUseDynamicColor(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "字体设置",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(8.dp)
            ) {
                FontStyleOption(
                    text = "默认",
                    selected = fontStyle == FontStyle.DEFAULT,
                    onClick = { viewModel.setFontStyle(FontStyle.DEFAULT) }
                )
                FontStyleOption(
                    text = "衬线体 (Serif)",
                    selected = fontStyle == FontStyle.SERIF,
                    onClick = { viewModel.setFontStyle(FontStyle.SERIF) }
                )
                FontStyleOption(
                    text = "无衬线体 (Sans Serif)",
                    selected = fontStyle == FontStyle.SANS_SERIF,
                    onClick = { viewModel.setFontStyle(FontStyle.SANS_SERIF) }
                )
                FontStyleOption(
                    text = "等宽字体 (Monospace)",
                    selected = fontStyle == FontStyle.MONOSPACE,
                    onClick = { viewModel.setFontStyle(FontStyle.MONOSPACE) }
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
                        TextButton(onClick = { viewModel.clearCustomFont() }) {
                            Text("清除")
                        }
                    }
                }

                DataManagementOption(
                    title = "从文件选择字体",
                    subtitle = "选择 .ttf 或 .otf 字体文件",
                    isLoading = false,
                    onClick = {
                        fontFileLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf"))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "数据管理",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                DataManagementOption(
                    title = "导出数据",
                    subtitle = "将所有单词导出为JSON文件",
                    isLoading = isExporting,
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        exportLauncher.launch("mineword_backup_$timestamp.json")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DataManagementOption(
                    title = "批量导入",
                    subtitle = "选择多个JSON文件批量导入单词",
                    isLoading = isImporting,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                )
            }
        }
    }

    if (importDialogState.isVisible) {
        DuplicateStrategyDialog(
            duplicateCount = importDialogState.duplicateWords.size,
            duplicateWords = importDialogState.duplicateWords,
            onStrategySelected = { viewModel.onDuplicateStrategySelected(it) },
            onDismiss = { viewModel.dismissImportDialog() }
        )
    }

    if (importResultDialogState.isVisible) {
        val result = importResultDialogState.result
        ImportResultDialog(
            result = result,
            onDismiss = { viewModel.dismissImportResultDialog() }
        )
    }
}

@Composable
private fun FontStyleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun DataManagementOption(
    title: String,
    subtitle: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                    StrategyOption(
                        text = "替换现有数据",
                        description = "用导入的数据覆盖重复的单词",
                        selected = selectedStrategy == DuplicateStrategy.REPLACE,
                        onClick = { selectedStrategy = DuplicateStrategy.REPLACE }
                    )
                    StrategyOption(
                        text = "跳过重复项",
                        description = "保留原数据，忽略重复项",
                        selected = selectedStrategy == DuplicateStrategy.SKIP,
                        onClick = { selectedStrategy = DuplicateStrategy.SKIP }
                    )
                    StrategyOption(
                        text = "合并",
                        description = "合并释义和例句，去重",
                        selected = selectedStrategy == DuplicateStrategy.MERGE,
                        onClick = { selectedStrategy = DuplicateStrategy.MERGE }
                    )
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
private fun StrategyOption(
    text: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
                    if (result.successCount > 0) {
                        Text("新增: ${result.successCount}")
                    }
                    if (result.replacedCount > 0) {
                        Text("替换: ${result.replacedCount}")
                    }
                    if (result.mergedCount > 0) {
                        Text("合并: ${result.mergedCount}")
                    }
                    if (result.skipCount > 0) {
                        Text("跳过: ${result.skipCount}")
                    }
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
