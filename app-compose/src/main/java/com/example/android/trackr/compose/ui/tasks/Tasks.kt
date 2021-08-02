/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.compose.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.trackr.compose.ui.TaskSummaryCard
import com.example.android.trackr.compose.ui.TrackrTheme
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Composable
fun Tasks(
    viewModel: TasksViewModel,
    onTaskClick: (taskId: Long) -> Unit
) {
    val taskSummaries by viewModel.taskSummaries.collectAsState(emptyList())
    TasksContent(
        summaries = taskSummaries,
        clock = viewModel.clock,
        onStarClick = { viewModel.toggleTaskStarState(it) },
        onTaskClick = onTaskClick,
    )
}

@Composable
private fun TasksContent(
    summaries: List<TaskSummary>,
    clock: Clock,
    onStarClick: (taskId: Long) -> Unit,
    onTaskClick: (taskId: Long) -> Unit
) {
    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.systemBars),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            items = summaries,
            key = { summary -> summary.id },
        ) { summary ->
            TaskSummaryCard(
                summary = summary,
                clock = clock,
                onStarClick = { onStarClick(summary.id) },
                onClick = { onTaskClick(summary.id) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTasksContent() {
    TrackrTheme {
        Scaffold {
            TasksContent(
                summaries = listOf(
                    TaskSummary(
                        id = 1L,
                        title = "Create default illustrations for event types",
                        status = TaskStatus.IN_PROGRESS,
                        dueAt = Instant.now() + Duration.ofHours(73),
                        orderInCategory = 1,
                        isArchived = false,
                        owner = User(id = 1L, username = "Daring Dove", avatar = Avatar.DARING_DOVE),
                        tags = listOf(
                            Tag(id = 1L, label = "2.3 release", color = TagColor.BLUE),
                            Tag(id = 4L, label = "UI/UX", color = TagColor.PURPLE),
                        ),
                        starred = true,
                    )
                ),
                clock = Clock.systemDefaultZone(),
                onStarClick = {},
                onTaskClick = {}
            )
        }
    }
}
