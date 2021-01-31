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

package com.example.android.trackr.ui.tasks

import com.example.android.trackr.R
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat
import org.threeten.bp.Instant


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class DataItemCreatorTest {
    @Test
    fun execute_whenArgsAreNull() {
        val subject = DataItemsCreator(null, null)
        assertThat(subject.execute()).isNull()
    }

    @Test
    fun execute_withNullExpandedStatesMap() {
        val subject = DataItemsCreator(listOf(inProgressTaskListItem), null)
        assertThat(subject.execute()).isNull()
    }

    @Test
    fun execute_withNullTaskListItems() {
        val subject = DataItemsCreator(null, mutableMapOf(TaskState.IN_PROGRESS to true))
        assertThat(subject.execute()).isNull()
    }

    @Test
    fun execute_whenNoTaskListItems() {
        val subject = DataItemsCreator(emptyList(), mutableMapOf(TaskState.IN_PROGRESS to true))
        val dataItems = subject.execute()
        assertThat(dataItems!!.size).isEqualTo(1)
        assertThat(dataItems[0]).isInstanceOf(DataItem.HeaderItem::class.java)
        val header = (dataItems[0] as DataItem.HeaderItem)
        assertThat(header.headerData.count).isEqualTo(0)
        assertThat(header.headerData.taskState).isEqualTo(TaskState.IN_PROGRESS)
    }

    @Test
    fun execute_whenExpandedState_returnsHeaderAndTaskListItem() {
        val subject = DataItemsCreator(
            listOf(inProgressTaskListItem),
            mutableMapOf(TaskState.IN_PROGRESS to true)
        )
        val dataItems = subject.execute()
        assertThat(dataItems!!.size).isEqualTo(2)
        assertThat(dataItems[0]).isInstanceOf(DataItem.HeaderItem::class.java)
        assertThat(dataItems[1]).isInstanceOf(DataItem.TaskItem::class.java)
    }

    @Test
    fun execute_whenCollapsedState_savesCountInHeader() {
        val subject = DataItemsCreator(
            listOf(inProgressTaskListItem),
            mutableMapOf(TaskState.IN_PROGRESS to false)
        )
        val dataItems = subject.execute()
        val header = (dataItems!![0] as DataItem.HeaderItem)
        assertThat(header.headerData.count).isEqualTo(1)
    }

    companion object {
        private val user = User(1, "user", Avatar.DEFAULT_USER)
        val inProgressTaskListItem = TaskListItem(
            id = 1,
            title = "task list item 1",
            dueAt = Instant.now(),
            owner = user,
            state = TaskState.IN_PROGRESS,
            starUsers = emptyList(),
            tags = emptyList()
        )
    }
}