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

package com.example.android.trackr.ui.archives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.example.android.trackr.ui.utils.WhileViewSubscribed
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.ArchivedTaskListItemsUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UnarchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val currentUser: User,
    archivedTaskListItemsUseCase: ArchivedTaskListItemsUseCase,
    private val toggleTaskStarStateUseCase: ToggleTaskStarStateUseCase,
    private val archiveUseCase: ArchiveUseCase,
    private val unarchiveUseCase: UnarchiveUseCase
) : ViewModel() {

    private val archivedTaskSummaries = archivedTaskListItemsUseCase(currentUser.id)
    private val selectedTaskIds = MutableStateFlow(emptySet<Long>())

    // Notify subscribers when 1 or more tasks are unarchived.
    private val _unarchiveActions = Channel<UnarchiveAction>(capacity = Channel.CONFLATED)
    val unarchiveActions = _unarchiveActions.receiveAsFlow()

    private var detailTaskId: Long? = null
    private val showTaskDetailChannel = Channel<ShowTaskDetailEvent>(capacity = Channel.CONFLATED)
    val showTaskDetailEvents = showTaskDetailChannel.receiveAsFlow()

    val archivedTasks = combine(archivedTaskSummaries, selectedTaskIds) { tasks, selectedIds ->
        tasks.map {
            ArchivedTask(it, it.id in selectedIds)
        }.also {
            if (detailTaskId == null && tasks.isNotEmpty()) {
                // Select the first item. This will set the detail pane content without opening it.
                selectTask(tasks[0], isUserSelection = false)
            }
        }
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    val selectedCount =
        selectedTaskIds.map { it.size }.stateIn(viewModelScope, WhileViewSubscribed, 0)

    fun toggleTaskSelection(taskId: Long) {
        val selected = selectedTaskIds.value
        if (taskId in selected) {
            selectedTaskIds.value = selected - taskId
        } else {
            selectedTaskIds.value = selected + taskId
        }
    }

    fun toggleTaskStarState(taskId: Long) {
        viewModelScope.launch {
            toggleTaskStarStateUseCase(taskId, currentUser)
        }
    }

    fun clearSelection() {
        selectedTaskIds.value = emptySet()
    }

    fun unarchiveSelectedTasks() {
        val ids = selectedTaskIds.value
        viewModelScope.launch {
            unarchiveUseCase(ids.toList())
            _unarchiveActions.trySend(UnarchiveAction(ids))
            clearSelection()
        }
    }

    fun undoUnarchiving(action: UnarchiveAction) {
        viewModelScope.launch {
            archiveUseCase(action.taskIds.toList())
        }
    }

    fun selectTask(taskSummary: TaskSummary, isUserSelection: Boolean = true) {
        showTaskDetailChannel.trySend(
            ShowTaskDetailEvent(taskSummary.id, taskSummary.id != detailTaskId, isUserSelection)
        )
        detailTaskId = taskSummary.id
    }
}

data class ArchivedTask(
    val taskSummary: TaskSummary,
    val selected: Boolean
)

data class UnarchiveAction(val taskIds: Set<Long>)

data class ShowTaskDetailEvent(
    val taskId: Long,
    val isNewSelection: Boolean = true,
    val isUserSelection: Boolean = true
)
