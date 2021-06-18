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

package com.example.android.trackr.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskDetail
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.dao.TaskDao

@Database(
    entities = [Task::class, Tag::class, User::class, TaskTag::class, UserTask::class],
    views = [TaskSummary::class, TaskDetail::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
}
