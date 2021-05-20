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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.User
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ReorderListUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UpdateTaskStatusUseCase
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    getOngoingTaskSummariesUseCase: GetOngoingTaskSummariesUseCase,
    private val archiveUseCase: ArchiveUseCase,
    private val toggleTaskStarStateUseCase: ToggleTaskStarStateUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val reorderListUseCase: ReorderListUseCase,
    private val currentUser: User
) : ViewModel() {

    // This can be observed by a client interested in presenting the undo logic for the task that
    // was archived.
    // TODO (b/165432948): consider a holistic approach to undoing actions.
    private val _archivedItem: MutableLiveData<ArchivedItem?> = MutableLiveData()
    val archivedItem: LiveData<ArchivedItem?> = _archivedItem

    private val taskSummaries = getOngoingTaskSummariesUseCase()

    // TODO: don't hardcode TaskStatus values; instead, read from the db
    private val _expandedStatesMap: MutableLiveData<MutableMap<TaskStatus, Boolean>> =
        MutableLiveData(
            mutableMapOf(
                TaskStatus.IN_PROGRESS to true,
                TaskStatus.NOT_STARTED to true,
                TaskStatus.COMPLETED to true
            )
        )
    private val expandedStatesMap: LiveData<MutableMap<TaskStatus, Boolean>> = _expandedStatesMap

    val listItems: LiveData<List<ListItem>> = MediatorLiveData<List<ListItem>>().apply {
        var cachedTaskSummaries: List<TaskSummary>? = null

        addSource(taskSummaries) {
            // In case the user changes the expanded/collapsed state, avoid a new db write by
            // providing cached data.
            cachedTaskSummaries = it
            ListItemsCreator(it, expandedStatesMap.value).execute()?.let { result ->
                value = result
            }
        }

        addSource(expandedStatesMap) {
            ListItemsCreator(cachedTaskSummaries, it).execute()?.let { result ->
                value = result
            }
        }
    }

    fun toggleExpandedState(headerData: HeaderData) {
        _expandedStatesMap.value = _expandedStatesMap.value?.also { it ->
            it[headerData.taskStatus] = !it[headerData.taskStatus]!!
        }
    }

    fun toggleTaskStarState(taskSummary: TaskSummary) {
        viewModelScope.launch {
            toggleTaskStarStateUseCase(taskSummary.id, currentUser)
        }
    }

    fun archiveTask(taskSummary: TaskSummary) {
        _archivedItem.value = ArchivedItem(taskSummary.id, taskSummary.status)

        viewModelScope.launch {
            archiveUseCase(listOf(taskSummary.id))
        }
    }

    fun unarchiveTask() {
        archivedItem.value?.let {
            viewModelScope.launch {
                updateTaskStatusUseCase(listOf(it.taskId), it.previousStatus)
            }
            _archivedItem.value = null
        }
    }

    private var cachedList: List<TaskSummary> = emptyList()
    private var dragAndDropCategory: TaskStatus? = null

    fun cacheCurrentList(items: List<TaskSummary>) {
        cachedList = items
    }

    fun restoreListFromCache() {
        viewModelScope.launch {
            dragAndDropCategory?.let {
                reorderListUseCase(it, cachedList)
            }
        }
    }

    fun persistUpdatedList(status: TaskStatus, items: List<TaskSummary>) {
        dragAndDropCategory = status
        viewModelScope.launch {
            reorderListUseCase(status, cachedList)
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