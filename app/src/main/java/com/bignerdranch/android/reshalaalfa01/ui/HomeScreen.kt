package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.UserData
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userData: UserData?,
    history: List<RecognitionEntity>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onShowMoreClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onFeedbackClick: (RecognitionEntity) -> Unit
) {
    var showProfileMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar( 
                title = { 
                    Text(
                        text = buildAnnotatedString {
                            append("Re∫λala")
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    baselineShift = BaselineShift.Superscript,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("alfa")
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
                            onDismissRequest = { showProfileMenu = false }
                        ) {
                            userData?.let {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = it.username,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = it.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = { Text("Logout") },
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
            if (history.isEmpty() && !isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No history yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val recentHistory = history.take(3)
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
                    
                    if (history.size > 3) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = onShowMoreClick) {
                                    Text(
                                        "Show more",
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
            history = mockHistory,
            isRefreshing = false,
            onRefresh = {},
            onLogout = {},
            onShowMoreClick = {},
            onTaskClick = {},
            onFeedbackClick = {}
        )
    }
}
