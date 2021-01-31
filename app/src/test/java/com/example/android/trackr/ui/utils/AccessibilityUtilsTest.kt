/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.ui.utils

import android.app.Application
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.R
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import com.google.common.truth.Truth.assertThat


@RunWith(AndroidJUnit4::class)
class AccessibilityUtilsTest {
    private val dateInEpochSecond = 1584310694L // March 15, 2020
    private val fakeClock =
        Clock.fixed(Instant.ofEpochSecond(dateInEpochSecond), ZoneId.of("US/Central"))
    private lateinit var resources: Resources
    private lateinit var application: Application

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        resources = application.resources

        mockkObject(DateTimeUtils)
        every {
            DateTimeUtils.durationMessageOrDueDate(
                resources,
                any(),
                fakeClock
            )
        } returns dateTimeValue
    }

    @Test
    fun taskListItemLabel_noTags() {
        assertThat(
            AccessibilityUtils.taskListItemLabel(
                application,
                taskListItem,
                fakeClock
            )
        ).isEqualTo("task 1. Owner: user. $dateTimeValue.")
    }

    @Test
    fun taskListItemLabel_withTags() {
        assertThat(
            AccessibilityUtils.taskListItemLabel(
                application,
                taskListItemWithTags,
                fakeClock
            )
        ).isEqualTo("task 2. Owner: user. $dateTimeValue. Tag: tag1. Tag: tag2")
    }


    companion object {
        private val user1 = User(1, "user", Avatar.DEFAULT_USER)
        private val tag1 = Tag(1, "tag1", TagColor.BLUE)
        private val tag2 = Tag(2, "tag2", TagColor.RED)
        private val dateTimeValue = "Due today"

        var taskListItem = TaskListItem(
            id = 1,
            title = "task 1",
            dueAt = Instant.now(),
            owner = user1,
            state = TaskState.IN_PROGRESS,
            starUsers = emptyList(),
            tags = emptyList()
        )

        var taskListItemWithTags = TaskListItem(
            id = 2,
            title = "task 2",
            dueAt = Instant.now(),
            owner = user1,
            state = TaskState.IN_PROGRESS,
            starUsers = emptyList(),
            tags = listOf(tag1, tag2)
        )
    }
}