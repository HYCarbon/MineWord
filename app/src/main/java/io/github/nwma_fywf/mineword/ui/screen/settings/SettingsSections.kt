package io.github.nwma_fywf.mineword.ui.screen.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
import io.github.nwma_fywf.mineword.data.repository.ImportResult
import io.github.nwma_fywf.mineword.ui.component.ClickableOption
import io.github.nwma_fywf.mineword.ui.component.RadioOption
import io.github.nwma_fywf.mineword.ui.component.SettingsClickableCard
import io.github.nwma_fywf.mineword.ui.component.SettingsSectionCard
import io.github.nwma_fywf.mineword.ui.component.SwitchOption
import io.github.nwma_fywf.mineword.ui.theme.StandardThemeColors

@Composable
fun ThemeSettingsSection(
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
fun FontSettingsSection(
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
fun DataStatsSection(
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
fun DataManagementSection(
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
fun LearningSettingsSection(
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
