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

import java.time.Duration
import java.time.Instant

object SeedData {

    val Users = listOf(
        User(id = 1L, username = "Daring Dove", avatar = Avatar.DARING_DOVE),
        User(id = 2L, username = "Likeable Lark", avatar = Avatar.LIKEABLE_LARK),
        User(id = 3L, username = "Peaceful Puffin", avatar = Avatar.PEACEFUL_PUFFIN)
    )

    val Tags = listOf(
        Tag(id = 1L, label = "2.3 release", color = TagColor.BLUE),
        Tag(id = 2L, label = "2.4 release", color = TagColor.RED),
        Tag(id = 3L, label = "a11y", color = TagColor.GREEN),
        Tag(id = 4L, label = "UI/UX", color = TagColor.PURPLE),
        Tag(id = 5L, label = "eng", color = TagColor.TEAL),
        Tag(id = 6L, label ="VisD", color = TagColor.YELLOW),
    )

    val Tasks = arrayListOf(
        Task(
            id = 1L,
            title = "New mocks for tablet",
            description = "For our mobile apps, we currently only target phones. We need to " +
                    "start moving towards supporting tablets as well, and we should kick off the " +
                    "process of getting the mocks together",
            status = TaskStatus.NOT_STARTED,
            ownerId = 1L,
            creatorId = 2L,
            orderInCategory = 1
        ),
        Task(id = 2L,
            title = "Move the owner icon",
            description = "Our UX research has shown that users prefer that, and even though " +
                    "all us remain split on this, let’s go ahead and move the owner icon in the " +
                    "way specified in the mocks.",
            status = TaskStatus.NOT_STARTED,
            ownerId = 1L,
            creatorId = 3L,
            dueAt = Instant.now() - Duration.ofDays(1),
            orderInCategory = 2
        ),
        Task(
            id = 3L,
            title = "Allow optional guests",
            description = "Feedback from product: people find our app useful for scheduling " +
                    "things, but they really, really want to have a way to have optional " +
                    "invites. Right now, anyone who receives a calendar invite has no way of " +
                    "knowing how important (or unimportant) their presence is at the meeting. " +
                    "Alternatively, we could set up some kind of numerical 'important to attend' " +
                    "system (with 5 as most important, and 1 as totally optional), but this " +
                    "might be needlessly complex. Adding a simple “optional” bit in db might " +
                    "be all we need (plus the necessary UI changes). \n",
            status = TaskStatus.NOT_STARTED,
            ownerId = 2L,
            creatorId = 1L,
            createdAt = Instant.now() - Duration.ofDays(1),
            dueAt = Instant.now() - Duration.ofDays(2),
            orderInCategory = 3
        ),
        Task(
            id = 4L,
            title = "Suggest meeting times",
            description = "I’m finding that I have to look at the invitees’ calendars and work " +
                    "out the optimal time for a meeting. Surely our app can work this out and " +
                    "just offer a few times as the guest list is created? I think this could save " +
                    "our users a good amount of time. ",
            status = TaskStatus.NOT_STARTED,
            ownerId = 3L,
            creatorId = 2L,
            dueAt = Instant.now() + Duration.ofDays(5),
            orderInCategory = 4
        ),
        Task(
            id = 5L,
            title = "Enable share feature",
            description = "We’ve talked about this feature for a while and it’s fairly easy to " +
                    "implement (and the mocks exist). Let’s get it in the next release \n",
            status = TaskStatus.NOT_STARTED,
            ownerId = 1L,
            creatorId = 3L,
            dueAt = Instant.now() - Duration.ofDays(10),
            orderInCategory = 5
        ),

        Task(
            id = 6L,
            title = "Support display modes",
            description = "Default to the weekly view in desktop and daily view in mobile , but " +
                    "let users switch between modes seamlessly",
            status = TaskStatus.IN_PROGRESS,
            ownerId = 2L,
            creatorId = 2L,
            dueAt = Instant.now() + Duration.ofDays(2),
            orderInCategory = 1
        ),
        Task(
            id = 7L,
            title = "Let users set bg color",
            description = "We may want to present a finite palette of colors from which the user " +
                    "chooses the default; if there are a large number of options, we’ll probably " +
                    "find ourselves dealing with poor contrast between foreground and background",
            status = TaskStatus.IN_PROGRESS,
            ownerId = 1L,
            creatorId = 2L,
            dueAt = Instant.now() + Duration.ofDays(12),
            orderInCategory = 2
        ),
        Task(
            id = 8L,
            title = "Implement smart sync",
            status = TaskStatus.COMPLETED,
            ownerId = 1L,
            creatorId = 1L,
            dueAt = Instant.now() + Duration.ofDays(22),
            orderInCategory = 1
        ),
        Task(
            id = 9L,
            title = "Smartwatch UI design",
            status = TaskStatus.COMPLETED,
            ownerId = 3L,
            creatorId = 3L,
            dueAt = Instant.now() + Duration.ofDays(14),
            orderInCategory = 2
        ),
        Task(
            id = 10L,
            title = "New content for notifications",
            description = "Notif types: event coming up in [x] mins; event right now; time to " +
                    "leave; event changed / cancelled; invited to new event",
            status = TaskStatus.COMPLETED,
            ownerId = 3L,
            creatorId = 2L,
            dueAt = Instant.now() - Duration.ofDays(9),
            orderInCategory = 3
        ),
        Task(
            id = 11L,
            title = "Create default illustrations for event types",
            description = "Event types: coffee, lunch, gym, sports, travel, doctors appointment, " +
                    "game day, presentation, movie, theatre, class",
            status = TaskStatus.COMPLETED,
            ownerId = 1L,
            creatorId = 3L,
            dueAt = Instant.now() + Duration.ofDays(6),
            orderInCategory = 4
        ),
        Task(
            id = 12L,
            title = "Auto-decline holiday events",
            description = "User setting that automatically declines new meetings if they’re set " +
                    "on a holiday",
            status = TaskStatus.COMPLETED,
            ownerId = 2L,
            creatorId = 3L,
            dueAt = Instant.now() - Duration.ofDays(3),
            orderInCategory = 5
        ),
        Task(
            id = 13L,
            title = "Holiday conflict warning",
            description = "Pop up dialog warning user if they try to schedule a meeting on at " +
                    "least one of the participants’ holiday, depending on user profile location",
            status = TaskStatus.COMPLETED,
            ownerId = 1L,
            creatorId = 1L,
            dueAt = Instant.now() + Duration.ofDays(1),
            orderInCategory = 1,
            isArchived = true
        ),
        Task(
            id = 14L,
            title = "Prepopulate holidays",
            description = "Show national holidays (listed at top of each day’s schedule) " +
                    "directly in calendar view based on user profile location. Let users opt out " +
                    "of this if they want",
            status = TaskStatus.COMPLETED,
            ownerId = 2L,
            creatorId = 3L,
            dueAt = Instant.now() + Duration.ofDays(8),
            orderInCategory = 2,
            isArchived = true
        ),
        Task(
            id = 15L,
            title = "Holiday-specific illustrations",
            description = "Create illustrations that match each holiday. Prioritize for tier 1 " +
                    "regions first",
            status = TaskStatus.COMPLETED,
            ownerId = 3L,
            creatorId = 3L,
            orderInCategory = 3,
            isArchived = true
        ),
    )

    val TaskTags = listOf(
        TaskTag(taskId = 1L, tagId = 2L),
        TaskTag(taskId = 1L, tagId = 4L),

        TaskTag(taskId = 2L, tagId = 1L),

        TaskTag(taskId = 3L, tagId = 4L),
        TaskTag(taskId = 3L, tagId = 5L),

        TaskTag(taskId = 4L, tagId = 2L),
        TaskTag(taskId = 4L, tagId = 5L),

        TaskTag(taskId = 5L, tagId = 1L),
        TaskTag(taskId = 5L, tagId = 4L),

        TaskTag(taskId = 6L, tagId = 1L),
        TaskTag(taskId = 6L, tagId = 5L),
        TaskTag(taskId = 6L, tagId = 6L),

        TaskTag(taskId = 7L, tagId = 2L),
        TaskTag(taskId = 7L, tagId = 3L),

        TaskTag(taskId = 8L, tagId = 1L),

        TaskTag(taskId = 9L, tagId = 2L),

        TaskTag(taskId = 10L, tagId = 1L),
        TaskTag(taskId = 10L, tagId = 4L),

        TaskTag(taskId = 11L, tagId = 2L),

        TaskTag(taskId = 12L, tagId = 4L),
        TaskTag(taskId = 12L, tagId = 6L),

        TaskTag(taskId = 13L, tagId = 1L),
        TaskTag(taskId = 13L, tagId = 3L),
        TaskTag(taskId = 13L, tagId = 5L),
        TaskTag(taskId = 13L, tagId = 6L),

        TaskTag(taskId = 14L, tagId = 2L),

        TaskTag(taskId = 15L, tagId = 1L),
        TaskTag(taskId = 15L, tagId = 6L)
    )

    val UserTasks = listOf(
        UserTask(userId = 1L, taskId = 1L)
    )
}
