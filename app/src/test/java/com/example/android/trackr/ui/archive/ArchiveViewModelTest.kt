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

package com.example.android.trackr.ui.archive

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.MainCoroutineRule
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.ui.ARCHIVED_TASK_1
import com.example.android.trackr.ui.USER_OWNER
import com.example.android.trackr.ui.archives.ArchiveViewModel
import com.example.android.trackr.ui.archives.UnarchiveAction
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.ArchivedTaskListItemsUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UnarchiveUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchiveViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var appDatabase: AppDatabase

    private fun createViewModel(): ArchiveViewModel {
        val taskDao = appDatabase.taskDao()
        return ArchiveViewModel(
            USER_OWNER,
            ArchivedTaskListItemsUseCase(taskDao),
            ToggleTaskStarStateUseCase(appDatabase),
            ArchiveUseCase(taskDao),
            UnarchiveUseCase(taskDao)
        )
    }

    @Before
    fun setup() {
        appDatabase = createDatabase()
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    fun select() = runTest {
        val viewModel = createViewModel()
        viewModel.archivedTasks.first().let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.first()).isEqualTo(0)

        // Select
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        viewModel.archivedTasks.first().let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isTrue()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.first()).isEqualTo(1)

        // Unselect
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        viewModel.archivedTasks.first().let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.first()).isEqualTo(0)
    }

    @Test
    fun unarchive() = runTest {
        val viewModel = createViewModel()

        // Collect UnarchiveActions. This is because asserting the absence of values emitted to a
        // flow is hard to do directly.
        val unarchiveActions = mutableListOf<UnarchiveAction>()
        val collectingUnarchiveActionsJob = launch(UnconfinedTestDispatcher()) {
            viewModel.unarchiveActions.collect {
                unarchiveActions.add(it)
            }
        }

        assertThat(viewModel.selectedCount.first()).isEqualTo(0)
        assertThat(viewModel.archivedTasks.first()).hasSize(2)
        assertThat(unarchiveActions).isEmpty()

        // Select
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        assertThat(viewModel.selectedCount.first()).isEqualTo(1)
        assertThat(viewModel.archivedTasks.first()).hasSize(2)
        assertThat(unarchiveActions).isEmpty()

        // Unarchive
        viewModel.unarchiveSelectedTasks()
        assertThat(viewModel.selectedCount.first()).isEqualTo(0)
        assertThat(viewModel.archivedTasks.first()).hasSize(1)
        assertThat(unarchiveActions).hasSize(1)
        val action = unarchiveActions[0]
        assertThat(action.taskIds).containsExactly(ARCHIVED_TASK_1.id)

        unarchiveActions.clear()

        // Undo
        viewModel.undoUnarchiving(action)
        assertThat(viewModel.selectedCount.first()).isEqualTo(0)
        assertThat(viewModel.archivedTasks.first()).hasSize(2)
        assertThat(unarchiveActions).isEmpty()

        collectingUnarchiveActionsJob.cancel()
    }
}
