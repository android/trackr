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

import androidx.room.TypeConverter
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskStatus
import java.time.Instant

/**
 * Adds converters for custom types for working with database column values.
 */
class AppDatabaseTypeConverters {

    @TypeConverter
    fun instantToLong(value: Instant): Long {
        return value.toEpochMilli()
    }

    @TypeConverter
    fun longToInstant(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun taskStatusToInt(taskStatus: TaskStatus?): Int? {
        return taskStatus?.key
    }

    @TypeConverter
    fun intToTaskStatus(int: Int): TaskStatus? {
        return TaskStatus.fromKey(int)
    }

    @TypeConverter
    fun tagColorToString(color: TagColor?): String? {
        return color?.name
    }

    @TypeConverter
    fun stringToTagColor(string: String): TagColor {
        return TagColor.valueOf(string)
    }

    @TypeConverter
    fun avatarToString(avatar: Avatar): String {
        return avatar.name
    }

    @TypeConverter
    fun stringToAvatar(name: String): Avatar? {
        return Avatar.values().firstOrNull{ it.name == name}
    }
}