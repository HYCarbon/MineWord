package io.github.nwma_fywf.mineword.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onConfirm: (word: String, definition: String) -> Unit,
) {
    var word by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "添加单词",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("单词") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = definition,
                onValueChange = { definition = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("释义") },
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (word.isNotBlank() && definition.isNotBlank()) {
                        onConfirm(word.trim(), definition.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = word.isNotBlank() && definition.isNotBlank(),
            ) {
                Text("保存")
            }
        }
    }
}
