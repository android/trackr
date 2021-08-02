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

package com.example.android.trackr.utils

import android.content.res.Resources
import com.example.android.trackr.shared.R
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val DATE_TIME_FORMATTER_PATTERN = DateTimeFormatter.ofPattern("MMM d, yyyy")
    const val MAX_NUM_DAYS_FOR_CUSTOM_MESSAGE = 5

    /**
     * Returns `dueDate` in human readable format.
     */
    fun formattedDate(resources: Resources, dueDate: Instant, clock: Clock): String {
        return resources.getString(
            R.string.due_date_generic,
            ZonedDateTime
                .ofInstant(dueDate, clock.zone)
                .format(DATE_TIME_FORMATTER_PATTERN)
        )
    }

    /**
     * Returns either a message about the time till the due date (example: "Due today", or
     * "Due 3 days ago") or returns a human readable version of the due date (Example:
     * "October 10, 2021")
     */
    fun durationMessageOrDueDate(
        resources: Resources,
        dueDate: Instant,
        clock: Clock
    ): String {
        val message = durationMessage(resources, dueDate, clock)
        return if (message.isNotEmpty()) {
            message
        } else {
            formattedDate(resources, dueDate, clock)
        }
    }

    /**
     * Returns a message about the time till the due date (example: "Due today", or
     * "Due 3 days ago"). Used to draw attention to items which are overdue or about to become due.
     */
    fun durationMessage(
        resources: Resources,
        dueDate: Instant,
        clock: Clock
    ): String {
        return when (val daysTillDue =
            Duration.between(Instant.now(clock), dueDate).toDays().toInt()) {
            in Int.MIN_VALUE..-1 -> {
                val daysOverdue = daysTillDue.times(-1)
                resources.getQuantityString(
                    R.plurals.due_date_overdue_x_days,
                    daysOverdue,
                    daysOverdue
                )
            }
            0 -> {
                resources.getString(R.string.due_date_today)
            }
            in 1 until MAX_NUM_DAYS_FOR_CUSTOM_MESSAGE ->
                resources.getQuantityString(
                    R.plurals.due_date_days,
                    daysTillDue,
                    daysTillDue
                )
            else ->
                ""
        }
    }
}