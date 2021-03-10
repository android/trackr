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

package com.example.android.trackr.ui.tasks

import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskStatus


/**
 * Combines the results of [expandedStatesMap] and [taskSummaries] and returns a list of
 * [ListItem]s.
 * @param taskSummaries List of [TaskSummary]s, which could be null
 * @param  expandedStatesMap A [TaskStatus] to [Boolean] map, which determines the
 * collapsed/expanded state of a category of [TaskSummary]s
 * TODO: refactor into a UseCase.
 */
class ListItemsCreator(
    private val taskSummaries: List<TaskSummary>?,
    private val expandedStatesMap: MutableMap<TaskStatus, Boolean>?
) {
    fun execute(): List<ListItem>? {
        taskSummaries?.let { items ->
            expandedStatesMap?.let { statesMap ->
                val itemsToSubmit = mutableListOf<ListItem>()
                val statusToItemsMap: Map<TaskStatus, List<TaskSummary>>? =
                    items.groupBy { it.status }
                for (entry in statesMap) {
                    val sublist: List<TaskSummary>? = statusToItemsMap?.get(entry.key)
                    itemsToSubmit.add(
                        ListItem.TypeHeader(
                            HeaderData(
                                count = sublist?.size ?: 0,
                                taskStatus = entry.key,
                                expanded = entry.value
                            ),
                        )
                    )
                    if (statesMap[entry.key] == true) {
                        sublist?.sortedBy { it.orderInCategory }
                            ?.forEach { itemsToSubmit.add(ListItem.TypeTask(it)) }
                    }
                }
                return itemsToSubmit
            }
        }
        return null
    }
}