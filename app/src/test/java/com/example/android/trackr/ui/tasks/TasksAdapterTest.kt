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

import android.app.Application
import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TasksAdapterTest {
    class TestListener : TasksAdapter.TaskItemListener {
        override fun onItemClicked(taskListItem: TaskListItem) {}
        override fun onItemArchived(taskListItem: TaskListItem) {}
    }

    private val tasksAdapter = TasksAdapter(TestListener())
    private lateinit var context: Context
    private lateinit var frameLayout: FrameLayout

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        context = application
        frameLayout = FrameLayout(context)
    }

    @Test
    fun onCreateViewHolder_whenTypeHeader() {
        val viewHolder = tasksAdapter.onCreateViewHolder(
            frameLayout,
            TasksAdapter.ITEM_VIEW_TYPE_HEADER
        )
        assertThat(viewHolder).isInstanceOf(TasksAdapter.HeaderViewHolder::class.java)
    }

    @Test
    fun onCreateViewHolder_whenTypeTask() {
        val viewHolder = tasksAdapter.onCreateViewHolder(
            frameLayout,
            TasksAdapter.ITEM_VIEW_TYPE_TASK
        )
        assertThat(viewHolder).isInstanceOf(TasksAdapter.TaskViewHolder::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onCreateViewHolder_whenTypeUnknown() {
        tasksAdapter.onCreateViewHolder(frameLayout, -1)
    }

    @Test
    fun getItemViewType_withMatchingTask_showsHeaderAndTask() {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.IN_PROGRESS)
        )

        assertThat(tasksAdapter.currentList.size).isEqualTo(2)
        assertThat(tasksAdapter.getItemViewType(0)).isEqualTo(TasksAdapter.ITEM_VIEW_TYPE_HEADER)
        assertThat(tasksAdapter.getItemViewType(1)).isEqualTo(TasksAdapter.ITEM_VIEW_TYPE_TASK)
    }

    @Test
    fun getItemViewType_noMatchingTask_StillShowsHeader() {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.NOT_STARTED)
        )

        assertThat(tasksAdapter.currentList.size).isEqualTo(1)
        assertThat(tasksAdapter.getItemViewType(0)).isEqualTo(TasksAdapter.ITEM_VIEW_TYPE_HEADER)
    }


    companion object {
        private val user = User(1, "user")
        val taskListItem = TaskListItem(
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
