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
import com.example.android.trackr.usecase.UnarchiveUseCase
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
    private val toggleTaskStarStateUseCase: ToggleTaskStarStateUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase,
    private val archiveUseCase: ArchiveUseCase,
    private val unarchiveUseCase: UnarchiveUseCase,
    private val currentUser: User
) : ViewModel() {

    // This can be observed by a client interested in presenting the undo logic for the task that
    // was archived.
    // TODO (b/165432948): consider a holistic approach to undoing actions.
    private val archivedItemChannel = Channel<ArchivedItem>(capacity = Channel.CONFLATED)
    val archivedItem = archivedItemChannel.receiveAsFlow()

    private val undoReorderTasksChannel = Channel<UndoReorderTasks>(capacity = Channel.CONFLATED)
    val undoReorderTasks = undoReorderTasksChannel.receiveAsFlow()

    private var detailTaskId: Long? = null
    private val showTaskDetailChannel = Channel<ShowTaskDetailEvent>(capacity = Channel.CONFLATED)
    val showTaskDetailEvents = showTaskDetailChannel.receiveAsFlow()

    private val taskSummaries = getOngoingTaskSummariesUseCase(currentUser.id)

    // TODO: don't hardcode TaskStatus values; instead, read from the db
    private val expandedStatesMap = MutableStateFlow(
        TaskStatus.values().associateWith { true }
    )

    val listItems = combine(taskSummaries, expandedStatesMap) { taskSummaries, statesMap ->
        ListItemsCreator(taskSummaries, statesMap).execute().also { items ->
            if (detailTaskId == null && taskSummaries.isNotEmpty()) {
                // Show the first item. This will set the detail pane content without opening it.
                val firstItem = items.first { it is ListItem.TypeTask } as ListItem.TypeTask
                showTaskDetail(firstItem.taskSummary, isUserSelection = false)
            }
        }
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
            archivedItemChannel.send(ArchivedItem(taskSummary.id))
        }
    }

    fun unarchiveTask(item: ArchivedItem) {
        viewModelScope.launch {
            unarchiveUseCase(listOf(item.taskId))
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
            undoReorderTasksChannel.trySend(
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

    fun showTaskDetail(taskSummary: TaskSummary, isUserSelection: Boolean = true) {
        showTaskDetailChannel.trySend(
            ShowTaskDetailEvent(taskSummary.id, taskSummary.id != detailTaskId, isUserSelection)
        )
        detailTaskId = taskSummary.id
    }
}

data class ArchivedItem(val taskId: Long)

data class UndoReorderTasks(
    val taskId: Long,
    val status: TaskStatus,
    val currentOrderInCategory: Int,
    val targetOrderInCategory: Int
)

data class ShowTaskDetailEvent(
    val taskId: Long,
    val isNewSelection: Boolean = true,
    val isUserSelection: Boolean = true
)
