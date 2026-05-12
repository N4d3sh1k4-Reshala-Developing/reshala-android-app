package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.bignerdranch.android.reshalaalfa01.R
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.UserData
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.UserStatisticData
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userData: UserData?,
    statistic: UserStatisticData?,
    history: List<RecognitionEntity>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onShowMoreClick: () -> Unit,
    onFAQClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onFeedbackClick: (RecognitionEntity) -> Unit,
) {
    var showProfileMenu by remember { mutableStateOf(value = false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar( 
                title = { 
                    val appName = stringResource(R.string.app_name)
                    val alfa = stringResource(R.string.alfa)
                    Text(
                        text = buildAnnotatedString {
                            append(appName)
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    baselineShift = BaselineShift.Superscript,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(alfa)
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showProfileMenu = !showProfileMenu }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = userData?.username ?: "Loading...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ReshalaDarkBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userData?.username?.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier.width(220.dp).padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            userData?.let {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = it.username,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = it.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Settings, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                },
                                onClick = {
                                    showProfileMenu = false
                                    // action for settings
                                }
                            )

                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        stringResource(R.string.logout), 
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                leadingIcon = { 
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    showProfileMenu = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (history.isEmpty() && !isRefreshing && statistic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.no_history), color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onFAQClick) {
                            Text(
                                stringResource(R.string.faq),
                                color = ReshalaDarkBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (statistic != null) {
                        item {
                            Text(
                                text = stringResource(R.string.your_statistics),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StatItem(stringResource(R.string.stat_total), statistic.totalTasks.toString())
                                    StatItem(stringResource(R.string.stat_solved), statistic.successTasks.toString())
                                    StatItem(stringResource(R.string.stat_edited), statistic.editedTasks.toString())
                                    StatItem(stringResource(R.string.stat_direct), statistic.directSolutionTasks.toString())
                                }
                            }
                        }
                    }

                    if (history.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.recent_history),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }

                        val recentHistory = history.take(2)
                        val groupedRecent = recentHistory.groupBy { formatToDate(it.createdAt) }

                        groupedRecent.forEach { (date, itemsList) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(itemsList) { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.large)
                                        .clickable { 
                                            if (item.status != "READY_FOR_FEEDBACK") {
                                                onTaskClick(item.id)
                                            }
                                        }
                                ) {
                                    HistoryItemCard(
                                        item = item,
                                        onActionClick = if (item.status == "READY_FOR_FEEDBACK") {
                                            { onFeedbackClick(item) }
                                        } else null
                                    )
                                }
                            }
                        }
                        
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (history.size > 2) {
                                    TextButton(onClick = onShowMoreClick) {
                                        Text(
                                            stringResource(R.string.show_more),
                                            color = ReshalaDarkBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                TextButton(onClick = onFAQClick) {
                                    Text(
                                        stringResource(R.string.faq),
                                        color = ReshalaDarkBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (!isRefreshing) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_history),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = onFAQClick) {
                                    Text(
                                        stringResource(R.string.faq),
                                        color = ReshalaDarkBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val mockUserData = UserData(
        username = "misha5555548",
        email = "misha@example.com"
    )
    val mockHistory = listOf(
        RecognitionEntity(
            id = "1",
            originalResult = "x^2 + 5x + 6 = 0",
            status = "SOLUTION_READY",
            createdAt = "2024-05-25T14:00:00",
            editedResult = null,
            solutionResult = null
        )
    )

    ReshalaAlfa01Theme {
        HomeScreen(
            userData = mockUserData,
            statistic = null,
            history = mockHistory,
            isRefreshing = false,
            onRefresh = {},
            onLogout = {},
            onShowMoreClick = {},
            onFAQClick = {},
            onTaskClick = {},
            onFeedbackClick = { _ -> }
        )
    }
}
