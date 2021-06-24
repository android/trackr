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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.example.android.trackr.ui.utils.WhileViewSubscribed
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ReorderTasksUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    getOngoingTaskSummariesUseCase: GetOngoingTaskSummariesUseCase,
    private val archiveUseCase: ArchiveUseCase,
    private val toggleTaskStarStateUseCase: ToggleTaskStarStateUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase,
    private val currentUser: User
) : ViewModel() {

    // This can be observed by a client interested in presenting the undo logic for the task that
    // was archived.
    // TODO (b/165432948): consider a holistic approach to undoing actions.
    private val archivedItemChannel = Channel<ArchivedItem>(capacity = Channel.CONFLATED)
    val archivedItem = archivedItemChannel.receiveAsFlow()

    private val undoReorderTasksChannel = Channel<UndoReorderTasks>(capacity = Channel.CONFLATED)
    val undoReorderTasks = undoReorderTasksChannel.receiveAsFlow()

    private val taskSummaries = getOngoingTaskSummariesUseCase()

    // TODO: don't hardcode TaskStatus values; instead, read from the db
    private val expandedStatesMap = MutableStateFlow(
        mapOf(
            TaskStatus.IN_PROGRESS to true,
            TaskStatus.NOT_STARTED to true,
            TaskStatus.COMPLETED to true
        )
    )

    val listItems = combine(taskSummaries, expandedStatesMap) { taskSummaries, statesMap ->
        ListItemsCreator(taskSummaries, statesMap).execute()
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    fun toggleExpandedState(headerData: HeaderData) {
        val map = expandedStatesMap.value.toMutableMap()
        val previous = map[headerData.taskStatus] ?: return
        map[headerData.taskStatus] = !previous
        expandedStatesMap.value = map
    }

    fun toggleTaskStarState(taskSummary: TaskSummary) {
        viewModelScope.launch {
            toggleTaskStarStateUseCase(taskSummary.id, currentUser)
        }
    }

    fun archiveTask(taskSummary: TaskSummary) {
        viewModelScope.launch {
            archiveUseCase(listOf(taskSummary.id))
            archivedItemChannel.offer(ArchivedItem(taskSummary.id, taskSummary.status))
        }
    }

    fun unarchiveTask(item: ArchivedItem) {
        viewModelScope.launch {
            updateTaskStatusUseCase(listOf(item.taskId), item.previousStatus)
        }
    }

    fun reorderTasks(movedTask: TaskSummary, targetTask: TaskSummary) {
        if (movedTask.status != targetTask.status) {
            return
        }
        viewModelScope.launch {
            reorderTasksUseCase(
                movedTask.id,
                movedTask.status,
                movedTask.orderInCategory,
                targetTask.orderInCategory
            )
            undoReorderTasksChannel.offer(
                UndoReorderTasks(
                    movedTask.id,
                    movedTask.status,
                    targetTask.orderInCategory,
                    movedTask.orderInCategory
                )
            )
        }
    }

    fun undoReorderTasks(undo: UndoReorderTasks) {
        viewModelScope.launch {
            reorderTasksUseCase(
                undo.taskId,
                undo.status,
                undo.currentOrderInCategory,
                undo.targetOrderInCategory
            )
        }
    }
}

/**
 * Contains archived task fields that can be used to unarchive that task and restore the tasks
 * previous state.
 */
data class ArchivedItem(
    val taskId: Long,
    val previousStatus: TaskStatus
)

data class UndoReorderTasks(
    val taskId: Long,
    val status: TaskStatus,
    val currentOrderInCategory: Int,
    val targetOrderInCategory: Int
)
