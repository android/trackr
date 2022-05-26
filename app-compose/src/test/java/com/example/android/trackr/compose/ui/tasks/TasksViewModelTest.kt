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

package com.example.android.trackr.compose.ui.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.compose.MainCoroutineRule
import com.example.android.trackr.compose.createTestDatabase
import com.example.android.trackr.data.SeedData
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class TasksViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rule = MainCoroutineRule()

    private fun createViewModel(): TasksViewModel {
        val db = createTestDatabase()
        val taskDao = db.taskDao()
        return TasksViewModel(
            SeedData.Users[0],
            Clock.systemDefaultZone(),
            GetOngoingTaskSummariesUseCase(taskDao),
            ToggleTaskStarStateUseCase(db, taskDao)
        )
    }

    @Test
    fun taskSummaries() = runTest {
        val viewModel = createViewModel()
        viewModel.statusGroups.first().let { statusGroups ->
            for ((_, group) in statusGroups) {
                assertThat(group.expanded).isTrue()
                // Verify the order within the group.
                var previousOrderInCategory = Int.MIN_VALUE
                for (summary in group.summaries) {
                    assertThat(summary.orderInCategory).isGreaterThan(previousOrderInCategory)
                    previousOrderInCategory = summary.orderInCategory
                }
            }
        }
    }

    @Ignore("Database.withTransaction hangs in tests")
    @Test
    fun toggleTaskStarState() = runTest {
        val viewModel = createViewModel()
        viewModel.statusGroups.first().let { statusGroups ->
            val group = statusGroups[TaskStatus.NOT_STARTED]!!
            assertThat(group.summaries[0].id).isEqualTo(1L)
            assertThat(group.summaries[0].starred).isTrue()
        }
        viewModel.toggleTaskStarState(1L)
        viewModel.statusGroups.first().let { statusGroups ->
            val group = statusGroups[TaskStatus.NOT_STARTED]!!
            assertThat(group.summaries[0].id).isEqualTo(1L)
            assertThat(group.summaries[0].starred).isFalse()
        }
    }

    @Test
    fun toggleStatusExpanded() = runTest {
        val viewModel = createViewModel()
        viewModel.statusGroups.first().let { statusGroups ->
            val group = statusGroups[TaskStatus.NOT_STARTED]!!
            assertThat(group.expanded).isTrue()
        }
        viewModel.toggleStatusExpanded(TaskStatus.NOT_STARTED)
        viewModel.statusGroups.first().let { statusGroups ->
            val group = statusGroups[TaskStatus.NOT_STARTED]!!
            assertThat(group.expanded).isFalse()
        }
    }
}
