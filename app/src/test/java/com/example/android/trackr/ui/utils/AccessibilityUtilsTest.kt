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
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.User
import com.example.android.trackr.utils.DateTimeUtils
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
    fun taskSummaryLabel_noTags() {
        assertThat(
            AccessibilityUtils.taskSummaryLabel(
                application,
                taskSummary,
                fakeClock
            )
        ).isEqualTo("task 1. Owner: user. $dateTimeValue.")
    }

    @Test
    fun taskSummaryLabel_withTags() {
        assertThat(
            AccessibilityUtils.taskSummaryLabel(
                application,
                taskSummaryWithTags,
                fakeClock
            )
        ).isEqualTo("task 2. Owner: user. $dateTimeValue. Tag: tag1. Tag: tag2")
    }


    companion object {
        private val user1 = User(1, "user", Avatar.DEFAULT_USER)
        private val tag1 = Tag(1, "tag1", TagColor.BLUE)
        private val tag2 = Tag(2, "tag2", TagColor.RED)
        private const val dateTimeValue = "Due today"

        var taskSummary = TaskSummary(
            id = 1,
            title = "task 1",
            dueAt = Instant.now(),
            owner = user1,
            status = TaskStatus.IN_PROGRESS,
            tags = emptyList(),
            orderInCategory = 1,
            starred = false,
        )

        var taskSummaryWithTags = TaskSummary(
            id = 2,
            title = "task 2",
            dueAt = Instant.now(),
            owner = user1,
            status = TaskStatus.IN_PROGRESS,
            tags = listOf(tag1, tag2),
            orderInCategory = 2,
            starred = false,
        )
    }
}