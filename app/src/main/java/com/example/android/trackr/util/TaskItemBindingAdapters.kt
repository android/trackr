/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.trackr.R
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

private val DATE_TIME_FORMATTER_PATTERN = DateTimeFormatter.ofPattern("MMM d, YYYY")

/**
 * Binding adapter to format due date of task to a human-readable format.
 *
 * @param view to set dueDate text on.
 * @param dueDate as [Instant].
 */
@BindingAdapter("dueDate", "clock")
fun formatDate(view: TextView, dueDate: Instant, clock: Clock) {
    val daysTillDue =
        Duration.between(Instant.now(clock), dueDate).toDays().toInt()
    val resources = view.context.resources

    val readableDueDate =
        when (daysTillDue) {
            in Int.MIN_VALUE..-1 -> {
                val daysOverdue = daysTillDue.times(-1)
                resources.getQuantityString(
                    R.plurals.due_date_overdue_x_days,
                    daysOverdue,
                    daysOverdue
                )
            }
            0 -> resources.getString(R.string.due_date_today)
            in 1 until 5 -> resources.getQuantityString(
                R.plurals.due_date_days,
                daysTillDue,
                daysTillDue
            )
            else ->
                resources.getString(
                    R.string.due_date_generic,
                    ZonedDateTime
                        .ofInstant(dueDate, clock.zone)
                        .format(DATE_TIME_FORMATTER_PATTERN)
                )
        }

    view.text = readableDueDate
}