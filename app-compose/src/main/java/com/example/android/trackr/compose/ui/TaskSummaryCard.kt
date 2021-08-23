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

package com.example.android.trackr.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.trackr.compose.R
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.example.android.trackr.utils.DateTimeUtils
import java.time.Clock
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskSummaryCard(
    summary: TaskSummary,
    clock: Clock,
    onStarClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row {
            Spacer(modifier = Modifier.width(8.dp))
            // The star icon.
            StarIconButton(
                onClick = onStarClick,
                filled = summary.starred,
                contentDescription = "",
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.padding(top = 12.dp, end = 12.dp, bottom = 12.dp)) {
                // The title.
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(4.dp))
                // The owner.
                TaskSummaryOwner(owner = summary.owner)
                Spacer(modifier = Modifier.height(2.dp))
                // The due date.
                TaskSummaryDueAt(dueAt = summary.dueAt, clock = clock)
                Spacer(modifier = Modifier.height(4.dp))
                // The tags.
                TagGroup(
                    tags = summary.tags,
                    max = 3
                )
            }
        }
    }
}

@Composable
private fun TaskSummaryOwner(owner: User) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(owner.avatar.drawableResId),
            contentDescription = stringResource(R.string.owner)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = owner.username)
    }
}

@Composable
private fun TaskSummaryDueAt(dueAt: Instant, clock: Clock) {
    val resources = LocalContext.current.resources
    Text(
        text = DateTimeUtils.durationMessageOrDueDate(resources, dueAt, clock),
    )
}

@Preview
@Composable
private fun PreviewTaskSummaryCard() {
    TrackrTheme {
        TaskSummaryCard(
            summary = TaskSummary(
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
            ),
            clock = Clock.systemDefaultZone(),
            onStarClick = {},
            onClick = {},
        )
    }
}
