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

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
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

    private val taskListItems: LiveData<List<TaskListItem>>
        get() = taskDao.getOngoingTaskListItems()

    // TODO: don't hardcode TaskState values; instead, read from the db
    private val _expandedStatesMap: MutableLiveData<MutableMap<TaskState, Boolean>> =
        MutableLiveData(
            mutableMapOf(
                TaskState.IN_PROGRESS to true,
                TaskState.NOT_STARTED to true,
                TaskState.COMPLETED to true
            )
        )
    private val expandedStatesMap: LiveData<MutableMap<TaskState, Boolean>> = _expandedStatesMap

    var dataItems: LiveData<List<DataItem>> = MediatorLiveData<List<DataItem>>().apply {
        var cachedTaskListItems: List<TaskListItem>? = null

        addSource(taskListItems) {
            // In case the user changes the expanded/collapsed state, avoid a new db write by
            // providing cached data.
            cachedTaskListItems = it
            DataItemsCreator(it, expandedStatesMap.value).execute()?.let { result ->
                value = result
            }
        }

        addSource(expandedStatesMap) {
            DataItemsCreator(cachedTaskListItems, it).execute()?.let { result ->
                value = result
            }
        }
    }

    fun toggleExpandedState(headerData: HeaderData) {
        _expandedStatesMap.value = _expandedStatesMap.value?.also { it ->
            it[headerData.taskState] = !it[headerData.taskState]!!
        }
    }

    fun toggleTaskStarState(taskListItem: TaskListItem, currentUser: User) {
        viewModelScope.launch {
            val isTaskStarred = taskListItem.starUsers.contains(currentUser)

            if (isTaskStarred) {
                val existingUserTask = taskDao.getUserTask(taskListItem.id, currentUser.id)
                if (existingUserTask != null) {
                    taskDao.deleteUserTasks(listOf(existingUserTask))
                } else {
                    Log.e(TAG, "Error deleting user task because it's not in the database")
                }
            } else {
                val newUserTask = UserTask(userId = currentUser.id, taskId = taskListItem.id)
                taskDao.insertUserTasks(listOf(newUserTask))
            }
        }
    }

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

    companion object {
        private const val TAG = "TasksViewModel"
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