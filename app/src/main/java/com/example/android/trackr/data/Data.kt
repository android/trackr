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

import android.graphics.Color

data class Issue(
    val id: Long,

    /**
     * The issue title. TODO: consider adding char limit which may help showcase a11y validation issues.
     */
    var title: String,

    /**
     * The issue description, which can be verbose.
     */
    var description: String = "",

    /**
     * The issue type.
     */
    val type: IssueType = IssueType.BUG,

    /**
     * The state of the issue.
     */
    val state: IssueState = IssueState.NOT_STARTED,

    /**
     * The team member who created the issue (this defaults to the current user).
     */
    val reporter: User,

    /**
     * The team member who the issue has been assigned to.
     */
    val owner: User,

    /**
     * An arbitrary list of tags associated with an issue.
     */
    val tags: List<Tag> = emptyList()
)

// TODO: put in adapter?
data class HeaderData(
    val title: String,
    val count: Int
)

enum class IssueType {
    BUG,
    FEATURE_REQUEST
}

enum class IssueState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}

data class Tag(
    val id: Long,

    /**
     * A short label for the tag.
     */
    var label: String,

    // TODO: consider making the label optional and adding an icon/pattern for color-only tags.

    /**
     * A color associated with the tag.
     */
    var color: Int = Color.rgb(255, 255, 255)
)

data class User (
    val id: Long,

    /**
     * A short name for the user.
     */
    val username: String,

    // TODO: add field for avatar.

    /**
     * The list of issues that the user has starred.
     */
    val starredIssues: List<Issue> = emptyList()
)
