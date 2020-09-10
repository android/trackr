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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.db.dao.TaskDao
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    // This can be observed by a client interested in presenting the undo logic for the task that
    // was archived.
    // TODO (b/165432948): consider a holistic approach to undoing actions.
    private val _archivedItem: MutableLiveData<ArchivedItem?> = MutableLiveData()
    val archivedItem: LiveData<ArchivedItem?> = _archivedItem

    val taskListItems: LiveData<List<TaskListItem>>
        get() = taskDao.getOngoingTaskListItems()

    fun archiveTask(taskListItem: TaskListItem) {
        _archivedItem.value = ArchivedItem(taskListItem.id, taskListItem.state)

        viewModelScope.launch {
            taskDao.updateTaskState(taskListItem.id, TaskState.ARCHIVED)
        }
    }

    fun unarchiveTask() {
        archivedItem.value?.let {
            viewModelScope.launch {
                taskDao.updateTaskState(it.taskId, it.previousState)
            }
            _archivedItem.value = null
        }
    }
}

/**
 * Contains archived task fields that can be used to unarchive that task and restore the tasks
 * previous state.
 */
data class ArchivedItem(
    val taskId: Long,
    val previousState: TaskState
)