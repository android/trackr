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

package com.example.android.trackr.ui.utils

import android.content.Context
import android.text.TextUtils
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.utils.DateTimeUtils
import java.time.Clock

object AccessibilityUtils {
    private const val COLON_SEPARATOR = ": "
    private const val PERIOD_SEPARATOR = "."
    private const val PERIOD_SEPARATOR_AND_SPACE = "$PERIOD_SEPARATOR "

    fun taskSummaryLabel(context: Context, taskSummary: TaskSummary, clock: Clock): String {
        val sb = StringBuffer()

        sb.append(taskSummary.title).append(PERIOD_SEPARATOR_AND_SPACE)

        sb.append(context.getString(R.string.owner)).append(COLON_SEPARATOR)
        sb.append(taskSummary.owner.username).append(PERIOD_SEPARATOR_AND_SPACE)

        sb.append(
            DateTimeUtils.durationMessageOrDueDate(
                context.resources,
                taskSummary.dueAt,
                clock
            )
        )
        sb.append(if (taskSummary.tags.isEmpty()) PERIOD_SEPARATOR else PERIOD_SEPARATOR_AND_SPACE)

        sb.append(
            TextUtils.join(
                PERIOD_SEPARATOR_AND_SPACE,
                taskSummary.tags.map { context.getString(R.string.tag_label, it.label) })
        )

        return sb.toString()
    }
}