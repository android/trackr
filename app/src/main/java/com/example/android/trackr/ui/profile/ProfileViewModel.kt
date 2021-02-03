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

package com.example.android.trackr.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.android.trackr.db.dao.TaskDao

class ProfileViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    private val _userId = MutableLiveData<Long>()
    var userId: Long?
        get() = _userId.value
        set(value) {
            _userId.value = value!!
        }

    val user = _userId.switchMap { id -> taskDao.getUserById(id) }
}