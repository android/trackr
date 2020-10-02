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

import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.TaskListItem
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.objectweb.asm.tree.analysis.Frame
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TasksAdapterTest {
    class TestListener : TasksAdapter.TaskItemListener {
        override fun onItemClicked(taskListItem: TaskListItem) {}
        override fun onItemArchived(taskListItem: TaskListItem) {}
    }

    private val tasksAdapter = TasksAdapter(TestListener())
    private lateinit var frameLayout: FrameLayout

    @Before
    fun setup() {
        frameLayout = FrameLayout(ApplicationProvider.getApplicationContext())
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
}
