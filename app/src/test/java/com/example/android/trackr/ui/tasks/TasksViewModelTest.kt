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

package com.example.android.trackr.ui.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.MainCoroutineRule
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.ui.TASK_1
import com.example.android.trackr.ui.USER_OWNER
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ReorderTasksUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UnarchiveUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TasksViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private fun createViewModel(): TasksViewModel {
        val db = createDatabase()
        val taskDao = db.taskDao()
        return TasksViewModel(
            GetOngoingTaskSummariesUseCase(taskDao),
            ToggleTaskStarStateUseCase(db, taskDao),
            ReorderTasksUseCase(taskDao),
            ArchiveUseCase(taskDao),
            UnarchiveUseCase(taskDao),
            currentUser
        )
    }

    @Test
    fun listItems() = runTest {
        val viewModel = createViewModel()
        viewModel.listItems.first().let { listItems ->
            assertThat(listItems).hasSize(4)
            val task = listItems.find { it is ListItem.TypeTask } as ListItem.TypeTask
            assertThat(task.taskSummary.id).isEqualTo(TASK_1.id)
        }
    }

    @Test
    fun toggleExpandedState() = runTest {
        val viewModel = createViewModel()
        viewModel.listItems.first().let { listItems ->
            val header = listItems.find {
                it is ListItem.TypeHeader && it.headerData.taskStatus == TaskStatus.IN_PROGRESS
            } as ListItem.TypeHeader
            assertThat(header.headerData.count).isEqualTo(1)
            assertThat(header.headerData.expanded).isTrue()
        }
        viewModel.toggleExpandedState(createHeaderItem(TaskStatus.IN_PROGRESS))
        viewModel.listItems.first().let { listItems ->
            val header = listItems.find {
                it is ListItem.TypeHeader && it.headerData.taskStatus == TaskStatus.IN_PROGRESS
            } as ListItem.TypeHeader
            assertThat(header.headerData.expanded).isFalse()
        }
    }

    @Test
    fun archiveTask() = runTest {
        val viewModel = createViewModel()

        // Collect ArchivedItems. This is because asserting the absence of values emitted to a
        // flow is hard to do directly.
        val archivedItems = mutableListOf<ArchivedItem>()
        val collectingArchivedItemJob = launch(UnconfinedTestDispatcher()) {
            viewModel.archivedItem.collect {
                archivedItems.add(it)
            }
        }

        assertThat(archivedItems).isEmpty()

        viewModel.archiveTask(createTaskSummary(TASK_1.id))
        assertThat(archivedItems).hasSize(1)
        val item = archivedItems[0]
        assertThat(item.taskId).isEqualTo(TASK_1.id)

        archivedItems.clear()

        viewModel.unarchiveTask(item)
        assertThat(archivedItems).isEmpty()

        collectingArchivedItemJob.cancel()
    }

    private fun createHeaderItem(status: TaskStatus): HeaderData {
        return HeaderData(0, status, true)
    }

    private fun createTaskSummary(id: Long): TaskSummary {
        return TaskSummary(
            id = id,
            title = "",
            status = TaskStatus.NOT_STARTED,
            dueAt = Instant.parse("2020-09-01T00:00:00.00Z"),
            orderInCategory = 1,
            owner = currentUser,
            tags = emptyList(),
            starred = false,
        )
    }

    private val currentUser = USER_OWNER
}
