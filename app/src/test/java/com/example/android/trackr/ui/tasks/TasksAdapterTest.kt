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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TasksAdapterTest {
    class TestListener : TasksAdapter.ItemListener {
        override fun onHeaderClicked(headerData: HeaderData) {}
        override fun onTaskClicked(taskListItem: TaskListItem) {}
        override fun onTaskArchived(taskListItem: TaskListItem) {}
        override fun onTaskDragged(fromPosition: Int, toPosition: Int) {}
    }

    private val dateInEpochSecond = 1584310694L // March 15, 2020
    private val fakeClock =
        Clock.fixed(Instant.ofEpochSecond(dateInEpochSecond), ZoneId.systemDefault())
    private val tasksAdapter = TasksAdapter(TestListener(), user, fakeClock)
    private lateinit var context: Context
    private lateinit var frameLayout: FrameLayout
    private lateinit var testItemListener: TestListener

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        context = application
        frameLayout = FrameLayout(context)
        testItemListener = TestListener()
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
    fun bindHeaderViewHolder_initialState() {
        val holder = TasksAdapter.HeaderViewHolder.from(frameLayout, testItemListener)
        val headerData = HeaderData(1, taskState = TaskState.NOT_STARTED)

        assertThat(holder.binding.headerData).isNull()

        holder.bind(headerData)

        assertThat(holder.binding.headerData).isEqualTo(headerData)
    }

    @Test
    fun bindTaskViewHolder_initialState() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user, fakeClock)

        assertThat(holder.binding.taskListItem).isNull()
        assertThat(holder.binding.listener).isNull()
        assertThat(holder.accessibilityActionIds).isEmpty()

        holder.bind(inProgressTaskListItem)

        assertThat(holder.binding.listener).isEqualTo(testItemListener)
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)
        assertThat(holder.binding.taskListItem).isEqualTo(inProgressTaskListItem)
    }

    @Test
    fun bindTaskViewHolder_starredTaskShownForCurrentUser() {
        val currentUser = user2
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, currentUser, fakeClock)

        assertFalse(holder.binding.star.isChecked)

        holder.bind(starredTaskListItem)

        assertTrue(holder.binding.star.isChecked)
    }

    @Test
    fun bindTaskViewHolder_starredTaskNotShownForOtherUsers() {
        val currentUser = user
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, currentUser, fakeClock)

        holder.bind(starredTaskListItem)

        assertFalse(holder.binding.star.isChecked)
    }

    @Test
    fun bindTaskViewHolder_addingAccessibilityAction_isIdempotent() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user, fakeClock)
        holder.bind(inProgressTaskListItem)
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)

        holder.bind(inProgressTaskListItem)
        // If previously added accessibility actions are not cleared, when the holder is rebound,
        // the actions will get added again. This check guards against that.
        assertThat(holder.accessibilityActionIds.size).isEqualTo(1)
    }

    @Test
    fun swipe_archivesItem() {
        val mockListener = Mockito.mock(TasksAdapter.ItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.onItemSwiped()

        Mockito.verify(mockListener).onTaskArchived(inProgressTaskListItem)
    }

    @Test
    fun accessibilityAction_archivesItem() {
        val mockListener = Mockito.mock(TasksAdapter.ItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.binding.root.performAccessibilityAction(holder.accessibilityActionIds[0], null)

        Mockito.verify(mockListener).onTaskArchived(inProgressTaskListItem)
    }

    private fun setUpAndBindTaskViewHolder(
        listener: TasksAdapter.ItemListener
    ): TasksAdapter.TaskViewHolder {
        val holder = TasksAdapter.TaskViewHolder.from(frameLayout, listener, user, fakeClock)
        holder.bind(inProgressTaskListItem)
        return holder
    }

    companion object {
        private val user = User(1, "user")
        private val user2 = User(2, "user2")

        val inProgressTaskListItem = TaskListItem(
            id = 1,
            title = "task list item 1",
            dueAt = Instant.now(),
            owner = user,
            state = TaskState.IN_PROGRESS,
            starUsers = emptyList(),
            tags = emptyList()
        )

        val starredTaskListItem = TaskListItem(
            id = 2,
            title = "task list item 2",
            dueAt = Instant.now(),
            owner = user2,
            state = TaskState.IN_PROGRESS,
            starUsers = listOf(user2),
            tags = emptyList()
        )
    }
}
