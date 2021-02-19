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

package com.example.android.trackr.ui.edit

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TaskDetail
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.User
import com.example.android.trackr.db.dao.TaskDao
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class TaskEditViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    var taskId: Long = 0L
        set(value) {
            field = value
            loadInitialData(value)
        }

    val title = MutableLiveData("")

    val description = MutableLiveData("")

    private val _status = MutableLiveData(TaskStatus.NOT_STARTED)
    val status: LiveData<TaskStatus> = _status

    private val _owner = MutableLiveData<User>()
    val owner: LiveData<User> = _owner

    private val _creator = MutableLiveData<User>()
    val creator: LiveData<User> = _creator

    private val _dueAt = MutableLiveData<Instant>()
    val dueAt: LiveData<Instant> = _dueAt

    private val _createdAt = MutableLiveData(Instant.now())
    val createdAt: LiveData<Instant> = _createdAt

    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    private var starUsers = mutableListOf<User>()

    lateinit var users: List<User>
        private set

    lateinit var allTags: List<Tag>
        private set

    private val _modified = MediatorLiveData<Boolean>().apply {
        val sources = listOf(title, description)
        var sourceCount = sources.size
        for (source in sources) {
            addSource(source.distinctUntilChanged()) {
                // Ignore initial data from each of the sources.
                if (sourceCount <= 0) {
                    value = true
                } else {
                    --sourceCount
                }
            }
        }
        value = false
    }

    private val _discarded = MutableLiveData(false)
    val discarded: LiveData<Boolean> = _discarded

    /**
     * Whether any of the content is modified or not.
     */
    val modified: LiveData<Boolean> = _modified

    private fun loadInitialData(taskId: Long) {
        viewModelScope.launch {
            users = taskDao.loadUsers()
            allTags = taskDao.loadTags()
            if (taskId != 0L) {
                val detail = taskDao.loadTaskDetailById(taskId)
                if (detail != null) {
                    title.value = detail.title
                    description.value = detail.description
                    _status.value = detail.status
                    _owner.value = detail.owner
                    _creator.value = detail.creator
                    _dueAt.value = detail.dueAt
                    _createdAt.value = detail.createdAt
                    _tags.value = detail.tags
                    starUsers.clear()
                    starUsers.addAll(detail.starUsers)
                    _modified.value = false
                }
            }
        }
    }

    fun updateState(status: TaskStatus) {
        if (_status.value != status) {
            _status.value = status
            _modified.value = true
        }
    }

    fun updateOwner(user: User) {
        _owner.value = user
        _modified.value = true
    }

    fun updateDueAt(instant: Instant) {
        _dueAt.value = instant
        _modified.value = true
    }

    fun addTag(tag: Tag) {
        _tags.value?.let { currentTags ->
            if (!currentTags.contains(tag)) {
                _tags.value = currentTags + tag
                _modified.value = true
            }
        }
    }

    fun removeTag(tag: Tag) {
        _tags.value?.let { currentTags ->
            if (currentTags.contains(tag)) {
                val tags = currentTags.toMutableList()
                tags.remove(tag)
                _tags.value = tags
                _modified.value = true
            }
        }
    }

    fun save(onSaveFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                taskDao.saveTaskDetail(
                    TaskDetail(
                        id = taskId,
                        title = title.value ?: "",
                        description = description.value ?: "",
                        status = _status.value ?: TaskStatus.NOT_STARTED,
                        createdAt = _createdAt.value ?: Instant.now(),
                        dueAt = _dueAt.value ?: Instant.now() + Duration.ofDays(7),
                        owner = _owner.value ?: users[0],
                        creator = _creator.value ?: users[0],
                        tags = _tags.value ?: emptyList(),
                        starUsers = starUsers
                    )
                )
                onSaveFinished(true)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                onSaveFinished(false)
            }
        }
    }

    fun discardChanges() {
        _discarded.value = true
    }
}
