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
import com.example.android.trackr.ui.ARCHIVED_TASK_1
import com.example.android.trackr.ui.USER_OWNER
import com.example.android.trackr.ui.archives.ArchiveViewModel
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.ArchivedTaskListItemsUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UnarchiveUseCase
import com.example.android.trackr.valueBlocking
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchiveViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): ArchiveViewModel {
        val db = createDatabase()
        return ArchiveViewModel(
            USER_OWNER,
            ArchivedTaskListItemsUseCase(db.taskDao()),
            ToggleTaskStarStateUseCase(db),
            ArchiveUseCase(db.taskDao()),
            UnarchiveUseCase(db.taskDao())
        )
    }

    @Test
    fun select() {
        val viewModel = createViewModel()
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(0)

        // Select
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isTrue()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(1)

        // Unselect
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(0)
    }

    @Test
    fun unarchive() {
        val viewModel = createViewModel()
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.undoableCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.archivedTasks.valueBlocking).hasSize(2)

        // Select
        viewModel.toggleTaskSelection(ARCHIVED_TASK_1.id)
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(1)
        assertThat(viewModel.undoableCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.archivedTasks.valueBlocking).hasSize(2)

        // Unarchive
        viewModel.unarchiveSelectedTasks()
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.undoableCount.valueBlocking).isEqualTo(1)
        assertThat(viewModel.archivedTasks.valueBlocking).hasSize(1)

        // Undo
        viewModel.undoUnarchiving()
        assertThat(viewModel.selectedCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.undoableCount.valueBlocking).isEqualTo(0)
        assertThat(viewModel.archivedTasks.valueBlocking).hasSize(2)
    }
}
