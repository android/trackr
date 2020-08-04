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

import android.graphics.Color.argb

object SeedData {
    private val user1 = User(1, "user1")
    private val user2 = User(2, "user2")
    private val user3 = User(3, "user3")

    private val i18nTag = Tag(1, "i18n")
    private val a11yTag = Tag(2, "a11y", argb(255, 200, 100, 30))
    private val androidTag = Tag(3, "android", argb(255, 100, 200, 30))
    private val serverTag = Tag(4, "server", argb(255, 200, 100, 30))
    private val designTag = Tag(5, "design")

    val tasks = listOf(
        Task(
            id = 1,
            title = "Make sure settings screen works with RTL, especially on small devices",
            reporter = user1,
            owner = user2,
            tags = listOf(i18nTag, androidTag)
        ),

        Task(
            id = 2,
            title = "Enable logging on XYZ component",
            state = TaskState.IN_PROGRESS,
            reporter = user2,
            owner = user3,
            tags = listOf(serverTag)
        ),

        Task(
            id = 3,
            title = "Create mocks for login screens",
            type = TaskType.FEATURE_REQUEST,
            reporter = user2,
            owner = user2,
            tags = listOf(designTag)
        ),

        Task(
            id = 4,
            title = "Try on app with Talkback and send feedback to team",
            reporter = user2,
            owner = user2,
            tags = listOf(androidTag, a11yTag)
        )
    )
}