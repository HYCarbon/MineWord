package io.github.nwma_fywf.mineword.ui.screen.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onStartReview: (QuizViewModel.QuizMode) -> Unit,
) {
    val reviewWords by viewModel.reviewWords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习") }
            )
        },
        floatingActionButton = {
            if (reviewWords.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { onStartReview(QuizViewModel.QuizMode.REVIEW) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    text = { Text("开始复习") }
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reviewWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "没有需要复习的单词",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val overdueCount = reviewWords.count { it.status == ReviewStatus.OVERDUE }
                val todayCount = reviewWords.count { it.status == ReviewStatus.DUE_TODAY }
                val tomorrowCount = reviewWords.count { it.status == ReviewStatus.DUE_TOMORROW }

                item {
                    ReviewSummaryCard(
                        overdueCount = overdueCount,
                        todayCount = todayCount,
                        tomorrowCount = tomorrowCount,
                        totalCount = reviewWords.size
                    )
                }

                items(
                    items = reviewWords,
                    key = { it.word.id }
                ) { reviewWord ->
                    ReviewWordCard(
                        word = reviewWord.word,
                        meanings = reviewWord.meanings,
                        status = reviewWord.status,
                        daysUntil = reviewWord.daysUntil,
                        onSkip = { viewModel.skipWord(reviewWord.word.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewSummaryCard(
    overdueCount: Int,
    todayCount: Int,
    tomorrowCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "共 ${totalCount} 个单词待复习",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(count = overdueCount, label = "已过期", color = MaterialTheme.colorScheme.error)
                SummaryItem(count = todayCount, label = "今日", color = MaterialTheme.colorScheme.primary)
                SummaryItem(count = tomorrowCount, label = "明日", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun SummaryItem(count: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ReviewWordCard(
    word: Word,
    meanings: List<Meaning>,
    status: ReviewStatus,
    daysUntil: Int,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(status = status)
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium
                )
                if (meanings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meanings.first().definition,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (status) {
                        ReviewStatus.OVERDUE -> "已过期"
                        ReviewStatus.DUE_TODAY -> "今日到期"
                        ReviewStatus.DUE_TOMORROW -> "明日到期"
                        ReviewStatus.DUE_DAYS -> "${daysUntil}天后到期"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (status) {
                        ReviewStatus.OVERDUE -> MaterialTheme.colorScheme.error
                        ReviewStatus.DUE_TODAY -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: ReviewStatus) {
    val (color, icon) = when (status) {
        ReviewStatus.OVERDUE -> MaterialTheme.colorScheme.error to Icons.Filled.Warning
        ReviewStatus.DUE_TODAY -> MaterialTheme.colorScheme.primary to Icons.Filled.CheckCircle
        ReviewStatus.DUE_TOMORROW -> MaterialTheme.colorScheme.secondary to Icons.Filled.CheckCircle
        ReviewStatus.DUE_DAYS -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Filled.CheckCircle
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = color
    )
}