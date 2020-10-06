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
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
    private lateinit var taskItemListener: TestListener

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        context = application
        frameLayout = FrameLayout(context)
        taskItemListener = TestListener()
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

    @Test
    fun bindHeaderViewHolder_initialState() {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.NOT_STARTED)
        )
        val holder = TasksAdapter.HeaderViewHolder.from(frameLayout)
        val headerData = HeaderData("some label")

        assertThat(holder.binding.headerData).isNull()

        holder.bind(headerData)

        assertThat(holder.binding.headerData).isEqualTo(headerData)
    }

    @Test
    fun bindTaskViewHolder_initialState() {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.NOT_STARTED)
        )
        val holder = TasksAdapter.TaskViewHolder.from(frameLayout, taskItemListener)

        assertThat(holder.binding.taskListItem).isNull()
        assertThat(holder.binding.listener).isNull()
        assertThat(holder.accessibilityActionIds).isEmpty()

        holder.bind(taskListItem)

        assertThat(holder.binding.listener).isEqualTo(taskItemListener)
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)
        assertThat(holder.binding.taskListItem).isEqualTo(taskListItem)
    }

    @Test
    fun bindTaskViewHolder_addingAccessibilityAction_isIdempotent() {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.NOT_STARTED)
        )
        val holder = TasksAdapter.TaskViewHolder.from(frameLayout, taskItemListener)
        holder.bind(taskListItem)
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)

        holder.bind(taskListItem)
        // If previously added accessibility actions are not cleared, when the holder is rebound,
        // the actions will get added again. This check guards against that.
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)
    }


    @Test
    fun swipe_archivesItem() {
        val mockListener = Mockito.mock(TasksAdapter.TaskItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.onSwipe()

        Mockito.verify(mockListener).onItemArchived(taskListItem)
    }

    @Test
    fun accessibilityAction_archivesItem() {
        val mockListener = Mockito.mock(TasksAdapter.TaskItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.binding.root.performAccessibilityAction(holder.accessibilityActionIds[0], null)

        Mockito.verify(mockListener).onItemArchived(taskListItem)
    }

    private fun setUpAndBindTaskViewHolder(
        listener: TasksAdapter.TaskItemListener
    ): TasksAdapter.TaskViewHolder {
        tasksAdapter.addHeadersAndSubmitList(
            context,
            listOf(taskListItem),
            listOf(TaskState.NOT_STARTED)
        )
        val holder = TasksAdapter.TaskViewHolder.from(frameLayout, listener)
        holder.bind(taskListItem)

        return holder
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
