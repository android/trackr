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

package com.example.android.trackr.data

object SeedData {
    // TODO(b/163065333): add descriptive task titles.
    val Tasks = listOf(
        Task(title = "Task 1"),
        Task(title = "Task 2"),
        Task(title = "Task 3", state = TaskState.IN_PROGRESS),
        Task(title = "Task 4", state = TaskState.COMPLETED),
        Task(title = "Task 5"),
        Task(title = "Task 6")
    )
}