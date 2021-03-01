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
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.R
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskStatus
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
        override fun onStarClicked(taskListItem: TaskListItem) {}
        override fun onTaskClicked(taskListItem: TaskListItem) {}
        override fun onTaskArchived(taskListItem: TaskListItem) {}
        override fun onTaskDragged(fromPosition: Int, toPosition: Int) {}
        override fun onDragStarted() {}
        override fun onDragCompleted(fromPosition: Int, toPosition: Int, customAction: Boolean) {}
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
        val headerData = HeaderData(1, taskStatus = TaskStatus.NOT_STARTED, expanded = true)

        assertThat(holder.binding.headerData).isNull()
        assertThat(ViewCompat.isAccessibilityHeading(holder.binding.root)).isFalse()

        holder.bind(headerData)

        assertThat(holder.binding.headerData).isEqualTo(headerData)
        assertThat(ViewCompat.isAccessibilityHeading(holder.binding.root)).isTrue()
    }

    // TODO: test expanded/collapsed logic in integration test.
    @Test
    fun headerState_Expanded() {
        val holder = TasksAdapter.HeaderViewHolder.from(frameLayout, testItemListener)
        val headerData = HeaderData(1, taskStatus = TaskStatus.NOT_STARTED, expanded = true)

        holder.bind(headerData)

        assertThat(ViewCompat.getStateDescription(holder.binding.root)).isEqualTo(
            context.getString(
                R.string.expanded
            )
        )
    }

    @Test
    fun headerState_Collapsed() {
        val holder = TasksAdapter.HeaderViewHolder.from(frameLayout, testItemListener)
        val headerData = HeaderData(1, taskStatus = TaskStatus.NOT_STARTED, expanded = false)

        holder.bind(headerData)

        assertThat(ViewCompat.getStateDescription(holder.binding.root)).isEqualTo(
            context.getString(
                R.string.collapsed
            )
        )
    }

    @Test
    fun bindTaskViewHolder_initialState() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user, fakeClock)

        assertThat(holder.binding.taskListItem).isNull()
        assertThat(holder.accessibilityActionIds).isEmpty()
        assertThat(ViewCompat.getStateDescription(holder.binding.root)).isNull()
        assertThat(holder.binding.chipGroup.isImportantForAccessibility).isTrue()
        assertThat(holder.binding.root.contentDescription).isNull()

        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))
        assertThat(holder.binding.taskListItem).isEqualTo(inProgressTaskListItem)
        assertThat(ViewCompat.getStateDescription(holder.binding.root)).isEqualTo(
            context.getString(
                R.string.unstarred
            )
        )
        assertThat(holder.binding.chipGroup.isImportantForAccessibility).isFalse()
        assertThat(holder.binding.root.contentDescription).isNotNull()
    }

    @Test
    fun bindTaskViewHolder_starredTaskShownForCurrentUser() {
        val currentUser = user2
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, currentUser, fakeClock)

        assertFalse(holder.binding.star.isChecked)


        holder.bind(starredTaskListItem, DragAndDropActionsHelper(headerAndTask()))

        assertTrue(holder.binding.star.isChecked)
        assertThat(ViewCompat.getStateDescription(holder.binding.root)).isEqualTo(
            context.getString(
                R.string.starred
            )
        )
    }

    @Test
    fun bindTaskViewHolder_starredTaskNotShownForOtherUsers() {
        val currentUser = user
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, currentUser, fakeClock)

        holder.bind(starredTaskListItem, DragAndDropActionsHelper(headerAndTask()))

        assertFalse(holder.binding.star.isChecked)
    }

    @Test
    fun starTask() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user, fakeClock)

        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))

        assertFalse(holder.binding.star.isChecked)

        holder.binding.star.performClick()

        assertTrue(holder.binding.star.isChecked)
    }

    @Test
    fun unstarTask() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user2, fakeClock)

        holder.bind(starredTaskListItem, DragAndDropActionsHelper(headerAndTask()))

        assertTrue(holder.binding.star.isChecked)

        holder.binding.star.performClick()

        assertFalse(holder.binding.star.isChecked)
    }

    @Test
    fun replaceAccessibilityAction_ActionClickLabel() {
        val holder = TasksAdapter.TaskViewHolder.from(
            frameLayout,
            testItemListener,
            user,
            fakeClock
        )

        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))

        val nodeInfo = holder.binding.root.createAccessibilityNodeInfo()
        val actionClick = nodeInfo.actionList.filter {
            it.id == AccessibilityNodeInfo.ACTION_CLICK
        }[0]
        assertThat(actionClick.label).isEqualTo(context.resources.getString(R.string.explore_details))
    }

    @Test
    fun accessibilityAction_starItem() {
        val mockListener = Mockito.mock(TasksAdapter.ItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.binding.root.performAccessibilityAction(holder.accessibilityActionIds[1], null)

        Mockito.verify(mockListener).onStarClicked(inProgressTaskListItem)
    }

    @Test
    fun bindTaskViewHolder_addingAccessibilityAction_isIdempotent() {
        val holder =
            TasksAdapter.TaskViewHolder.from(frameLayout, testItemListener, user, fakeClock)
        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))
        val size = holder.accessibilityActionIds.size

        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))
        // If previously added accessibility actions are not cleared, when the holder is rebound,
        // the actions will get added again. This check guards against that.
        assertThat(holder.accessibilityActionIds.size).isEqualTo(size)
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

    @Test
    fun changePosition_whenStateIsDifferent_doesNothing() {

        val item1 = DataItem.TaskItem(inProgressTaskListItem)
        val item2 = DataItem.TaskItem(notStartedTaskListItem)

        tasksAdapter.submitList(listOf(item1, item2))
        tasksAdapter.changeTaskPosition(0, 1)

        assertThat(tasksAdapter.currentList[0]).isEqualTo(item1)
        assertThat(tasksAdapter.currentList[1]).isEqualTo(item2)
    }

    @Test
    fun changePosition_whenHeader_doesNothing() {

        val item1 = DataItem.TaskItem(inProgressTaskListItem)
        val item2 = inProgressHeader

        tasksAdapter.submitList(listOf(item1, item2))
        tasksAdapter.changeTaskPosition(0, 1)

        assertThat(tasksAdapter.currentList[0]).isEqualTo(item1)
        assertThat(tasksAdapter.currentList[1]).isEqualTo(item2)
    }

    // TODO: test for reordered list using drag/drop and undo actions See b/180352173

    @Test
    fun dragStarted_dragCompleted() {
        val mockListener = Mockito.mock(TasksAdapter.ItemListener::class.java)
        val holder = setUpAndBindTaskViewHolder(mockListener)

        holder.onItemMoveStarted()
        Mockito.verify(mockListener).onDragStarted()

        holder.onItemMoveCompleted(0, 1)
        Mockito.verify(mockListener).onDragCompleted(0, 1)
    }

    private fun setUpAndBindTaskViewHolder(
        listener: TasksAdapter.ItemListener
    ): TasksAdapter.TaskViewHolder {
        val holder = TasksAdapter.TaskViewHolder.from(frameLayout, listener, user, fakeClock)
        holder.bind(inProgressTaskListItem, DragAndDropActionsHelper(headerAndTask()))
        return holder
    }

    companion object {
        private val user = User(1, "user", Avatar.DEFAULT_USER)
        private val user2 = User(2, "user2", Avatar.DEFAULT_USER)

        val inProgressTaskListItem = TaskListItem(
            id = 1,
            title = "task list item 1",
            dueAt = Instant.now(),
            owner = user,
            status = TaskStatus.IN_PROGRESS,
            starUsers = emptyList(),
            tags = emptyList(),
            orderInCategory = 1
        )

        val starredTaskListItem = TaskListItem(
            id = 2,
            title = "task list item 2",
            dueAt = Instant.now(),
            owner = user2,
            status = TaskStatus.IN_PROGRESS,
            starUsers = listOf(user2),
            tags = emptyList(),
            orderInCategory = 2
        )

        val notStartedTaskListItem = TaskListItem(
            id = 3,
            title = "task list item 3",
            dueAt = Instant.now(),
            owner = user,
            status = TaskStatus.NOT_STARTED,
            starUsers = emptyList(),
            tags = emptyList(),
            orderInCategory = 3
        )

        val inProgressHeader = DataItem.HeaderItem(
            HeaderData(
                count = 3,
                taskStatus = TaskStatus.IN_PROGRESS,
                expanded = true
            )
        )

        fun headerAndTask() : List<DataItem> {
            return mutableListOf<DataItem>().apply {
                add(inProgressHeader)
                add(DataItem.TaskItem(inProgressTaskListItem))
            }
        }
    }
}
